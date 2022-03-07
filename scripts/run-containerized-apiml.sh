#!/bin/bash -e

###################################################################
# This program and the accompanying materials are made available under the terms of the
# Eclipse Public License v2.0 which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#
# Copyright IBM Corporation 2020
###################################################################
################################################################################
# Run containerized APIML prepared for Files API
#
# This script requires these environment variables:
# - ZOSMF_HOST
# - ZOSMF_PORT
################################################################################
ZOSMF_PORT=443
ZOSMF_HOST=tvt5003.svl.ibm.com
######################## DEFINE CONSTANTS #########################
SCRIPT_NAME=$(basename "$0")
SCRIPT_PWD=$(cd "$(dirname "$0")" && pwd)
ROOT_DIR=$(cd "$SCRIPT_PWD" && cd .. && pwd)
WORKSPACE_SHORT=.apiml
WORKSPACE="${ROOT_DIR}/${WORKSPACE_SHORT}"
KEYSTORE_DIR=keystore
API_DEFS_DIR=api-defs
###################################################################
###################### VALIDATE PARAMETERS ########################
echo "[${SCRIPT_NAME}] validate parameters"
if [ -z "${ZOSMF_HOST}" ]; then
  echo "[${SCRIPT_NAME}][error] environment variable ZOSMF_HOST is required."
  exit 1
fi
if [ -z "${ZOSMF_PORT}" ]; then
  echo "[${SCRIPT_NAME}][error] environment variable ZOSMF_PORT is required."
  exit 1
fi
if [ -z "${DISCOVERY_PORT}" ]; then
  DISCOVERY_PORT=7553
fi
if [ -z "${GATEWAY_PORT}" ]; then
  GATEWAY_PORT=7554
fi
if [ -z "${FILES_PORT}" ]; then
  FILES_PORT=8443
fi
if [ -z "${HOST_OS}" ]; then
  HOST_OS="not-linux"
fi
if [ -z $(command -v docker-compose) ]; then
  echo "[${SCRIPT_NAME}][error] docker-compose is required."
  exit 1
fi

FILES_HOST="host.docker.internal"
dockerComposeTemplate="docker-compose.yml.template"
if [ "$HOST_OS" == "linux" ]; then
  dockerComposeTemplate="docker-compose.yml.linux.template"
  FILES_HOST="localhost"
fi
###################################################################
######################## CREATE WORKSPACE #########################
echo "[${SCRIPT_NAME}] prepare workspace"
rm -rf "${WORKSPACE}"
mkdir -p "${WORKSPACE}/${KEYSTORE_DIR}"
mkdir -p "${WORKSPACE}/${API_DEFS_DIR}"
echo
###################################################################
################ PREPARE CERTIFICATES AND KEYSTORE ################
echo "[${SCRIPT_NAME}] prepare certificates"
echo "[${SCRIPT_NAME}] - generate CA in PKCS12 format"
keytool -genkeypair -v \
  -alias localca \
  -keyalg RSA -keysize 2048 \
  -keystore "${WORKSPACE}/${KEYSTORE_DIR}/localca.keystore.p12" \
  -dname "CN=Zowe Development Instances Certificate Authority, OU=API Mediation Layer, O=Zowe Sample, L=Prague, S=Prague, C=CZ" \
  -keypass local_ca_password \
  -storepass local_ca_password \
  -storetype PKCS12 \
  -validity 3650 \
  -ext KeyUsage=keyCertSign -ext BasicConstraints:critical=ca:true
echo "[${SCRIPT_NAME}] - export CA public key"
keytool -export -v \
  -alias localca \
  -file "${WORKSPACE}/${KEYSTORE_DIR}/localca.cer" \
  -keystore "${WORKSPACE}/${KEYSTORE_DIR}/localca.keystore.p12" \
  -rfc \
  -keypass local_ca_password \
  -storepass local_ca_password \
  -storetype PKCS12
echo "[${SCRIPT_NAME}] - generate server certificate in PKCS12 format"
keytool -genkeypair -v \
  -alias localhost \
  -keyalg RSA -keysize 2048 \
  -keystore "${WORKSPACE}/${KEYSTORE_DIR}/localhost.keystore.p12" \
  -keypass password \
  -storepass password \
  -storetype PKCS12 \
  -dname "CN=Zowe Service, OU=API Mediation Layer, O=Zowe Sample, L=Prague, S=Prague, C=CZ" \
  -validity 3650
