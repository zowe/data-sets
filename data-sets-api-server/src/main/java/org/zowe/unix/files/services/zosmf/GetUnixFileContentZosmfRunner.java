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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Base64Utils;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.unix.files.exceptions.NotAFileException;
import org.zowe.unix.files.model.UnixFileContent;
import org.zowe.unix.files.model.UnixFileContentWithETag;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class GetUnixFileContentZosmfRunner extends AbstractZosmfUnixFilesRequestRunner<UnixFileContentWithETag> {
    
    @Autowired
    ZosmfConnector zosmfConnector;

    private String path;
    private boolean convert;
    private boolean decode;
    
    public GetUnixFileContentZosmfRunner(String path, boolean convert, boolean decode) {
        this.path = path;
        this.convert = convert;
        this.decode = decode;
    }

    @Override
    protected int[] getSuccessStatus() {
        return new int[] { HttpStatus.SC_OK };
    }

    @Override
    protected RequestBuilder prepareQuery(ZosmfConnector zosmfConnector) throws URISyntaxException {
        URI requestUrl = zosmfConnector.getFullUrl("restfiles/fs" + path);
        RequestBuilder requestBuilder = RequestBuilder.get(requestUrl);
        if (convert) {
            requestBuilder.addHeader("X-IBM-Data-Type", "binary");
        }
        return requestBuilder;
    }

    private UnixFileContent getContent(ResponseCache responseCache) {
        String content = responseCache.getEntity();
        if (decode) {
            String decodedContent = new String(Base64Utils.decodeFromString(content));
            return new UnixFileContent(decodedContent);
        } 
        return new UnixFileContent(content);
    }

    @Override
    protected UnixFileContentWithETag getResult(ResponseCache responseCache) throws IOException {
        UnixFileContent content = getContent(responseCache);
        
        String eTag = null;
        Header eTagHeader = responseCache.getFirstHeader("ETag");
        if (eTagHeader != null) {
            eTag = eTagHeader.getValue();
        }

        return new UnixFileContentWithETag(content, eTag);
    }
    
    @Override
    protected ZoweApiRestException createException(JsonObject jsonResponse, int statusCode) {
        JsonElement details = jsonResponse.get("details");
        if (details.getAsString().contains("EDC5121I Invalid argument.")) {
            throw new NotAFileException(path);
        }
        return createUnixFileException(jsonResponse, statusCode, path);
    }

}