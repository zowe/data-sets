################################################################################
# This program and the accompanying materials are made available under the terms of the
# Eclipse Public License v2.0 which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#
# Copyright IBM Corporation 2019
################################################################################

# Configure and start require:
# - ZOWE_PREFIX - should be <=5 char long and exist
#TODO - any lower bound (other than 0)?
PREFIX_LENGTH=${#ZOWE_PREFIX}
if [[ $PREFIX_LENGTH = 0 ]]
then
  echo "Error: ZOWE_PREFIX is not set"
elif [[ $PREFIX_LENGTH > 5 ]]
then
  echo "Error: ZOWE_PREFIX '$ZOWE_PREFIX' should be less than 6 characters"
fi

# - FILES_API_PORT - should not be bound to a port currently
MATCHES=`onetstat -a | grep -c $FILES_API_PORT`
if [[ $MATCHES > 0 ]]
then
    echo "Error: Port $FILES_API_PORT is already in use by process `netstat -a -P $FILES_API_PORT | grep Listen`"
fi

# Mediation stuff, should validate in a separate script
# - KEYSTORE - The keystore to use for SSL certificates
# - KEYSTORE_PASSWORD - The password to access the keystore supplied by KEYSTORE
# - KEY_ALIAS - The alias of the key within the keystore

# zosmf - can we do equivalent of a curl - maybe create a small jar?
# - ZOSMF_PORT - The SSL port z/OSMF is listening on.
# - ZOSMF_IP_ADDRESS - The IP Address z/OSMF can be reached

# Should exist and be writable?
# - STATIC_DEF_CONFIG_DIR
if [[ -w ${STATIC_DEF_CONFIG_DIR} ]] 	
    then	
        echo "  Liberty "$4" found  at "$1$2$3	
        ZOWE_ZOSMF_PATH=$1$2$3	
        persist "ZOWE_ZOSMF_PATH" $1$2$3	
    else 	
# on some machines the user may not have permission to look into $3, in which case if $1$2 exists set the variable	
# as the symlink permission will be allowed by the IZUUSR user ID that starts the explorer-server	
        echo "  Unable to determine whether "$1$2$3$4 "exists"	
        echo "  This may be because the current user is not authorized to look into "$1$2	
        echo "  The runtime user for the liberty-server needs to have sufficient authority"	
        ZOWE_ZOSMF_PATH=$1$2$3	
        persist "ZOWE_ZOSMF_PATH" $1$2$3	
    fi

# Not sure how we validate - just exist ok? dig/oping?
# - ZOWE_EXPLORER_HOST

# TODO move to a all zowe setup/validate?
# ZOWE_JAVA_HOME Should exist, be version 8+ and be on path
if [[ -n "${ZOWE_JAVA_HOME}" ]]
then 
    echo OK: ZOWE_JAVA_HOME is not empty 
    ls ${ZOWE_JAVA_HOME}/bin | grep java$ > /dev/null    # pick a file to check
    if [[ $? -ne 0 ]]
    then
        echo Error: ZOWE_JAVA_HOME does not point to a valid install of Java
    fi
else 
    echo Error: ZOWE_JAVA_HOME is empty
fi

JAVA_VERSION=`${ZOWE_JAVA_HOME}/bin/java -version 2>&1 | grep ^"java version"`
if [[ "$JAVA_VERSION" < "java version \"1.8" ]]
then 
  echo Error: $JAVA_VERSION is less than minimum level required of 1.8
fi

#Make sure Java is available on the path
export JAVA_HOME=$ZOWE_JAVA_HOME
if [[ ":$PATH:" == *":$JAVA_HOME/bin:"* ]]; then
  echo "ZOWE_JAVA_HOME already exists on the PATH"
else
  echo "Appending ZOWE_JAVA_HOME/bin to the PATH..."
  export PATH=$PATH:$JAVA_HOME/bin
  echo "Done."
fi