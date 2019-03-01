/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */
package org.zowe.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.junit.BeforeClass;
import org.zowe.api.common.errors.ApiError;
import org.zowe.api.common.exceptions.ZoweApiRestException;

import static io.restassured.RestAssured.preemptive;
import static org.hamcrest.CoreMatchers.equalTo;

//TODO - refactor with Jobs https://github.com/zowe/explorer-api-common/issues/11
public abstract class AbstractHttpIntegrationTest {

    private final static String SERVER_HOST = System.getProperty("server.host");
    private final static String SERVER_PORT = System.getProperty("server.port");

    protected final static String BASE_URL = "https://" + SERVER_HOST + ":" + SERVER_PORT + "/api/v1/";

    protected final static String USER = System.getProperty("server.username");
    private final static String PASSWORD = System.getProperty("server.password");

    static final String JOB_IEFBR14 = "IEFBR14";
    static final String JOB_WITH_STEPS = "JOB1DD";

    @BeforeClass
    public static void setUpConnection() {
        RestAssured.port = Integer.valueOf(SERVER_PORT);
        RestAssured.baseURI = BASE_URL;
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.authentication = preemptive().basic(USER, PASSWORD);
    }

    protected void verifyExceptionReturn(ZoweApiRestException expected, Response response) {
        verifyExceptionReturn(expected.getApiError(), response);
    }

    protected void verifyExceptionReturn(ApiError expectedError, Response response) {
        response.then().statusCode(expectedError.getStatus().value()).contentType(ContentType.JSON)
            .body("status", equalTo(expectedError.getStatus().name()))
            .body("message", equalTo(expectedError.getMessage()));
    }
}
