/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2020
 */
package org.zowe.data.sets.services.zosmf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.data.sets.model.DataSetRenameRequest;
import org.zowe.data.sets.model.ZosmfRenameRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class PutDataSetRenameZosmfRequestRunner extends AbstractZosmfDataSetsRequestRunner<String> {
    private DataSetRenameRequest request;
    private String oldDataSetName;
    
    public PutDataSetRenameZosmfRequestRunner(String oldDataSetName, DataSetRenameRequest request) {
        this.oldDataSetName = oldDataSetName;
        this.request = request;
    }

    @Override
    protected RequestBuilder prepareQuery(ZosmfConnector zosmfConnector) throws URISyntaxException, IOException {
        String urlPath = String.format("restfiles/ds/%s", request.getNewName());
        URI requestUrl = zosmfConnector.getFullUrl(urlPath); // $NON-NLS-1$
        
        JsonObject requestBody = convertIntoZosmfRequestJson(oldDataSetName);      
        StringEntity requestEntity = new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON);

        return RequestBuilder.put(requestUrl).setEntity(requestEntity);
    }

    @Override
    protected int[] getSuccessStatus() {
        return new int[] { HttpStatus.SC_OK, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT };
    }

    @Override
    protected String getResult(ResponseCache responseCache) throws IOException {
        return responseCache.getEntity();
    }
 

    @Override
    protected ZoweApiRestException createException(JsonObject jsonResponse, int statusCode) {
        
        JsonElement details = jsonResponse.get("details");
        JsonElement stack = jsonResponse.get("stack");
        if (details != null) {
            if (details.getAsString().contains("not found")) {
                String errorMessage = details.getAsString();
                ApiError expectedError = ApiError.builder().message(errorMessage).status(org.springframework.http.HttpStatus.NOT_FOUND).build();
                throw new ZoweApiErrorException(expectedError);
            } else  {
                String errorMessage = details.getAsString();
                ApiError expectedError = ApiError.builder().message(errorMessage).status(org.springframework.http.HttpStatus.BAD_REQUEST).build();
                throw new ZoweApiErrorException(expectedError);
            }
        }
        else if (stack != null && stack.getAsString().contains("parse")) {
            String errorMessage =  String.format("Rename request of dataset %s failed because of invalid name %s", oldDataSetName, request.getNewName());
            ApiError expectedError = ApiError.builder().message(errorMessage).status(org.springframework.http.HttpStatus.BAD_REQUEST).build();
            throw new ZoweApiErrorException(expectedError);
        } 
        
        return createDataSetException(jsonResponse, statusCode, oldDataSetName);
    }
    
    private JsonObject convertIntoZosmfRequestJson(String name) {
        ZosmfRenameRequest renameRequest = ZosmfRenameRequest.createFromDataSetRenameRequest(name);
        return renameRequest.buildJson();
    }
    
}
