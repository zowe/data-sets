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
import org.zowe.unix.files.exceptions.NotAnEmptyDirectoryException;
import org.zowe.unix.files.exceptions.UnauthorisedFileException;
import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren;
import org.zowe.unix.files.model.UnixDirectoryChild;
import org.zowe.unix.files.model.UnixEntityType;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;




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

        RestAssured.given().when().delete(fvtDeleteFile).then().statusCode(HttpStatus.SC_NO_CONTENT);
        
        ApiError expectedError = new FileNotFoundException(fvtDeleteFile).getApiError();
        
        RestAssured.given().when().get(fvtDeleteFile)
            .then().statusCode(HttpStatus.SC_NOT_FOUND).contentType(ContentType.JSON)
            .body("message", equalTo(expectedError.getMessage()));
    }

    @Test
    public void testDeleteUnixFilePermissionDenied() throws Exception {
        final String noPermissionDenied = TEST_DIRECTORY
                + "/deleteTestDirectoryWithoutAccess/deleteFileWithoutWritePermission";
        ApiError expectedError = new UnauthorisedFileException(noPermissionDenied).getApiError();

        RestAssured.given().when().delete(noPermissionDenied).then().statusCode(HttpStatus.SC_FORBIDDEN).body("message",
                equalTo(expectedError.getMessage()));
        
        RestAssured.given().when().get(noPermissionDenied)
        .then().statusCode(HttpStatus.SC_OK).contentType(ContentType.JSON)
        .body("content", equalTo(""));
    }

    @Test
    public void testDeleteUnixFileFileNotFound() throws Exception {
        String invalidPath = "/u/zzzzzztxt";
        ApiError expectedError = new FileNotFoundException(invalidPath).getApiError();

        RestAssured.given().when().delete(invalidPath).then().statusCode(HttpStatus.SC_NOT_FOUND)
                .contentType(ContentType.JSON).body("message", equalTo(expectedError.getMessage()));
    }

    @Test
    public void testDeleteUnixNonEmptyDirectoryWithoutHeader() throws Exception {
        final String deleteNonEmptyDirectoryWithoutHeader = TEST_DIRECTORY + "/deleteTestDirectoryAccess/nestedDir";
        ApiError expectedError = new NotAnEmptyDirectoryException(deleteNonEmptyDirectoryWithoutHeader).getApiError();

        RestAssured.given().when().delete(deleteNonEmptyDirectoryWithoutHeader).then()
                .statusCode(HttpStatus.SC_BAD_REQUEST).contentType(ContentType.JSON)
                .body("message", equalTo(expectedError.getMessage()));
        
        testGetDirectoryList(deleteNonEmptyDirectoryWithoutHeader,"nestedFile","nestedDir2");
    }

    @Test
    public void testDeleteUnixNonEmptyDirectoryWithHeader() throws Exception {
        final String deleteNonEmptyDirectoryWithHeader = TEST_DIRECTORY + "/deleteTestDirectoryAccess/nestedDir";

        RestAssured.given().header("recursive", true).when().delete(deleteNonEmptyDirectoryWithHeader).then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
        
        ApiError expectedError = new FileNotFoundException(deleteNonEmptyDirectoryWithHeader).getApiError();
        RestAssured.given().when().get(deleteNonEmptyDirectoryWithHeader)
        .then().statusCode(HttpStatus.SC_NOT_FOUND).contentType(ContentType.JSON)
        .body("message", equalTo(expectedError.getMessage()));
        
    }
    
    public void testGetDirectoryList(String testDirectoryPath, String fileWithAccess, String directoryWithAccess) throws Exception {        
        UnixDirectoryChild file = UnixDirectoryChild.builder().name(fileWithAccess)
                .type(UnixEntityType.FILE).link(BASE_URL + UNIX_FILES_ENDPOINT + testDirectoryPath + '/' + fileWithAccess).build();
        UnixDirectoryChild directory = UnixDirectoryChild.builder().name(directoryWithAccess)
                .type(UnixEntityType.DIRECTORY).link(BASE_URL + UNIX_FILES_ENDPOINT + testDirectoryPath + '/' + directoryWithAccess).build();
        List<UnixDirectoryChild> children = new ArrayList<UnixDirectoryChild>();
        children.addAll(Arrays.asList(file, directory));
        
        
        UnixDirectoryAttributesWithChildren response = RestAssured.given().when().get("?path=" + testDirectoryPath)
                .then().statusCode(HttpStatus.SC_OK).extract()
                .body().as(UnixDirectoryAttributesWithChildren.class);
        
        assertFalse(response.getOwner().isEmpty());
        assertFalse(response.getGroup().isEmpty());
        assertFalse(response.getPermissionsSymbolic().isEmpty());
        assertTrue(response.getPermissionsSymbolic().startsWith("d"));
        assertTrue(response.getSize() == 8192);
        assertTrue(response.getLastModified().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"));
        assertEquals(response.getType(), UnixEntityType.DIRECTORY);
        assertTrue(children.containsAll(response.getChildren()));
    }

}