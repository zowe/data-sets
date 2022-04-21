#!/bin/sh

################################################################################
# This program and the accompanying materials are made available under the terms of the
# Eclipse Public License v2.0 which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#
# Copyright IBM Corporation 2021
################################################################################

# Variables required on shell:
# - JAVA_HOME
# - ZWE_zowe_certificate_keystore_alias - The default alias of the key within the keystore
# - ZWE_zowe_certificate_keystore_file - The default keystore to use for SSL certificates
# - ZWE_zowe_certificate_keystore_password - The default password to access the keystore supplied by KEYSTORE
# - ZWE_zowe_job_prefix

# Optional variables:
# - ZWE_components_gateway_port - the port the api gateway service will use
# - ZWE_configs_certificate_keystore_alias - The alias of the key within the keystore
# - ZWE_configs_certificate_keystore_file - The keystore to use for SSL certificates
# - ZWE_configs_certificate_keystore_password - The password to access the keystore supplied by KEYSTORE
# - ZWE_configs_certificate_keystore_type - The keystore type to use for SSL certificates
# - ZWE_configs_port - the port the api catalog service will use
# - ZWE_GATEWAY_HOST
# - ZWE_haInstance_hostname
# - ZWE_zowe_certificate_keystore_type - The default keystore type to use for SSL certificates

JAR_FILE=$(ls -1 $(pwd)/bin/data-sets-api-server-*.jar | head -n 1)

LOG_LEVEL=
if [[ ! -z ${ZWE_configs_debug} && ${ZWE_configs_debug} == true ]]
then
  LOG_LEVEL="debug"
fi

options="-Xms16m -Xmx512m"
if [ `uname` = "OS/390" ]; then
  options="${options} -Xquickstart"
fi

COMPONENT_CODE=EF
_BPX_JOBNAME=${ZWE_zowe_job_prefix}${COMPONENT_CODE} java \
  ${options} \
  -Dibm.serversocket.recover=true \
  -Dfile.encoding=UTF-8 \
  -Djava.io.tmpdir=${TMPDIR:-${TMP:-/tmp}} \
  -Dspring.profiles.include="${LOG_LEVEL}" \
  -Dserver.port=${ZWE_configs_port:-8547} \
  -Dserver.ssl.keyAlias="${ZWE_configs_certificate_keystore_alias:-${ZWE_zowe_certificate_keystore_alias}}" \
  -Dserver.ssl.keyStore="${ZWE_configs_certificate_keystore_file:-${ZWE_zowe_certificate_keystore_file}}" \
  -Dserver.ssl.keyStorePassword="${ZWE_configs_certificate_keystore_password:-${ZWE_zowe_certificate_keystore_password}}" \
  -Dserver.ssl.keyStoreType="${ZWE_configs_certificate_keystore_type:-${ZWE_zowe_certificate_keystore_type:-PKCS12}}" \
  -Dserver.connection-timeout=8000 \
  -Dcom.ibm.jsse2.overrideDefaultTLS=true \
  -Dconnection.httpsPort=${ZWE_components_gateway_port:-7554} \
  -Dconnection.ipAddress=${ZWE_GATEWAY_HOST:-${ZWE_haInstance_hostname:-localhost}} \
  -Dspring.main.banner-mode=off \
  -Djava.protocol.handler.pkgs=com.ibm.crypto.provider \
  -jar "${JAR_FILE}"