echo "[${SCRIPT_NAME}] - generate CSR"
keytool -certreq -v \
  -alias localhost \
  -keystore "${WORKSPACE}/${KEYSTORE_DIR}/localhost.keystore.p12" \
  -storepass password \
  -file "${WORKSPACE}/${KEYSTORE_DIR}/localhost.keystore.csr" \
  -keyalg RSA -storetype PKCS12 \
  -dname "CN=Zowe Service, OU=API Mediation Layer, O=Zowe Sample, L=Prague, S=Prague, C=CZ" \
  -validity 3650
echo "[${SCRIPT_NAME}] - sign CSR"
keytool -gencert -v \
  -infile "${WORKSPACE}/${KEYSTORE_DIR}/localhost.keystore.csr" \
  -outfile "${WORKSPACE}/${KEYSTORE_DIR}/localhost.keystore_signed.cer" \
  -keystore "${WORKSPACE}/${KEYSTORE_DIR}/localca.keystore.p12" \
  -alias localca \
  -keypass local_ca_password \
  -storepass local_ca_password \
  -storetype PKCS12 \
  -ext "SAN=dns:localhost,dns:gateway-service,dns:discovery-service" \
  -ext "KeyUsage:critical=keyEncipherment,digitalSignature,nonRepudiation,dataEncipherment" \
  -ext "ExtendedKeyUsage=clientAuth,serverAuth" \
  -rfc \
  -validity 3650
echo "[${SCRIPT_NAME}] - import CA to server keystore"
keytool -importcert -v \
  -trustcacerts -noprompt \
  -file "${WORKSPACE}/${KEYSTORE_DIR}/localca.cer" \
  -alias localca \
  -keystore "${WORKSPACE}/${KEYSTORE_DIR}/localhost.keystore.p12" \
  -storepass password \
  -storetype PKCS12
echo "[${SCRIPT_NAME}] - import signed CSR to server keystore"
keytool -importcert -v \
  -trustcacerts -noprompt \
  -file "${WORKSPACE}/${KEYSTORE_DIR}/localhost.keystore_signed.cer" \
  -alias localhost \
  -keystore "${WORKSPACE}/${KEYSTORE_DIR}/localhost.keystore.p12" \
  -storepass password \
  -storetype PKCS12
echo "[${SCRIPT_NAME}] - import CA as truststore"
keytool -importcert -v \
  -trustcacerts -noprompt \
  -file "${WORKSPACE}/${KEYSTORE_DIR}/localca.cer" \
  -alias localca \
  -keystore "${WORKSPACE}/${KEYSTORE_DIR}/localhost.truststore.p12" \
  -storepass password \
  -storetype PKCS12
echo "[${SCRIPT_NAME}] - certificates prepared:"
ls -l "${WORKSPACE}/${KEYSTORE_DIR}"
echo
###################################################################
################## PREPARE STATIC REGISTRATION ####################
echo "[${SCRIPT_NAME}] create static registration files"
cd "${ROOT_DIR}"
sed -e "s|{ZOSMF_HOST}|${ZOSMF_HOST}|g" \
  -e "s|{ZOSMF_PORT}|${ZOSMF_PORT}|g" \
  "scripts/containerized-apiml/zosmf.yml.template" > "${WORKSPACE}/${API_DEFS_DIR}/zosmf.yml"
sed -e "s|{FILES_HOST}|${FILES_HOST}|g" \
  -e "s|{FILES_PORT}|${FILES_PORT}|g" \
  "scripts/containerized-apiml/files-api.yml.template" > "${WORKSPACE}/${API_DEFS_DIR}/files-api.yml"
echo "[${SCRIPT_NAME}] - static registration files prepared:"
ls -l "${WORKSPACE}/${API_DEFS_DIR}"
echo
###################################################################
#################### START APIML CONTAINERS #######################
echo "[${SCRIPT_NAME}] create docker-compose file and run"
echo "[${SCRIPT_NAME}] creating docker-compose file for '$HOST_OS' operating system using template $dockerComposeTemplate"
cd "${ROOT_DIR}"
sed -e "s|{WORKSPACE}|${WORKSPACE}|g" \
  -e "s|{DISCOVERY_PORT}|${DISCOVERY_PORT}|g" \
  -e "s|{GATEWAY_PORT}|${GATEWAY_PORT}|g" \
  "scripts/containerized-apiml/$dockerComposeTemplate" > "${WORKSPACE}/docker-compose.yml"
docker-compose -f "${WORKSPACE}/docker-compose.yml" up -d
###################################################################
echo "[${SCRIPT_NAME}] done."
exit 0