#!/bin/sh

################################################################################
# This program and the accompanying materials are made available under the terms of the
# Eclipse Public License v2.0 which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#
# Copyright IBM Corporation 2018, 2021
################################################################################

# Variables required on shell:
# - ZOWE_PREFIX
# - FILES_API_PORT - The port the data sets server will use
# - KEY_ALIAS
# - KEYSTORE - The keystore to use for SSL certificates
# - KEYSTORE_TYPE - The keystore type to use for SSL certificates
# - KEYSTORE_PASSWORD - The password to access the keystore supplied by KEYSTORE
# - KEY_ALIAS - The alias of the key within the keystore
# - GATEWAY_PORT - The SSL port z/OSMF is listening on.
# - ZOWE_EXPLORER_HOST - The IP Address z/OSMF can be reached


if [ -z "${LAUNCH_COMPONENT}" ]; then
  # component should be started from component home directory
  LAUNCH_COMPONENT=$(pwd)/bin
fi

JAR_FILE=$(ls -1 ${LAUNCH_COMPONENT}/data-sets-api-server-*.jar | head -n 1)

options="-Xms16m -Xmx512m"
if [ `uname` = "OS/390" ]; then
  options="${options} -Xquickstart"
fi

COMPONENT_CODE=EF
_BPX_JOBNAME=${ZOWE_PREFIX}${COMPONENT_CODE} java \
  ${options} \
  -Dibm.serversocket.recover=true \
  -Dfile.encoding=UTF-8 \
  -Djava.io.tmpdir=${TMPDIR:-${TMP:-/tmp}} \
  -Dspring.profiles.include="${EXPLORER_API_LOG_LEVEL}" \
  -Dserver.port=${FILES_API_PORT:-8547} \
  -Dserver.ssl.keyAlias="${KEY_ALIAS}" \
  -Dserver.ssl.keyStore="${KEYSTORE}" \
  -Dserver.ssl.keyStorePassword="${KEYSTORE_PASSWORD}" \
  -Dserver.ssl.keyStoreType=${KEYSTORE_TYPE:-PKCS12} \
  -Dserver.connection-timeout=8000 \
  -Dcom.ibm.jsse2.overrideDefaultTLS=true \
  -Dconnection.httpsPort=${GATEWAY_PORT} \
  -Dconnection.ipAddress=${GATEWAY_HOST:-${ZOWE_EXPLORER_HOST:-localhost}} \
  -Dspring.main.banner-mode=off \
  -Djava.protocol.handler.pkgs=com.ibm.crypto.provider \
  -jar "${JAR_FILE}"
