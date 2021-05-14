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
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.unix.files.exceptions.PathNameNotValidException;
import org.zowe.unix.files.exceptions.UnauthorisedDirectoryException;
import org.zowe.unix.files.model.UnixDirectoryChild;
import org.zowe.unix.files.model.UnixEntityType;

import static org.hamcrest.CoreMatchers.equalTo;

public class UnixFilesGetDirectoryListingIntegrationTest extends AbstractUnixFilesIntegrationTest {
 
    @Test
    public void testGetDirectoryListingWithDirectoryChildren() throws Exception {
        final String directoryPath = TEST_DIRECTORY + "/directoryWithAccess";
        final String childFileName = "fileInDirectoryWithAccess";
        final String childDirectoryName = "directoryInDirectoryWithAccess";
        
        UnixDirectoryChild childFile = UnixDirectoryChild
            .builder()
            .name(childFileName)
            .type(UnixEntityType.FILE)
            .size(12)
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
    public void testGetDirectoryListingWithNoDirectoryChildren() throws Exception {
        final String directoryPath = TEST_DIRECTORY + "/directoryWithAccess/directoryInDirectoryWithAccess"; 
        
        testGetDirectory(directoryPath, new UnixDirectoryChild[0]);
    }

    @Test
    public void testGetDirectoryListingWithoutPermission() {
        final String testDirectoryPath = TEST_DIRECTORY + "/directoryWithoutAccess";
        ApiError expectedError = new UnauthorisedDirectoryException(testDirectoryPath).getApiError();
        
        RestAssured.given().header(AUTH_HEADER).when().get("?path=" + testDirectoryPath).then()
            .statusCode(HttpStatus.SC_FORBIDDEN).header("Content-Encoding", "gzip").contentType(ContentType.JSON)
            .body("message", equalTo(expectedError.getMessage()));
    }
    
    @Test
    public void testGetDirectoryListingWithInvlaidPath() {
        String invalidPath = "//";
        ZoweApiRestException expected = new PathNameNotValidException(invalidPath);
        ApiError expectedError = expected.getApiError();
        
        RestAssured.given().header(AUTH_HEADER).when().get("?path=" + invalidPath).then()
            .statusCode(HttpStatus.SC_BAD_REQUEST).header("Content-Encoding", "gzip").contentType(ContentType.JSON)
            .body("message", equalTo(expectedError.getMessage()));
    }
}