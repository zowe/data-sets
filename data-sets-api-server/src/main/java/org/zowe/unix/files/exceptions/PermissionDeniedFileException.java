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

public class PermissionDeniedFileException extends ZoweApiRestException {
    
    /**
     * 
     */
    private static final long serialVersionUID = 8800178058552607094L;
    
    
    public PermissionDeniedFileException(String path) {
        super(HttpStatus.FORBIDDEN, "You dont have enough permission(s) to perform requested operation on ''{0}'' file", path);
    }

}
