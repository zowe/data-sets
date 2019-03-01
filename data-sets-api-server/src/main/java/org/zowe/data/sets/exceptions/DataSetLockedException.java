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

public class DataSetLockedException extends ZoweApiRestException {

    /**
     * 
     */
    private static final long serialVersionUID = 4979279899041608520L;

    public DataSetLockedException(String dataSet, String systemName, String jobName, String asID) {
        super(HttpStatus.FORBIDDEN,
                "The data set ''{0}'' is currently allocated to job ''{2}'' on system ''{1}'' (ASID {3}). Please try again later.",
                dataSet, systemName, jobName, asID);
    }

}