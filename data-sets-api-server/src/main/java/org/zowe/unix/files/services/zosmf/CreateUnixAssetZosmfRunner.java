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

import com.google.gson.JsonObject;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.unix.files.exceptions.AlreadyExistsException;
import org.zowe.unix.files.exceptions.InvalidPermissionsException;
import org.zowe.unix.files.model.UnixCreateAssetRequest;
import org.zowe.unix.files.model.UnixEntityType;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateUnixAssetZosmfRunner extends AbstractZosmfUnixFilesRequestRunner<String> {

    private UnixCreateAssetRequest request;
    private String path;
    
    public CreateUnixAssetZosmfRunner(String name, UnixCreateAssetRequest request, List<Header> headers) {
        super(headers);
        this.request = request;
        this.path = name;
    }

    @Override
    protected RequestBuilder prepareQuery(ZosmfConnector zosmfConnector) throws URISyntaxException, IOException {
        URI requestUrl = zosmfConnector.getFullUrl("restfiles/fs" + path);
        JsonObject requestBody = getZosmfRequestJson();
        StringEntity requestEntity = new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON);
        RequestBuilder requestBuilder = RequestBuilder.post(requestUrl).setEntity(requestEntity);
        return requestBuilder;
    }
    
    private JsonObject getZosmfRequestJson() {
        JsonObject zosmfRequestJson = new JsonObject();
        if (request.getType() == UnixEntityType.DIRECTORY) {
            zosmfRequestJson.addProperty("type", "directory");
        } else if (request.getType() == UnixEntityType.FILE) {
            zosmfRequestJson.addProperty("type", "file");
        }
        if (request.getPermissions() != null) {
            validatePermissionsString(request.getPermissions());
            zosmfRequestJson.addProperty("mode", request.getPermissions());
        }
        return zosmfRequestJson;
    }
    
    private void validatePermissionsString(String permissions) {
        String validPattern = "((r|R|-)(w|W|-)(x|X|-)){3}";
        Pattern pattern = Pattern.compile(validPattern);
        Matcher matcher = pattern.matcher(permissions);
        if (!matcher.matches() || permissions.length() > 9) {
            throw new InvalidPermissionsException(permissions);
        }
    }
    
    // TODO - z/OSMF doesn't provide any obvious way of deciphering permission denied or route not valid,
    // need to find a way of deciphering response codes to throw better exceptions
    @Override
    protected ZoweApiRestException createException(JsonObject jsonResponse, int statusCode) {
        String message = jsonResponse.get("message").getAsString();
        if (message.contains("already exists")) {
            throw new AlreadyExistsException(path);
        }
        if (message.contains("mode")) {
            String details = jsonResponse.get("details").getAsString();
            if (details.contains(request.getPermissions())) {
                throw new InvalidPermissionsException(request.getPermissions());
            }
        }
        return null;
    }
    
    @Override
    protected int[] getSuccessStatus() {
        return new int[] { HttpStatus.SC_CREATED };
    }

    @Override
    protected String getResult(ResponseCache responseCache) throws IOException {
        return path;
    }
    
}
