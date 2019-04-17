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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.test.ZoweApiTest;
import org.zowe.api.common.utils.JsonUtils;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.api.common.zosmf.services.AbstractZosmfRequestRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

//TODO MARK - speak to Mark about how to get this moved to common test https://github.com/zowe/explorer-api-common/issues/11
//TODO Class used by both org.zowe.data.sets and org.zowe.unix.files, when moving to api-common consider this
@RunWith(PowerMockRunner.class)
@PrepareForTest({ RequestBuilder.class, JsonUtils.class, ContentType.class, AbstractZosmfRequestRunner.class })
public abstract class AbstractZosmfRequestRunnerTest extends ZoweApiTest {

    static final String BASE_URL = "https://dummy.com/zosmf/";

    @Mock
    protected ZosmfConnector zosmfConnector;

    @Mock
    protected HttpResponse response;

    @Before
    public void setUp() throws Exception {
        when(zosmfConnector.getFullUrl(anyString())).thenAnswer(new org.mockito.stubbing.Answer<URI>() {
            @Override
            public URI answer(org.mockito.invocation.InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return new URI(BASE_URL + (String) args[0]);
            }
        });

        when(zosmfConnector.getFullUrl(anyString(), anyString())).thenAnswer(new org.mockito.stubbing.Answer<URI>() {
            @Override
            public URI answer(org.mockito.invocation.InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return new URI(BASE_URL + (String) args[0] + "?" + (String) args[1]);
            }
        });
    }

    protected void verifyInteractions(RequestBuilder requestBuilder) throws IOException, URISyntaxException {
        verifyInteractions(requestBuilder, false);
    }

    protected void verifyInteractions(RequestBuilder requestBuilder, boolean path) throws IOException, URISyntaxException {
        verify(zosmfConnector, times(1)).request(requestBuilder);
        if (path) {
            verify(zosmfConnector, times(1)).getFullUrl(anyString(), anyString());
        } else {
            verify(zosmfConnector, times(1)).getFullUrl(anyString());
        }
        verifyNoMoreInteractions(zosmfConnector);
    }

    // TODO - refactor common bits together
    protected RequestBuilder mockGetBuilder(String relativeUri) throws URISyntaxException {
        RequestBuilder builder = mock(RequestBuilder.class);
        mockStatic(RequestBuilder.class);
        URI uri = new URI(BASE_URL + relativeUri);
        when(builder.getUri()).thenReturn(uri);
        when(RequestBuilder.get(uri)).thenReturn(builder);
        return builder;
    }

    protected RequestBuilder mockDeleteBuilder(String relativeUri) throws URISyntaxException {
        RequestBuilder builder = mock(RequestBuilder.class);
        mockStatic(RequestBuilder.class);
        URI uri = new URI(BASE_URL + relativeUri);
        when(builder.getUri()).thenReturn(uri);
        when(RequestBuilder.delete(uri)).thenReturn(builder);
        return builder;
    }

    protected RequestBuilder mockPutBuilder(String relativeUri, String string) throws Exception {
        StringEntity stringEntity = mock(StringEntity.class);
        PowerMockito.whenNew(StringEntity.class).withArguments(string).thenReturn(stringEntity);
        return mockPutBuilder(relativeUri, stringEntity);
    }

    RequestBuilder mockPutBuilder(String relativeUri, JsonObject json) throws Exception {
        StringEntity stringEntity = mock(StringEntity.class);
        PowerMockito.whenNew(StringEntity.class).withArguments(json.toString(), ContentType.APPLICATION_JSON)
            .thenReturn(stringEntity);
        return mockPutBuilder(relativeUri, stringEntity);
    }

    private RequestBuilder mockPutBuilder(String relativeUri, StringEntity stringEntity) throws Exception {
        RequestBuilder builder = mock(RequestBuilder.class);
        mockStatic(RequestBuilder.class);
        URI uri = new URI(BASE_URL + relativeUri);
        when(builder.getUri()).thenReturn(uri);
        when(RequestBuilder.put(uri)).thenReturn(builder);
        when(builder.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")).thenReturn(builder);
        when(builder.setEntity(stringEntity)).thenReturn(builder);
        return builder;
    }

    RequestBuilder mockPostBuilder(String relativeUri, String string) throws Exception {
        StringEntity stringEntity = mock(StringEntity.class);
        PowerMockito.whenNew(StringEntity.class).withArguments(string).thenReturn(stringEntity);
        return mockPostBuilder(relativeUri, stringEntity);
    }

    RequestBuilder mockPostBuilder(String relativeUri, JsonObject json) throws Exception {
        StringEntity stringEntity = mock(StringEntity.class);
        PowerMockito.whenNew(StringEntity.class).withArguments(json.toString(), ContentType.APPLICATION_JSON)
            .thenReturn(stringEntity);

        return mockPostBuilder(relativeUri, stringEntity);
    }

    RequestBuilder mockPostBuilder(String relativeUri, StringEntity stringEntity) throws Exception {
        RequestBuilder builder = mock(RequestBuilder.class);

        mockStatic(RequestBuilder.class);
        URI uri = new URI(BASE_URL + relativeUri);
        when(RequestBuilder.post(uri)).thenReturn(builder);
        when(builder.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")).thenReturn(builder);
        when(builder.setEntity(stringEntity)).thenReturn(builder);
        when(builder.getUri()).thenReturn(uri);
        return builder;
    }

    protected ResponseCache mockJsonResponse(int statusCode, String jsonString) throws Exception {

        ResponseCache responseCache = mockResponseAndContentType(statusCode, jsonString, ContentType.APPLICATION_JSON);

        JsonElement json = new Gson().fromJson(jsonString, JsonElement.class);
        when(responseCache.getEntityAsJson()).thenReturn(json);

        if (json.isJsonArray()) {
            when(responseCache.getEntityAsJsonArray()).thenReturn(json.getAsJsonArray());
        } else if (json.isJsonObject()) {
            when(responseCache.getEntityAsJsonObject()).thenReturn(json.getAsJsonObject());
        }

        return responseCache;
    }

    protected ResponseCache mockTextResponse(int statusCode, String text) throws Exception {
        return mockResponseAndContentType(statusCode, text, ContentType.TEXT_PLAIN);
    }

    private ResponseCache mockResponseAndContentType(int statusCode, String entityString, ContentType contentType)
            throws Exception {

        ResponseCache responseCache = mockResponseCache(statusCode);
        when(responseCache.getEntity()).thenReturn(entityString);

        ContentType mockContentType = mock(ContentType.class);
        when(responseCache.getContentType()).thenReturn(contentType);
        when(mockContentType.getMimeType()).thenReturn(contentType.getMimeType());

        return responseCache;
    }

    protected ResponseCache mockResponseCache(int statusCode) throws Exception {
        ResponseCache responseCache = mock(ResponseCache.class);
        PowerMockito.whenNew(ResponseCache.class).withArguments(response).thenReturn(responseCache);
        when(responseCache.getStatus()).thenReturn(statusCode);
        return responseCache;
    }

    protected String loadTestFile(String relativePath) throws IOException {
        return loadFile("src/test/resources/zosmfResponses/" + relativePath);
    }

}
