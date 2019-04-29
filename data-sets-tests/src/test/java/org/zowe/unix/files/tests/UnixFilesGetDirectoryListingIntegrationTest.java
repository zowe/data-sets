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
import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren;
import org.zowe.unix.files.model.UnixDirectoryChild;
import org.zowe.unix.files.model.UnixEntityType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class UnixFilesGetDirectoryListingIntegrationTest extends AbstractUnixFilesIntegrationTest {

    @Test
    public void testGetDirectoryList() throws Exception {
        final String testDirectoryPath = TEST_DIRECTORY + "/directoryWithAccess";
        final String fileWithAccess = "fileInDirectoryWithAccess";
        final String directoryWithAccess = "directoryInDirectoryWithAccess";
        
        UnixDirectoryChild file = UnixDirectoryChild
            .builder()
            .name(fileWithAccess)
            .type(UnixEntityType.FILE)
            .size(12)
            .link(BASE_URL + UNIX_FILES_ENDPOINT + testDirectoryPath + '/' + fileWithAccess)
            .build();
        UnixDirectoryChild directory = UnixDirectoryChild
            .builder()
            .name(directoryWithAccess)
            .type(UnixEntityType.DIRECTORY)
            .size(0)
            .link(BASE_URL + UNIX_FILES_ENDPOINT + testDirectoryPath + '/' + directoryWithAccess)
            .build();
        
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
        
        for (UnixDirectoryChild child : response.getChildren()) {
            assertTrue(child.getLastModified().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"));
            child.setLastModified(null);
        }
        assertThat(response.getChildren(), hasItems(file, directory));
    }
    
    @Test
    public void testGetDirectoryListingWithNoDirectoryChildren() {
        final String testDirectoryPath = TEST_DIRECTORY + "/directoryWithAccess/directoryInDirectoryWithAccess"; 
        
        UnixDirectoryAttributesWithChildren response = RestAssured.given().when().get("?path=" + testDirectoryPath)
                .then().statusCode(HttpStatus.SC_OK).extract()
                .body().as(UnixDirectoryAttributesWithChildren.class);
        
        assertEquals(response.getType(), UnixEntityType.DIRECTORY);
        assertTrue(response.getChildren().size() == 0);
    }

    @Test
    public void testGetDirectoryListingWithoutPermission() {
        final String testDirectoryPath = TEST_DIRECTORY + "/directoryWithoutAccess";
        ApiError expectedError = new UnauthorisedDirectoryException(testDirectoryPath).getApiError();
        
        RestAssured.given().when().get("?path=" + testDirectoryPath).then()
            .statusCode(HttpStatus.SC_FORBIDDEN).contentType(ContentType.JSON)
            .body("message", equalTo(expectedError.getMessage()));
    }
    
    @Test
    public void testGetDirectoryListingWithInvlaidPath() {
        String invalidPath = "//";
        ZoweApiRestException expected = new PathNameNotValidException(invalidPath);
        ApiError expectedError = expected.getApiError();
        
        RestAssured.given().when().get("?path=" + invalidPath).then()
                .statusCode(HttpStatus.SC_BAD_REQUEST).contentType(ContentType.JSON)
                .body("message", equalTo(expectedError.getMessage()));
    }
}