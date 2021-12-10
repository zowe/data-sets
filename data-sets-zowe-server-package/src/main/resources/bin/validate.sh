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

print_formatted_debug "ZWELS" "files-api,bin/validate.sh:${LINENO}" "- Checking Files API port ${ZWE_configs_port}"
validate_this "is_port_available \"${ZWE_configs_port}\" 2>&1" "files-api,bin/validate.sh:${LINENO}"
validate_this "is_directory_writable \"${ZWE_STATIC_DEFINITIONS_DIR}\" 2>&1" "files-api,bin/validate.sh:${LINENO}"
