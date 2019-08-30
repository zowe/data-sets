################################################################################
# This program and the accompanying materials are made available under the terms of the
# Eclipse Public License v2.0 which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#
# Copyright IBM Corporation 2019
################################################################################

FILES_API_ERRORS_FOUND=0

error() {
  echo "Error $FILES_API_ERRORS_FOUND: $1"
  let "FILES_API_ERRORS_FOUND=$FILES_API_ERRORS_FOUND+1"
}

# Configure and start require:
# - ZOWE_PREFIX - should be <=5 char long and exist
#TODO - any lower bound (other than 0)?
PREFIX_LENGTH=${#ZOWE_PREFIX}
if [[ -z $ZOWE_PREFIX ]]
then
  error "ZOWE_PREFIX is not set"
elif [[ $PREFIX_LENGTH > 5 ]]
then
  error "ZOWE_PREFIX '$ZOWE_PREFIX' should be less than 6 characters"
fi

# - FILES_API_PORT - should not be bound to a port currently
MATCHES=`onetstat -a | grep -c $FILES_API_PORT`
if [[ $MATCHES > 0 ]]
then
    error "Port $FILES_API_PORT is already in use by process `netstat -a -P $FILES_API_PORT | grep Listen`"
fi

# Mediation stuff, should validate in a separate script
# - KEYSTORE - The keystore to use for SSL certificates
# - KEYSTORE_PASSWORD - The password to access the keystore supplied by KEYSTORE
# - KEY_ALIAS - The alias of the key within the keystore
if [[ -z "${KEYSTORE}" ]]
then 
    error "KEYSTORE is empty"
fi
if [[ -z "${KEYSTORE_PASSWORD}" ]]
then 
    error "KEYSTORE_PASSWORD is empty"
fi
if [[ -z "${KEY_ALIAS}" ]]
then 
    error "KEY_ALIAS is empty"
fi

# - ZOSMF_PORT - The SSL port z/OSMF is listening on.
# - ZOSMF_IP_ADDRESS - The IP Address z/OSMF can be reached
# TODO - improve by creating in zowe-install-packaging in some sort of shared utils?
cat <<EOF >httpRequest.js
const https = require('https');

const args = process.argv.slice(2)
const zosmfIp=args[0];
const zosmfPort=args[1];

const options = {
  hostname: zosmfIp,
  port: zosmfPort,
  path: '/zosmf/info',
  method: 'GET',
  rejectUnauthorized: false
};

const req = https.request(options, (res) => {
  console.log(res.statusCode);
}).on("error", (err) => {
  console.log("Error: " + err.message);
});
req.end();
EOF

if [[ -z "${ZOSMF_IP_ADDRESS}" || -z "${ZOSMF_PORT}" ]]
then 
    error "ZOSMF_IP_ADDRESS and ZOSMF_PORT are not both set"
else
  if [ ! -z "$NODE_HOME" ];
  then
    NODE_BIN=${NODE_HOME}/bin/node
    RESPONSE_CODE=`$NODE_BIN httpRequest.js ${ZOSMF_IP_ADDRESS} ${ZOSMF_PORT}`
    if [[ -z "${RESPONSE_CODE}" ]]
    then
      echo "Warning: Could not validate if z/OS MF is available on 'https://${ZOSMF_IP_ADDRESS}:${ZOSMF_PORT}/zosmf/info'"
    else
      if [[ $RESPONSE_CODE != 200 ]]
      then
        error "Could not contact z/OS MF on 'https://${ZOSMF_IP_ADDRESS}:${ZOSMF_PORT}/zosmf/info' - $RESPONSE_CODE"
      fi
    fi
  else
    echo "Warning: Could not validate if z/OS MF is available on 'https://${ZOSMF_IP_ADDRESS}:${ZOSMF_PORT}/zosmf/info'"
  fi
fi

# - STATIC_DEF_CONFIG_DIR - Should exist and be writable
if [[ ! -d ${STATIC_DEF_CONFIG_DIR} ]]
then	
  error "Static definition config directory '${STATIC_DEF_CONFIG_DIR}' doesn't exist"
else 	
	if [[ ! -w ${STATIC_DEF_CONFIG_DIR} ]]
	then	
	  error "Static definition config directory '${STATIC_DEF_CONFIG_DIR}' does not have write access"	
	fi
fi

# Not sure how we validate - just exist ok? dig/oping?
#TODO - use oping, or the switcher in zowe-install-packaging utils?
# - ZOWE_EXPLORER_HOST
if [[ -n "${ZOWE_EXPLORER_HOST}" ]]
then 
    oping ${ZOWE_EXPLORER_HOST} > /dev/null    # check host
    if [[ $? -ne 0 ]]
    then    
        error "ZOWE_EXPLORER_HOST '$ZOWE_EXPLORER_HOST' does not point to a valid hostname"
    fi
else 
    error "ZOWE_EXPLORER_HOST is empty"
fi

# TODO move to a all zowe setup/validate?
# ZOWE_JAVA_HOME Should exist, be version 8+ and be on path
if [[ -n "${ZOWE_JAVA_HOME}" ]]
then 
    ls ${ZOWE_JAVA_HOME}/bin | grep java$ > /dev/null    # pick a file to check
    if [[ $? -ne 0 ]]
    then
        echo Error: ZOWE_JAVA_HOME does not point to a valid install of Java
    else
      JAVA_VERSION=`${ZOWE_JAVA_HOME}/bin/java -version 2>&1 | grep ^"java version"`
      if [[ "$JAVA_VERSION" < "java version \"1.8" ]]
      then 
        echo Error: $JAVA_VERSION is less than minimum level required of 1.8
      fi
    fi
else 
    echo Error: ZOWE_JAVA_HOME is empty
fi

return $FILES_API_ERRORS_FOUND