/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019, 2020
 */
package org.zowe.unix.files.services.zosmf;

import com.google.gson.JsonObject;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.util.StringUtils;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.exceptions.PreconditionFailedException;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.unix.files.model.UnixFileContent;
import org.zowe.unix.files.model.UnixFileContentWithETag;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class PutUnixFileContentZosmfRunner extends AbstractZosmfUnixFilesRequestRunner<String> {
    
    private String path;
    private UnixFileContentWithETag contentWithETag;
    private boolean convert;
    
    public PutUnixFileContentZosmfRunner(String path, UnixFileContentWithETag content, Boolean convert) {
        this.path = path;
        this.contentWithETag = content;
        this.convert = convert;
    }

    @Override
    protected RequestBuilder prepareQuery(ZosmfConnector zosmfConnector) throws URISyntaxException, IOException {
        URI requestUrl = zosmfConnector.getFullUrl("restfiles/fs" + path);
        UnixFileContent content = contentWithETag.getContent();
        StringEntity requestEntity = new StringEntity(content.getContent());
        RequestBuilder requestBuilder = RequestBuilder.put(requestUrl).setEntity(requestEntity);
        String ifMatch = contentWithETag.getETag();
        if (StringUtils.hasText(ifMatch)) {
            requestBuilder.addHeader("If-Match", ifMatch.replaceAll("\"", ""));
        }
        requestBuilder.addHeader("Content-type", ContentType.TEXT_PLAIN.getMimeType());
        if (convert) {
            requestBuilder.addHeader("X-IBM-Data-Type", "binary");
        }
        return requestBuilder;
    }
    
    @Override
    protected int[] getSuccessStatus() {
        return new int[] { HttpStatus.SC_NO_CONTENT };
    }

    @Override
    protected String getResult(ResponseCache responseCache) throws IOException {
        return responseCache.getFirstHeader("ETag").getValue();
    }
    
    @Override
    protected ZoweApiRestException createException(JsonObject jsonResponse, int statusCode) throws IOException {
        return createUnixFileException(jsonResponse, statusCode, path);
    }

    @Override
    protected ZoweApiRestException createGeneralException(ResponseCache responseCache, URI uri) throws IOException {
        if (responseCache.getStatus() == HttpStatus.SC_PRECONDITION_FAILED) {
            throw new PreconditionFailedException(path);
        }
        return super.createGeneralException(responseCache, uri);
    }

}
