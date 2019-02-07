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
import org.zowe.data.sets.exceptions.UnauthorisedDataSetException;
import org.zowe.data.sets.model.DataSetContent;
import org.zowe.data.sets.model.DataSetContentWithEtag;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class GetDataSetContentZosmfRequestRunner extends AbstractZosmfDataSetsRequestRunner<DataSetContentWithEtag> {

    private String dataSetName;

    public GetDataSetContentZosmfRequestRunner(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    @Override
    protected RequestBuilder prepareQuery(ZosmfConnector zosmfConnector) throws URISyntaxException, IOException {
        String urlPath = String.format("restfiles/ds/%s", dataSetName);
        URI requestUrl = zosmfConnector.getFullUrl(urlPath); // $NON-NLS-1$
        RequestBuilder requestBuilder = RequestBuilder.get(requestUrl);
        requestBuilder.addHeader("X-IBM-Return-Etag", "true");
        return requestBuilder;
    }

    @Override
    protected int[] getSuccessStatus() {
        return new int[] { HttpStatus.SC_OK };
    }

    @Override
    protected DataSetContentWithEtag getResult(ResponseCache responseCache) throws IOException {
        DataSetContent content = new DataSetContent(responseCache.getEntity());
        String eTag = null;
        Header etagHeader = responseCache.getFirstHeader("ETag");
        if (etagHeader != null) {
            eTag = etagHeader.getValue();
        }
        return new DataSetContentWithEtag(content, eTag);
    }

    @Override
    protected ZoweApiRestException createException(JsonObject jsonResponse, int statusCode) {
        String zosmfMessage = jsonResponse.get("message").getAsString();
        if ("Data set not found.".equals(zosmfMessage)) {
            throw new DataSetNotFoundException(dataSetName);
        } else if ("Member not found".equals(zosmfMessage)) {
            throw new DataSetNotFoundException(dataSetName);
        }

        // TODO NOW - refactor out auth failures?
        JsonElement details = jsonResponse.get("details");
        if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            if (details.toString().contains(AUTHORIZATION_FAILURE)) {
                throw new UnauthorisedDataSetException(dataSetName);
            }
        }
        return null;
    }
}
