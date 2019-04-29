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

import lombok.AllArgsConstructor;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.unix.files.exceptions.NotAnEmptyDirectoryException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


@AllArgsConstructor
public class DeleteUnixFileZosmfRunner extends AbstractZosmfUnixFilesRequestRunner<Void> {

    private String path;
    private boolean isRecursive;

    public DeleteUnixFileZosmfRunner(String path) {
        this(path, false);
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
            if (details.toString().contains("EDC5136I Directory not empty.")) {
                throw new NotAnEmptyDirectoryException(path);
            } else {
                return createUnixFileException(jsonResponse, statusCode, path);
            }
        }
        return null;
    }

}
