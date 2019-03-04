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
import org.zowe.data.sets.model.DataSet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ListDataSetsZosmfRequestRunner
        extends AbstractZosmfDataSetsRequestRunner<ItemsWrapper<DataSet>> {

    private String filter;

    public ListDataSetsZosmfRequestRunner(String filter) {
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
        requestBuilder.addHeader("X-IBM-Attributes", "dsname");
        return requestBuilder;
    }

    @Override
    protected ItemsWrapper<DataSet> getResult(ResponseCache responseCache) throws IOException {
        JsonObject dataSetsResponse = responseCache.getEntityAsJsonObject();
        JsonElement dataSetJsonArray = dataSetsResponse.get("items");

        List<DataSet> dataSets = new ArrayList<>();
        for (JsonElement jsonElement : dataSetJsonArray.getAsJsonArray()) {
            try {
                DataSet dataSet = DataSetMapper.INSTANCE.zosToDataSetDTO(jsonElement.getAsJsonObject());
                dataSets.add(dataSet);
            } catch (IllegalArgumentException e) {
                log.error("listDataSets", e);
            }
        }
        return new ItemsWrapper<>(dataSets);
    }

    @Override
    protected ZoweApiRestException createException(JsonObject jsonResponse, int statusCode) {
        return createDataSetException(jsonResponse, statusCode, filter);
    }
}
