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

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.data.sets.exceptions.UnauthorisedDataSetException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ListDataSetMembersZosmfRequestRunner extends AbstractZosmfDataSetsRequestRunner<List<String>> {

    private String dataSetName;

    public ListDataSetMembersZosmfRequestRunner(String dataSetName) {
        this.dataSetName = dataSetName;
    }

    @Override
    protected RequestBuilder prepareQuery(ZosmfConnector zosmfConnector) throws URISyntaxException, IOException {
        String urlPath = String.format("restfiles/ds/%s/member", dataSetName);
        URI requestUrl = zosmfConnector.getFullUrl(urlPath);
        return RequestBuilder.get(requestUrl);
    }

    @Override
    protected int[] getSuccessStatus() {
        return new int[] { HttpStatus.SC_OK };
    }

    @Override
    protected List<String> getResult(ResponseCache responseCache) throws IOException {
        List<String> memberNames = new ArrayList<>();
        JsonObject memberResponse = responseCache.getEntityAsJsonObject();
        JsonElement memberJsonArray = memberResponse.get("items");
        for (JsonElement jsonElement : memberJsonArray.getAsJsonArray()) {
            memberNames.add(jsonElement.getAsJsonObject().get("member").getAsString());
        }
        return memberNames;
    }

    @Override
    protected ZoweApiRestException createException(JsonObject jsonResponse, int statusCode) {
        JsonElement details = jsonResponse.get("details");
        if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            if (details.toString().contains(AUTHORIZATION_FAILURE)) {
                throw new UnauthorisedDataSetException(dataSetName);
            }
        } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
            if (details.toString().contains(DATA_SET_NOT_FOUND)) {
                throw new DataSetNotFoundException(dataSetName);
            }
        }
        return null;
    }
}
