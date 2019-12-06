#!/bin/sh

################################################################################
# This program and the accompanying materials are made available under the terms of the
# Eclipse Public License v2.0 which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#
# Copyright IBM Corporation 2019
################################################################################

INITIAL_ERRORS_FOUND=$ERRORS_FOUND

error() {
  . ${ROOT_DIR}/scripts/utils/error.sh $1
}

. ${ROOT_DIR}/scripts/utils/validate-zowe-prefix.sh 

# - FILES_API_PORT - should not be bound to a port currently
. ${ROOT_DIR}/scripts/utils/validate-port-available.sh $FILES_API_PORT

# Mediation stuff, should validate in a separate script
. ${ROOT_DIR}/scripts/utils/validate-apiml-variables.sh 

# - ZOSMF_PORT - The SSL port z/OSMF is listening on.
# - ZOSMF_HOST - The hostname, or ip address z/OSMF can be reached on
. ${ROOT_DIR}/scripts/utils/validate-zosmf-host-and-port.sh

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

. ${ROOT_DIR}/scripts/utils/validate-java.sh

return $(($ERRORS_FOUND-$INITIAL_ERRORS_FOUND))