/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.data.sets.exceptions;

import org.springframework.http.HttpStatus;
import org.zowe.api.common.exceptions.ZoweApiRestException;

public class UnauthorisedDirectoryException extends ZoweApiRestException {

    /**
     * 
     */
    private static final long serialVersionUID = 5056221873442698156L;

    public UnauthorisedDirectoryException(String path) {
        super(HttpStatus.FORBIDDEN, "You are not authorised to access directory ''{0}''", path);
    }
}
