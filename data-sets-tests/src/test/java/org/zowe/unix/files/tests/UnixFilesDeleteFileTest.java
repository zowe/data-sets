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
import io.restassured.http.ContentType;

import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zowe.api.common.errors.ApiError;
import org.zowe.tests.AbstractHttpIntegrationTest;
import org.zowe.unix.files.exceptions.FileNotFoundException;
import org.zowe.unix.files.exceptions.PermissionDeniedFileException;

import static org.hamcrest.CoreMatchers.equalTo;

public class UnixFilesDeleteFileTest extends AbstractHttpIntegrationTest {

    static final String UNIX_FILES_ENDPOINT = "unixfiles";
	static final String TEST_DIRECTORY = System.getProperty("server.test.directory");
    
    @BeforeClass
    public static void setUpEndpoint() throws Exception {
        RestAssured.basePath = UNIX_FILES_ENDPOINT;
    }
    
    @Test
    public void testDeleteUnixFileContent() throws Exception {
    	final String fvtDeleteFile = TEST_DIRECTORY + "/deleteTestDirectoryAccess/deleteFileWithWritePermission";
        
        RestAssured.given().when().delete(fvtDeleteFile)
            .then().statusCode(HttpStatus.SC_NO_CONTENT);
    }
    
    @Test
    public void testDeleteUnixFilePermissionDenied() throws Exception {
        final String noPermissionDenied = TEST_DIRECTORY + "/deleteTestDirectoryWithoutAccess/deleteFileWithoutWritePermission";
        ApiError expectedError = new PermissionDeniedFileException(noPermissionDenied).getApiError();
        
        RestAssured.given().when().delete(noPermissionDenied)
            .then().statusCode(HttpStatus.SC_FORBIDDEN)
            .body("message", equalTo(expectedError.getMessage()));
    }
    
    @Test
    public void testDeleteUnixFileFileNotFound() throws Exception {
        String invalidPath = "/u/zzzzzztxt";
        ApiError expectedError = new FileNotFoundException(invalidPath).getApiError();
        
        RestAssured.given().when().delete(invalidPath)
            .then().statusCode(HttpStatus.SC_NOT_FOUND).contentType(ContentType.JSON)
            .body("message", equalTo(expectedError.getMessage()));
    }
    
    
}