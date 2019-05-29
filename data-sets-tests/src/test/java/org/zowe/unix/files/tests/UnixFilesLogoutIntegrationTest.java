/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.unix.files.tests;

import io.restassured.RestAssured;

import org.apache.http.HttpStatus;
import org.junit.Test;

public class UnixFilesLogoutIntegrationTest extends AbstractUnixFilesIntegrationTest {
    final String path = "?path=" + TEST_DIRECTORY;

    @Test
    public void testGetDirectoryListingWithoutAuth() throws Exception {
        RestAssured.given().when().auth().none().get(path)
            .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void testLogoutWithGetDirectoryListing() throws Exception {
        String cookie = RestAssured.given().when().get(path).getCookie("JSESSIONID");

        RestAssured.given().when().auth().none().cookie("JSESSIONID", cookie).get(path)
            .then().statusCode(HttpStatus.SC_OK);

        RestAssured.given().when().auth().none().cookie("JSESSIONID", cookie).get("/logout")
            .then().statusCode(HttpStatus.SC_NO_CONTENT);

        RestAssured.given().when().auth().none().cookie("JSESSIONID", cookie).get(path)
        .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }
}