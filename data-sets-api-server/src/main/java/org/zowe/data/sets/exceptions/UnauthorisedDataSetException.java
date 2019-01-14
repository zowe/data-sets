/**
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */

package org.zowe.data.sets.exceptions;

import org.springframework.http.HttpStatus;
import org.zowe.api.common.exceptions.ZoweApiRestException;

public class UnauthorisedDataSetException extends ZoweApiRestException {

    public UnauthorisedDataSetException(String dataSet) {
        super(HttpStatus.FORBIDDEN, "You are not authorised to access data set ''{0}''", dataSet);
    }

}