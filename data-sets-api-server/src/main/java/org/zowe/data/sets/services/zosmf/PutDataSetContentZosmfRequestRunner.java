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
import org.springframework.util.StringUtils;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.exceptions.PreconditionFailedException;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.data.sets.model.DataSetContent;
import org.zowe.data.sets.model.DataSetContentWithEtag;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class PutDataSetContentZosmfRequestRunner extends AbstractZosmfDataSetsRequestRunner<String> {

    private String dataSetName;
    private DataSetContentWithEtag contentWithEtag;

    public PutDataSetContentZosmfRequestRunner(String dataSetName, DataSetContentWithEtag contentWithEtag) {
        this.dataSetName = dataSetName;
        this.contentWithEtag = contentWithEtag;
    }

    @Override
    protected RequestBuilder prepareQuery(ZosmfConnector zosmfConnector) throws URISyntaxException, IOException {
        String urlPath = String.format("restfiles/ds/%s", dataSetName);
        URI requestUrl = zosmfConnector.getFullUrl(urlPath); // $NON-NLS-1$
        DataSetContent content = contentWithEtag.getContent();
        StringEntity requestEntity = new StringEntity(content.getRecords());
        RequestBuilder requestBuilder = RequestBuilder.put(requestUrl).setEntity(requestEntity);
        requestBuilder.addHeader("Content-type", ContentType.TEXT_PLAIN.getMimeType());
        String ifMatch = contentWithEtag.getEtag();
        if (StringUtils.hasText(ifMatch)) {
            requestBuilder.addHeader("If-Match", ifMatch.replaceAll("\"", ""));// zosmf doesn't conform to spec where ifmatch is in double quotes
        }
        return requestBuilder;
    }

    @Override
    protected int[] getSuccessStatus() {
        return new int[] { HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT };
    }

    @Override
    protected String getResult(ResponseCache responseCache) throws IOException {
        return responseCache.getFirstHeader("ETag").getValue();
    }

    @Override
    protected ZoweApiRestException createGeneralException(ResponseCache responseCache, URI uri) throws IOException {
        // SJH - bit of a hack - could refactor = PutDataSetcontent currently the only error case without a json response
        if (responseCache.getStatus() == HttpStatus.SC_PRECONDITION_FAILED) {
            return new PreconditionFailedException(dataSetName);
        }
        return super.createGeneralException(responseCache, uri);
    }

    @Override
    protected ZoweApiRestException createException(JsonObject jsonResponse, int statusCode) {

        String zosmfMessage = jsonResponse.get("message").getAsString();
        if ("Data set not found.".equals(zosmfMessage)) {
            throw new DataSetNotFoundException(dataSetName);
        }
        return createDataSetException(jsonResponse, statusCode, dataSetName);
    }
}
