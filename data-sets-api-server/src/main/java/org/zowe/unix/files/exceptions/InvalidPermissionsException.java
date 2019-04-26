/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.unix.files.exceptions;

import org.springframework.http.HttpStatus;
import org.zowe.api.common.exceptions.ZoweApiRestException;

public class InvalidPermissionsException extends ZoweApiRestException {

    /**
     * 
     */
    private static final long serialVersionUID = 1532367270138368924L;

    public InvalidPermissionsException(String permissions) {
        super(HttpStatus.BAD_REQUEST, "Invalid permissions ''{0}''", permissions);
    }

}
