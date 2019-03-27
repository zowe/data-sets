/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.unix.files.services.zosmf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.api.common.zosmf.services.AbstractZosmfRequestRunner;
import org.zowe.unix.files.exceptions.FileNotFoundException;
import org.zowe.unix.files.exceptions.PathNameNotValidException;
import org.zowe.unix.files.exceptions.UnauthorisedFileException;
import org.zowe.unix.files.model.UnixFileContent;
import org.zowe.unix.files.model.UnixFileContentWithETag;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class GetUnixFileContentRunner extends AbstractZosmfRequestRunner<UnixFileContentWithETag> {
    
    @Autowired
    ZosmfConnector zosmfConnector;

    private String path;
    
    public GetUnixFileContentRunner(String path) {
        this.path = path;
    }

    @Override
    protected int[] getSuccessStatus() {
        return new int[] { HttpStatus.SC_OK };
    }

    @Override
    protected RequestBuilder prepareQuery(ZosmfConnector zosmfConnector) throws URISyntaxException {
        URI requestUrl = zosmfConnector.getFullUrl("restfiles/fs" + path);
        RequestBuilder requestBuilder = RequestBuilder.get(requestUrl);
        return requestBuilder;
    }

    @Override
    protected UnixFileContentWithETag getResult(ResponseCache responseCache) throws IOException {
        UnixFileContent content = new UnixFileContent(responseCache.getEntity());
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
        if (null != details) {
            if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                if (details.toString().contains("EDC5135I Not a directory.")) {
                    throw new PathNameNotValidException(path);
                } else if (details.toString().contains("EDC5111I Permission denied.")) {
                    throw new UnauthorisedFileException(path);
                }
            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                if (details.toString().contains("EDC5129I No such file or directory.")) {
                    throw new FileNotFoundException(path);
                }
            }
        }
        return null;
    }

}
