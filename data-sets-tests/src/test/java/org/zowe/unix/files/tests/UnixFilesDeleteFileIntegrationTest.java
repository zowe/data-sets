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
import org.junit.Test;
import org.zowe.api.common.errors.ApiError;
import org.zowe.unix.files.exceptions.FileNotFoundException;
import org.zowe.unix.files.exceptions.NotAnEmptyDirectoryException;
import org.zowe.unix.files.exceptions.UnauthorisedFileException;
import org.zowe.unix.files.model.UnixDirectoryChild;
import org.zowe.unix.files.model.UnixEntityType;

import static org.hamcrest.CoreMatchers.equalTo;

public class UnixFilesDeleteFileIntegrationTest extends AbstractUnixFilesIntegrationTest {

    @Test
    public void testDeleteUnixFileContent() throws Exception {
        final String fvtDeleteFile = TEST_DIRECTORY + "/deleteTestDirectoryAccess/deleteFileWithWritePermission";

        RestAssured.given().header(AUTH_HEADER).when().delete(fvtDeleteFile).then().statusCode(HttpStatus.SC_NO_CONTENT);
        
        ApiError expectedError = new FileNotFoundException(fvtDeleteFile).getApiError();
        
        RestAssured.given().header(AUTH_HEADER).when().get(fvtDeleteFile)
            .then().statusCode(HttpStatus.SC_NOT_FOUND).contentType(ContentType.JSON)
            .body("message", equalTo(expectedError.getMessage()));
    }

    @Test
    public void testDeleteUnixFilePermissionDenied() throws Exception {
        final String noPermissionDenied = TEST_DIRECTORY
                + "/deleteTestDirectoryWithoutAccess/deleteFileWithoutWritePermission";
        ApiError expectedError = new UnauthorisedFileException(noPermissionDenied).getApiError();

        RestAssured.given().header(AUTH_HEADER).when().delete(noPermissionDenied)
            .then().statusCode(HttpStatus.SC_FORBIDDEN).body("message", equalTo(expectedError.getMessage()));
        
        RestAssured.given().header(AUTH_HEADER).when().get(noPermissionDenied)
            .then().statusCode(HttpStatus.SC_OK).contentType(ContentType.JSON)
            .body("content", equalTo(""));
    }

    @Test
    public void testDeleteUnixFileFileNotFound() throws Exception {
        String invalidPath = "/u/zzzzzztxt";
        ApiError expectedError = new FileNotFoundException(invalidPath).getApiError();

        RestAssured.given().header(AUTH_HEADER).when().delete(invalidPath)
            .then().statusCode(HttpStatus.SC_NOT_FOUND).contentType(ContentType.JSON)
            .body("message", equalTo(expectedError.getMessage()));
    }

    @Test
    public void testDeleteUnixNonEmptyDirectoryWithoutHeader() throws Exception {
        final String directoryPath = TEST_DIRECTORY + "/deleteTestDirectoryAccess/nestedDir";
        final String childFileName = "nestedFile";
        final String childDirectoryName = "nestedDir2";

        ApiError expectedError = new NotAnEmptyDirectoryException(directoryPath).getApiError();

        RestAssured.given().header(AUTH_HEADER).when().delete(directoryPath).then()
            .statusCode(HttpStatus.SC_BAD_REQUEST).contentType(ContentType.JSON)
            .body("message", equalTo(expectedError.getMessage()));
        
        UnixDirectoryChild childFile = UnixDirectoryChild
            .builder()
            .name(childFileName)
            .type(UnixEntityType.FILE)
            .size(0)
            .link(BASE_URL + UNIX_FILES_ENDPOINT + directoryPath + '/' + childFileName)
            .build();
        
        UnixDirectoryChild childDirectory = UnixDirectoryChild
            .builder()
            .name(childDirectoryName)
            .type(UnixEntityType.DIRECTORY)
            .size(0)
            .link(BASE_URL + UNIX_FILES_ENDPOINT + directoryPath + '/' + childDirectoryName)
            .build();
        
        UnixDirectoryChild[] children = {childFile, childDirectory};
        
        testGetDirectory(directoryPath, children);
    }

    @Test
    public void testDeleteUnixNonEmptyDirectoryWithHeader() throws Exception {
        final String deleteNonEmptyDirectoryWithHeader = TEST_DIRECTORY + "/deleteTestDirectoryAccess/nestedDir";

        RestAssured.given().header("recursive", true).header(AUTH_HEADER).when().delete(deleteNonEmptyDirectoryWithHeader).then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        
        ApiError expectedError = new FileNotFoundException(deleteNonEmptyDirectoryWithHeader).getApiError();
        RestAssured.given().header(AUTH_HEADER).when().get(deleteNonEmptyDirectoryWithHeader)
            .then().statusCode(HttpStatus.SC_NOT_FOUND).contentType(ContentType.JSON)
            .body("message", equalTo(expectedError.getMessage()));
        
    }
}