/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019, 2020
 */
package org.zowe.unix.files.tests;

import io.restassured.RestAssured;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assume.assumeTrue;

public class UnixFilesLogoutIntegrationTest extends AbstractUnixFilesIntegrationTest {
    final String path = "?path=" + TEST_DIRECTORY;

    @Before
    public void checkVersionBeforeRunningTest() throws Exception {
        assumeTrue(System.getProperty("test.version").equals("1"));        
    }
    
    @Test
    public void testGetDirectoryListingWithoutAuth() throws Exception {
        RestAssured.given().header(AUTH_HEADER).when().auth().none().get(path)
            .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void testLogoutWithGetDirectoryListing() throws Exception {
        String cookie = RestAssured.given().header(AUTH_HEADER).when().get(path).getCookie("JSESSIONID");

        RestAssured.given().header(AUTH_HEADER).when().auth().none().cookie("JSESSIONID", cookie).get(path)
            .then().statusCode(HttpStatus.SC_OK);

        RestAssured.given().header(AUTH_HEADER).when().auth().none().cookie("JSESSIONID", cookie).get("/logout")
            .then().statusCode(HttpStatus.SC_NO_CONTENT);

        RestAssured.given().header(AUTH_HEADER).when().auth().none().cookie("JSESSIONID", cookie).get(path)
        .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }
}