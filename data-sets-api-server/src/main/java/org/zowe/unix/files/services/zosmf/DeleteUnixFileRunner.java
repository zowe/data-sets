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

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.unix.files.exceptions.FileNotFoundException;
import org.zowe.unix.files.exceptions.NotAEmptyDirectoryException;
import org.zowe.unix.files.exceptions.PermissionDeniedFileException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class DeleteUnixFileRunner extends AbstractZosmfUnixFilesRequestRunner<Void> {
    
    @Autowired
    ZosmfConnector zosmfConnector;

    private String path;
    private boolean isRecursive;
    
    public DeleteUnixFileRunner(String path) {
        this(path, false);
    }
    
    public DeleteUnixFileRunner(String path, boolean isRecursive) {
        this.path = path;
        this.isRecursive = isRecursive;
    }

    @Override
    protected int[] getSuccessStatus() {
        return new int[] { HttpStatus.SC_NO_CONTENT };
    }

    @Override
    protected RequestBuilder prepareQuery(ZosmfConnector zosmfConnector) throws URISyntaxException, IOException {
        URI requestUrl = zosmfConnector.getFullUrl("restfiles/fs" + path);
        RequestBuilder requestBuilder = RequestBuilder.delete(requestUrl);
        
        if (this.isRecursive) {
            requestBuilder.addHeader("X-IBM-Option", "recursive");
        }
        
        return requestBuilder;
    }

    @Override
    protected Void getResult(ResponseCache responseCache) throws IOException {
        return null;
    }
    
    @Override
    protected ZoweApiRestException createException(JsonObject jsonResponse, int statusCode) {
        JsonElement details = jsonResponse.get("details");
        if (null != details) {
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                if (details.toString().contains("EDC5129I No such file or directory.")) {
                    throw new FileNotFoundException(path);
                }
            }
            if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                if (details.toString().contains("EDC5111I Permission denied.")) {
                    throw new PermissionDeniedFileException(path);
                } else if (details.toString().contains("EDC5136I Directory not empty.")) {
                    throw new NotAEmptyDirectoryException(path);
                } else {
                   
                }
            }
        }
        return null;
    }

}
