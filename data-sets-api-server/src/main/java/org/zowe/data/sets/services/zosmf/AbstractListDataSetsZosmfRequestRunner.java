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
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.ResponseCache;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class AbstractListDataSetsZosmfRequestRunner<T> extends AbstractZosmfDataSetsRequestRunner<T> {

    protected String filter;

    public AbstractListDataSetsZosmfRequestRunner(String filter) {
        this.filter = filter;
    }

    @Override
    protected RequestBuilder prepareQuery(ZosmfConnector zosmfConnector) throws URISyntaxException, IOException {
        String query = String.format("dslevel=%s", filter);
        URI requestUrl = zosmfConnector.getFullUrl("restfiles/ds", query); // $NON-NLS-1$
        RequestBuilder requestBuilder = RequestBuilder.get(requestUrl);
        addHeaders(requestBuilder);
        return requestBuilder;
    }

    protected abstract void addHeaders(RequestBuilder builder);

    @Override
    protected int[] getSuccessStatus() {
        return new int[]{HttpStatus.SC_OK};
    }

    @Override
    protected ZoweApiRestException createException(JsonObject jsonResponse, int statusCode) {
        return createDataSetException(jsonResponse, statusCode, filter);
    }

    @Override
    protected T getResult(ResponseCache responseCache) throws IOException {
        JsonObject response = responseCache.getEntityAsJsonObject();
        JsonElement items = response.get("items");
        return retrieveItems(items);
    }

    protected abstract T retrieveItems(JsonElement items);

}
