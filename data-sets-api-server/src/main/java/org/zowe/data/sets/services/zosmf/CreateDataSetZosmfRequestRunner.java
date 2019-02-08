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

import com.google.gson.JsonObject;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.JsonUtils;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.data.sets.exceptions.InvalidDirectoryBlockException;
import org.zowe.data.sets.model.DataSetCreateRequest;
import org.zowe.data.sets.model.DataSetOrganisationType;
import org.zowe.data.sets.model.ZosmfCreateRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class CreateDataSetZosmfRequestRunner extends AbstractZosmfDataSetsRequestRunner<String> {

    private DataSetCreateRequest request;
    private String dataSetName;

    public CreateDataSetZosmfRequestRunner(DataSetCreateRequest request) {
        this.request = request;
        dataSetName = request.getName();
    }

    @Override
    protected RequestBuilder prepareQuery(ZosmfConnector zosmfConnector) throws URISyntaxException, IOException {
        // TODO MAYBE - consider extracting to common validation mechanism
        if (request.getDataSetOrganization() == DataSetOrganisationType.PS && request.getDirectoryBlocks() != null
                && request.getDirectoryBlocks() != 0) {
            throw new InvalidDirectoryBlockException(dataSetName);
        }
        String urlPath = String.format("restfiles/ds/%s", dataSetName);
        URI requestUrl = zosmfConnector.getFullUrl(urlPath);
        JsonObject requestBody = convertIntoZosmfRequestJson(request);
        StringEntity requestEntity = new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON);
        RequestBuilder requestBuilder = RequestBuilder.post(requestUrl).setEntity(requestEntity);
        return requestBuilder;
    }

    @Override
    protected int[] getSuccessStatus() {
        return new int[] { HttpStatus.SC_CREATED };
    }

    @Override
    protected String getResult(ResponseCache responseCache) throws IOException {
        return dataSetName;
    }

    // TODO - work out how to decipher the dynamic allocation error codes to detect if data set already exists, then throw more specific exceptions
    @Override
    protected ZoweApiRestException createException(JsonObject jsonResponse, int statusCode) {
        return new ZoweApiRestException(org.springframework.http.HttpStatus.resolve(statusCode),
                jsonResponse.toString());
    }

    private JsonObject convertIntoZosmfRequestJson(DataSetCreateRequest input) throws IOException {
        ZosmfCreateRequest request = ZosmfCreateRequest.createFromDataSetCreateRequest(input);
        return JsonUtils.convertToJsonObject(request);
    }
}
