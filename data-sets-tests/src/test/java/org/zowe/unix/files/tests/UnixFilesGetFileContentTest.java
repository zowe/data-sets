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
import org.junit.Ignore;
import org.junit.Test;
import org.zowe.api.common.errors.ApiError;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.tests.AbstractHttpIntegrationTest;
import org.zowe.unix.files.exceptions.FileNotFoundException;

import static org.hamcrest.CoreMatchers.equalTo;

public class UnixFilesGetFileContentTest extends AbstractHttpIntegrationTest {

    static final String UNIX_FILES_ENDPOINT = "unixfiles";
    
    //TODO:: How can we test without ability to first create a file
    @Test
    @Ignore
    public void testGetUnixFileContent() throws Exception {}
    
    @Test
    @Ignore
    public void testGetUnifFileContentNotAuthorised() throws Exception {}
    
    @Test
    public void testGetUnixFileFileNotFound() throws Exception {
        String invalidPath = "/u/zzzzzztxt";
        ZoweApiRestException expectedException = new FileNotFoundException(invalidPath);
        ApiError expectedError = expectedException.getApiError();
        
        RestAssured.given().when().get(UNIX_FILES_ENDPOINT + invalidPath).then()
            .statusCode(HttpStatus.SC_NOT_FOUND).contentType(ContentType.JSON)
            .body("message", equalTo(expectedError.getMessage()));
    }
    
}
