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

import org.apache.http.HttpStatus;
import org.zowe.api.common.test.AbstractHttpIntegrationTest;

public abstract class AbstractFilesIntegrationTest extends AbstractHttpIntegrationTest {
    protected static final String HEX_IN_QUOTES_REGEX = "^\"[0-9A-F]+\"$";

    public void testLogout(String path) {
        String cookie = RestAssured.given().when().get(path).getCookie("JSESSIONID");

        RestAssured.given().when().auth().none().cookie("JSESSIONID", cookie).get(path)
            .then().statusCode(HttpStatus.SC_OK);

        RestAssured.given().when().auth().none().cookie("JSESSIONID", cookie).get("/logout")
            .then().statusCode(HttpStatus.SC_NO_CONTENT);

        RestAssured.given().when().auth().none().cookie("JSESSIONID", cookie).get(path)
        .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }
}
