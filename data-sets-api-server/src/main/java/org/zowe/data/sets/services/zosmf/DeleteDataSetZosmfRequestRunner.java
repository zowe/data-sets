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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.ResponseCache;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class DeleteDataSetZosmfRequestRunner extends AbstractZosmfDataSetsRequestRunner<Void> {

    private String dataSetName;

    public DeleteDataSetZosmfRequestRunner(String dataSetName, List<Header> headers) {
        super(headers);
        this.dataSetName = dataSetName;
    }

    @Override
    protected RequestBuilder prepareQuery(ZosmfConnector zosmfConnector) throws URISyntaxException, IOException {
        URI requestUrl = zosmfConnector.getFullUrl(String.format("restfiles/ds/%s", dataSetName));
        return RequestBuilder.delete(requestUrl);
    }

    @Override
    protected int[] getSuccessStatus() {
        return new int[] { HttpStatus.SC_NO_CONTENT };
    }

    @Override
    protected Void getResult(ResponseCache responseCache) throws IOException {
        return null;
    }

    @Override
    protected ZoweApiRestException createException(JsonObject jsonResponse, int statusCode) {
        JsonElement details = jsonResponse.get("details");
        if (statusCode == HttpStatus.SC_NOT_FOUND) {
            if (details.toString().contains(
                    String.format("ISRZ002 Data set not cataloged - '%s' was not found in catalog.", dataSetName))) {
                throw new DataSetNotFoundException(dataSetName);
            }
        }
        return null;
    }
}
