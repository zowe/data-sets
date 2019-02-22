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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.data.sets.model.UnixFileAtributes;
import org.zowe.data.sets.model.UnixFileAtributes.UnixFileAtributesBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ListUnixDirectoryZosmfRunner extends AbstractZosmfDataSetsRequestRunner<List<UnixFileAtributes>> {

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
	protected List<UnixFileAtributes> getResult(ResponseCache responseCache) throws IOException {
		JsonObject directoryListResponse = responseCache.getEntityAsJsonObject();
        JsonElement directoryListArray = directoryListResponse.get("items");
        
        List<UnixFileAtributes> directoryListing = new ArrayList<UnixFileAtributes>();
        for(JsonElement jsonElement : directoryListArray.getAsJsonArray()) {
        	UnixFileAtributes fileAttributes = getFileFromJson(jsonElement.getAsJsonObject());
        	directoryListing.add(fileAttributes);
        }
        return directoryListing;
	}
	
	private UnixFileAtributes getFileFromJson(JsonObject jsonObject) {
		UnixFileAtributesBuilder builder = UnixFileAtributes.builder().name(getStringOrNull(jsonObject, "name"))
				.accessMode(getStringOrNull(jsonObject, "mode"))
				.size(getIntegerOrNull(jsonObject, "size"))
				.userId(getStringOrNull(jsonObject, "uid"))
				.user(getStringOrNull(jsonObject, "user"))
				.groupId(getStringOrNull(jsonObject, "gid"))
				.group(getStringOrNull(jsonObject, "group"))
				.lastModified(getStringOrNull(jsonObject, "mtime"));
		return builder.build();
	}
}
