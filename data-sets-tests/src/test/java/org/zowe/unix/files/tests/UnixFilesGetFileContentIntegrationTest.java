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
import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.hamcrest.text.MatchesPattern;
import org.junit.Test;
import org.zowe.api.common.errors.ApiError;
import org.zowe.unix.files.exceptions.FileNotFoundException;
import org.zowe.unix.files.exceptions.UnauthorisedFileException;

import static org.hamcrest.CoreMatchers.equalTo;

public class UnixFilesGetFileContentIntegrationTest extends AbstractUnixFilesIntegrationTest {
    
    @Test
    public void testGetUnixFileContent() throws Exception {
        final String expectedContent =  "Hello world\nhello world on new line.\n";
        
        RestAssured.given().when().get(TEST_DIRECTORY + "/fileWithAccess")
            .then().statusCode(HttpStatus.SC_OK)
            .header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX))
            .body("content", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(expectedContent + "\n"));
    }
    
    @Test
    public void testGetUnifFileContentUnauthorised() throws Exception {
        final String unauthorisedFile = TEST_DIRECTORY + "/fileWithoutAccess";
        ApiError expectedError = new UnauthorisedFileException(unauthorisedFile).getApiError();
        
        RestAssured.given().when().get(unauthorisedFile)
            .then().statusCode(HttpStatus.SC_FORBIDDEN)
            .body("message", equalTo(expectedError.getMessage()));
    }
    
    @Test
    public void testGetUnixFileFileNotFound() throws Exception {
        String invalidPath = "/u/zzzzzztxt";
        ApiError expectedError = new FileNotFoundException(invalidPath).getApiError();
        
        RestAssured.given().when().get(invalidPath)
            .then().statusCode(HttpStatus.SC_NOT_FOUND).contentType(ContentType.JSON)
            .body("message", equalTo(expectedError.getMessage()));
    }
    
}