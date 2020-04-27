#!/bin/sh

################################################################################
# This program and the accompanying materials are made available under the terms of the
# Eclipse Public License v2.0 which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#
# Copyright IBM Corporation 2018, 2020
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

COMPONENT_CODE=EF
_BPX_JOBNAME=${ZOWE_PREFIX}${COMPONENT_CODE} java -Xms16m -Xmx512m -Dibm.serversocket.recover=true -Dfile.encoding=UTF-8 \
    -Djava.io.tmpdir=/tmp -Xquickstart \
    -Dserver.port=${FILES_API_PORT} \
    -Dcom.ibm.jsse2.overrideDefaultTLS=true \
    -Dserver.ssl.keyAlias=${KEY_ALIAS} \
    -Dserver.ssl.keyStore=${KEYSTORE} \
    -Dserver.ssl.keyStorePassword=${KEYSTORE_PASSWORD} \
    -Dserver.ssl.keyStoreType=${KEYSTORE_TYPE} \
    -Dserver.compression.enabled=true \
    -Dserver.connection-timeout=60000 \
    -Dconnection.httpsPort=${GATEWAY_PORT} \
    -Dconnection.ipAddress=${ZOWE_EXPLORER_HOST} \
    -Dspring.main.banner-mode=off \
    -jar {{jar_path}} &
