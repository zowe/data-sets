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

import lombok.extern.slf4j.Slf4j;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.model.ItemsWrapper;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.data.sets.mapper.DataSetMapper;
import org.zowe.data.sets.model.DataSetAttributes;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ListDataSetsAttributesZosmfRequestRunner extends AbstractZosmfDataSetsRequestRunner<ItemsWrapper<DataSetAttributes>> {

    private String filter;

    public ListDataSetsAttributesZosmfRequestRunner(String filter) {
        this.filter = filter;
    }

    @Override
    protected int[] getSuccessStatus() {
        return new int[]{HttpStatus.SC_OK};
    }

    @Override
    protected RequestBuilder prepareQuery(ZosmfConnector zosmfConnector) throws URISyntaxException, IOException {
        String query = String.format("dslevel=%s", filter);
        URI requestUrl = zosmfConnector.getFullUrl("restfiles/ds", query); // $NON-NLS-1$
        RequestBuilder requestBuilder = RequestBuilder.get(requestUrl);
        requestBuilder.addHeader("X-IBM-Attributes", "base");
        return requestBuilder;
    }

    @Override
    protected ItemsWrapper<DataSetAttributes> getResult(ResponseCache responseCache) throws IOException {
        JsonObject dataSetsResponse = responseCache.getEntityAsJsonObject();
        JsonElement dataSetJsonArray = dataSetsResponse.get("items");

        List<DataSetAttributes> dataSets = new ArrayList<>();
        for (JsonElement jsonElement : dataSetJsonArray.getAsJsonArray()) {
            try {
                DataSetAttributes dataSet = DataSetMapper.INSTANCE.zosToDataSetAttributesDTO(jsonElement.getAsJsonObject());
                dataSets.add(dataSet);
            } catch (IllegalArgumentException e) {
                log.error("listDataSetAttributes", e);
            }
        }
        return new ItemsWrapper<>(dataSets);
    }

    @Override
    protected ZoweApiRestException createException(JsonObject jsonResponse, int statusCode) {
        return createDataSetException(jsonResponse, statusCode, filter);
    }
}
