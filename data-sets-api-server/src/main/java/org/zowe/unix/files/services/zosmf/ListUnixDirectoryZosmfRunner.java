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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.api.common.zosmf.services.AbstractZosmfRequestRunner;
import org.zowe.unix.files.exceptions.PathNameNotValidException;
import org.zowe.unix.files.exceptions.UnauthorisedDirectoryException;
import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren;
import org.zowe.unix.files.model.UnixDirectoryChild;
import org.zowe.unix.files.model.UnixEntityType;
import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren.UnixDirectoryAttributesWithChildrenBuilder;
import org.zowe.unix.files.model.UnixDirectoryChild.UnixDirectoryChildBuilder;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ListUnixDirectoryZosmfRunner extends AbstractZosmfRequestRunner<UnixDirectoryAttributesWithChildren> {

    @Autowired
    ZosmfConnector zosmfConnector;
    
    private String path;

    public ListUnixDirectoryZosmfRunner(String path) {
        this.path = path;
    }

    @Override
    protected RequestBuilder prepareQuery(ZosmfConnector zosmfConnector) throws URISyntaxException {
        String query = String.format("path=%s", path);
        URI requestUrl = zosmfConnector.getFullUrl("restfiles/fs", query);
        RequestBuilder requestBuilder = RequestBuilder.get(requestUrl);
        return requestBuilder;
    }

    @Override
    protected int[] getSuccessStatus() {
        return new int[] { HttpStatus.SC_OK };
    }

    @Override
    protected UnixDirectoryAttributesWithChildren getResult(ResponseCache responseCache) throws IOException {
        JsonObject directoryListResponse = responseCache.getEntityAsJsonObject();
        JsonElement directoryListArray = directoryListResponse.get("items");
        List<UnixDirectoryChild> directoryChildren = getChildrenFromJsonArray(directoryListArray.getAsJsonArray());
        
        JsonObject directoryObject = directoryListArray.getAsJsonArray().get(0).getAsJsonObject();
        UnixDirectoryAttributesWithChildrenBuilder builder = UnixDirectoryAttributesWithChildren.builder()
                .owner(getStringOrNull(directoryObject, "user"))
                .group(getStringOrNull(directoryObject, "group"))
                .type(getEntityTypeFromSymbolicPermissions(getStringOrNull(directoryObject, "mode")))
                .permissionsSymbolic(getStringOrNull(directoryObject, "mode"))
                .size(getIntegerOrNull(directoryObject, "size"))
                .lastModified(getStringOrNull(directoryObject, "mtime"))
                .children(directoryChildren);
        
        return builder.build();
    }
    
    private List<UnixDirectoryChild> getChildrenFromJsonArray(JsonArray directoryListArray) {
        List<UnixDirectoryChild> directoryChildren = new ArrayList<UnixDirectoryChild>();
        
        for (JsonElement jsonElement : directoryListArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            // Skip self and parent
            if (!getStringOrNull(jsonObject, "name").equals(".") && !getStringOrNull(jsonObject, "name").equals("..")) {
                UnixDirectoryChildBuilder builder = UnixDirectoryChild.builder().name(getStringOrNull(jsonObject, "name"))
                        .type(getEntityTypeFromSymbolicPermissions(getStringOrNull(jsonObject, "mode")))
                        .link(constructLinkString(getStringOrNull(jsonObject, "name")));
                directoryChildren.add(builder.build());
            }
        }
        return directoryChildren;
    }
    
    private UnixEntityType getEntityTypeFromSymbolicPermissions(String permissions) {
        if (permissions.startsWith("d")) {
            return UnixEntityType.DIRECTORY;
        }
        return UnixEntityType.FILE;
        
    }

    private String constructLinkString(String fileName) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String requestURL = request.getRequestURL().toString();
        if (requestURL.charAt(requestURL.length() - 1) == '/') {
            requestURL = requestURL.substring(0, requestURL.length() - 1);
        }
        return String.format("%s%s/%s", requestURL, this.path, fileName);
    }
    
    @Override
    protected ZoweApiRestException createException(JsonObject jsonResponse, int statusCode) {
        JsonElement details = jsonResponse.get("details");
        JsonElement message = jsonResponse.get("message");
        if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            if (null != details && details.toString().contains("EDC5111I Permission denied. (errno2=0xEF076015)")) {
                throw new UnauthorisedDirectoryException(path);
            } else if (message.toString().contains("Path name is not valid")) {
                throw new PathNameNotValidException(path);
            }
        }
        return null;
    }
}
