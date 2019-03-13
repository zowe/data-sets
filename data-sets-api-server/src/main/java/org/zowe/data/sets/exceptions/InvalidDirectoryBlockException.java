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

public class InvalidDirectoryBlockException extends ZoweApiRestException {

    /**
     * 
     */
    private static final long serialVersionUID = -6481891748477944651L;

    public InvalidDirectoryBlockException(String dataSet) {
        super(HttpStatus.BAD_REQUEST,
                "The create request of data set ''{0}'' failed. A sequential data set can not have a directory block value not equal to 0",
                dataSet);
    }

}