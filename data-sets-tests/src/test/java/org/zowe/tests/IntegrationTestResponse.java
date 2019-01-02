/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2015, 2018
 */
package org.zowe.tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.zowe.api.common.errors.ApiError;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.utils.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IntegrationTestResponse {

    private final HttpResponse response;

    public IntegrationTestResponse(HttpResponse response) {
        this.response = response;
    }

    public int getStatus() {
        return response.getStatusLine().getStatusCode();
    }

    public String getEntity() throws IOException {
        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }

    public List<String> getEntityAsListOfStrings() throws IOException {
        List<String> stringList = new ArrayList<String>();
        return getEntityAs(stringList.getClass());
    }

    public JsonObject getEntityAsJsonObject() throws IOException {
        JsonParser parser = new JsonParser();
        return parser.parse(getEntity()).getAsJsonObject();
    }

    public JsonArray getEntityAsJsonArray() throws IOException {
        JsonParser parser = new JsonParser();
        return parser.parse(getEntity()).getAsJsonArray();
    }

    public <T> T getEntityAs(Class<T> entityType) throws IOException {
        return JsonUtils.convertString(getEntity(), entityType);
    }

    public IntegrationTestResponse shouldHaveStatusOk() {
        return shouldHaveStatus(HttpStatus.SC_OK);
    }

    public IntegrationTestResponse shouldHaveStatusCreated() {
        return shouldHaveStatus(HttpStatus.SC_CREATED);
    }

    public IntegrationTestResponse shouldHaveStatusNoContent() {
        return shouldHaveStatus(HttpStatus.SC_NO_CONTENT);
    }

    public IntegrationTestResponse shouldHaveStatus(int expectedStatus) {
        assertEquals(expectedStatus, getStatus());
        return this;
    }

    public void shouldHaveLocationHeader(String expectedLocation) {
        assertEquals(expectedLocation, getLocationHeader());
    }

    public String getLocationHeader() {
        return response.getLastHeader("Location").getValue();
    }

    public IntegrationTestResponse shouldHaveEntityContaining(String expectedEntity) throws IOException {
        String entity = getEntity();
        assertTrue(String.format("%s contains %s", entity, expectedEntity), entity.contains(expectedEntity));
        return this;
    }

    public IntegrationTestResponse shouldHaveEntityMatching(String pattern) throws IOException {
        String entity = getEntity();
        assertTrue(String.format("%s matches %s", entity, pattern), entity.matches(pattern));
        return this;
    }

    public IntegrationTestResponse shouldHaveEntity(Object expected) throws Exception {
        Object entity = getEntityAs(expected.getClass());
        assertEquals(expected, entity);
        return this;
    }

    public void shouldReturnError(ApiError expected) throws Exception {
        shouldHaveStatus(expected.getStatus().value());
        shouldHaveEntity(expected);
    }

    public void shouldReturnException(ZoweApiRestException exception) throws Exception {
        shouldReturnError(exception.getApiError());
    }

}
