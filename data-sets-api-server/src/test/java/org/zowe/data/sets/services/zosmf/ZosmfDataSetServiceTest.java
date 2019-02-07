/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */
package org.zowe.data.sets.services.zosmf;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.http.HttpEntity;
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
import org.zowe.api.common.utils.ResponseUtils;

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

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ResponseUtils.class, ZosmfDataSetService.class, RequestBuilder.class, JsonUtils.class,
        ContentType.class })
public class ZosmfDataSetServiceTest extends ZoweApiTest {

    private static final String BASE_URL = "https://dummy.com/zosmf/";

    @Mock
    ZosmfConnector zosmfConnector;

    ZosmfDataSetService dataService;

    // TODO - merge with jobs
    @Before
    public void setUp() throws Exception {
        dataService = new ZosmfDataSetService();
        dataService.zosmfConnector = zosmfConnector;
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

    // TODO - refactor with jobs
    private void verifyInteractions(RequestBuilder requestBuilder) throws IOException, URISyntaxException {
        verifyInteractions(requestBuilder, false);
    }

    // TODO - improve code - remove bool?
    private void verifyInteractions(RequestBuilder requestBuilder, boolean path)
            throws IOException, URISyntaxException {
        verify(zosmfConnector, times(1)).request(requestBuilder);
        if (path) {
            verify(zosmfConnector, times(1)).getFullUrl(anyString(), anyString());
        } else {
            verify(zosmfConnector, times(1)).getFullUrl(anyString());
        }
        verifyNoMoreInteractions(zosmfConnector);
    }

    private RequestBuilder mockGetBuilder(String relativeUri) throws URISyntaxException {
        RequestBuilder builder = mock(RequestBuilder.class);
        mockStatic(RequestBuilder.class);
        when(RequestBuilder.get(new URI(BASE_URL + relativeUri))).thenReturn(builder);
        return builder;
    }

    private RequestBuilder mockDeleteBuilder(String relativeUri) throws URISyntaxException {
        RequestBuilder builder = mock(RequestBuilder.class);
        mockStatic(RequestBuilder.class);
        when(RequestBuilder.delete(new URI(BASE_URL + relativeUri))).thenReturn(builder);
        return builder;
    }

    private RequestBuilder mockPutBuilder(String relativeUri, String string) throws Exception {
        StringEntity stringEntity = mock(StringEntity.class);
        PowerMockito.whenNew(StringEntity.class).withArguments(string).thenReturn(stringEntity);
        return mockPutBuilder(relativeUri, stringEntity);
    }

    private RequestBuilder mockPutBuilder(String relativeUri, JsonObject json) throws Exception {
        StringEntity stringEntity = mock(StringEntity.class);
        PowerMockito.whenNew(StringEntity.class).withArguments(json.toString(), ContentType.APPLICATION_JSON)
            .thenReturn(stringEntity);

        return mockPutBuilder(relativeUri, stringEntity);
    }

    private RequestBuilder mockPutBuilder(String relativeUri, StringEntity stringEntity) throws Exception {
        RequestBuilder builder = mock(RequestBuilder.class);

        mockStatic(RequestBuilder.class);
        when(RequestBuilder.put(new URI(BASE_URL + relativeUri))).thenReturn(builder);
        when(builder.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")).thenReturn(builder);
        when(builder.setEntity(stringEntity)).thenReturn(builder);
        return builder;
    }

    private RequestBuilder mockPostBuilder(String relativeUri, String string) throws Exception {
        StringEntity stringEntity = mock(StringEntity.class);
        PowerMockito.whenNew(StringEntity.class).withArguments(string).thenReturn(stringEntity);
        return mockPostBuilder(relativeUri, stringEntity);
    }

    private RequestBuilder mockPostBuilder(String relativeUri, JsonObject json) throws Exception {
        StringEntity stringEntity = mock(StringEntity.class);
        PowerMockito.whenNew(StringEntity.class).withArguments(json.toString(), ContentType.APPLICATION_JSON)
            .thenReturn(stringEntity);

        return mockPostBuilder(relativeUri, stringEntity);
    }

    private RequestBuilder mockPostBuilder(String relativeUri, StringEntity stringEntity) throws Exception {
        RequestBuilder builder = mock(RequestBuilder.class);

        mockStatic(RequestBuilder.class);
        when(RequestBuilder.post(new URI(BASE_URL + relativeUri))).thenReturn(builder);
        when(builder.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")).thenReturn(builder);
        when(builder.setEntity(stringEntity)).thenReturn(builder);
        return builder;
    }

    private HttpResponse mockJsonResponse(int statusCode, String jsonString) throws IOException {

        HttpEntity entity = new StringEntity(jsonString);
        HttpResponse response = mockResponse(statusCode);
        when(response.getEntity()).thenReturn(entity);

        JsonElement json = new Gson().fromJson(jsonString, JsonElement.class);
        when(ResponseUtils.getEntityAsJson(response)).thenReturn(json);

        ContentType contentType = mock(ContentType.class);
        mockStatic(ContentType.class);
        when(ContentType.get(entity)).thenReturn(contentType);
        when(contentType.getMimeType()).thenReturn(ContentType.APPLICATION_JSON.getMimeType());

        if (json.isJsonArray()) {
            when(ResponseUtils.getEntityAsJsonArray(response)).thenReturn(json.getAsJsonArray());
        } else if (json.isJsonObject()) {
            when(ResponseUtils.getEntityAsJsonObject(response)).thenReturn(json.getAsJsonObject());
        }

        return response;
    }

    private HttpResponse mockTextResponse(int statusCode, String text) throws IOException {

        HttpEntity entity = new StringEntity(text);
        HttpResponse response = mockResponse(statusCode);
        when(response.getEntity()).thenReturn(entity);

        when(ResponseUtils.getEntity(response)).thenReturn(text);

        ContentType contentType = mock(ContentType.class);
        mockStatic(ContentType.class);
        when(ContentType.get(entity)).thenReturn(contentType);
        when(contentType.getMimeType()).thenReturn(ContentType.TEXT_PLAIN.getMimeType());

        return response;
    }

    private HttpResponse mockResponse(int statusCode) throws IOException {
        HttpResponse response = mock(HttpResponse.class);
        mockStatic(ResponseUtils.class);
        when(ResponseUtils.getStatus(response)).thenReturn(statusCode);
        return response;
    }

    public String loadTestFile(String relativePath) throws IOException {
        return loadFile("src/test/resources/zosmfResponses/" + relativePath);
    }
}
