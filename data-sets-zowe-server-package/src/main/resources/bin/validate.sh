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

# Source main utils script
. ${ROOT_DIR}/bin/utils/utils.sh

validate_port_is_available ${FILES_API_PORT}
validate_zowe_prefix
validate_variables_are_set "KEYSTORE,KEYSTORE_PASSWORD,KEY_ALIAS,VERIFY_CERTIFICATES"
validate_directory_is_writable "${STATIC_DEF_CONFIG_DIR}"
validate_zosmf_host_and_port "${ZOSMF_HOST}" "${ZOSMF_PORT}"
validate_java_home

return $(($ERRORS_FOUND-$INITIAL_ERRORS_FOUND))