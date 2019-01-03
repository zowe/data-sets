/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */
package org.zowe.data.sets.tests;

import io.restassured.RestAssured;

import org.junit.BeforeClass;

import static io.restassured.RestAssured.preemptive;

//TODO NOW - refactor with Jobs
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

}
