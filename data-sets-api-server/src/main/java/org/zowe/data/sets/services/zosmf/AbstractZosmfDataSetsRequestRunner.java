/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.data.sets.services.zosmf;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.http.HttpStatus;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.zosmf.services.AbstractZosmfRequestRunner;
import org.zowe.data.sets.exceptions.DataSetLockedException;
import org.zowe.data.sets.exceptions.UnauthorisedDataSetException;

public abstract class AbstractZosmfDataSetsRequestRunner<T> extends AbstractZosmfRequestRunner<T> {

    private static final String AUTHORIZATION_FAILURE = "ISRZ002 Authorization failed";
    private static final String DATA_SET_NOT_FOUND = "ISRZ002 Data set not cataloged";

    ZoweApiRestException createDataSetException(JsonObject jsonResponse, int statusCode, String dataSetName) {
        JsonElement details = jsonResponse.get("details");
        if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            if (details.toString().contains(AUTHORIZATION_FAILURE)) {
                throw new UnauthorisedDataSetException(dataSetName);
            } else if (details.toString().contains("IEFA110I")) {
                // Extract the last line which has the details
                JsonArray array = details.getAsJsonArray();
                String[] dataLine = array.get(array.size() - 1).getAsString().split("\\s+");
                throw new DataSetLockedException(dataSetName, dataLine[0], dataLine[1], dataLine[2]);
            }
        } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
            if (details.toString().contains(DATA_SET_NOT_FOUND)) {
                throw new DataSetNotFoundException(dataSetName);
            }
        }
        return null;
    }

}
