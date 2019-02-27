/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.data.sets.tests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.zowe.api.common.errors.ApiError;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.data.sets.exceptions.PathNameNotValidException;
import org.zowe.data.sets.model.UnixDirectoryAttributesWithChildren;
import org.zowe.data.sets.model.UnixDirectoryChild;
import org.zowe.data.sets.model.UnixEntityType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UnixFilesGetDirectoryListingIntegrationTest extends AbstractHttpIntegrationTest {

    static final String UNIX_FILES_ENDPOINT = "unixfiles";
    static final String HOME_DIRECTORY = "/u/" + USER;
    static final int DIRECTORY_SIZE = 8192;
    
    @BeforeClass
    public static void setUpEndpoint() throws Exception {
        RestAssured.basePath = UNIX_FILES_ENDPOINT;
    }

    //TODO:: If we implement POST methods for creating files dynamically create the testable entities
    @Test
    public void testGetDirectoryList() throws Exception {
        
        UnixDirectoryAttributesWithChildren response = RestAssured.given().when().get("?path=" + HOME_DIRECTORY)
                .then().statusCode(HttpStatus.SC_OK).extract()
                .body().as(UnixDirectoryAttributesWithChildren.class);
        
        //TODO:: Once we have the ability to create files/directories we can assert equal to an expected UnixDirectoryAttributesWithChidlren object
        assertEquals(response.getType(), UnixEntityType.DIRECTORY);
        assertFalse(response.getOwner().isEmpty());
        assertFalse(response.getGroup().isEmpty());
        assertFalse(response.getPermissionsSymbolic().isEmpty());
        assertTrue(response.getPermissionsSymbolic().startsWith("d"));
        assertEquals((int) response.getSize(), DIRECTORY_SIZE);
        //Time pattern: 2019-02-13T18:00:30
        assertTrue(response.getLastModified().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"));
        assertFalse(response.getChildren().isEmpty());
        for (UnixDirectoryChild child : response.getChildren()) {
            assertFalse(child.getName().isEmpty());
            assertTrue(child.getType() == UnixEntityType.DIRECTORY || child.getType() == UnixEntityType.FILE);
            assertEquals(child.getLink(), BASE_URL + UNIX_FILES_ENDPOINT + HOME_DIRECTORY + "/" + child.getName());
        }
    }
    
    //TODO:: Need ability to create new directories
    @Test
    @Ignore()
    public void testGetDirectoryListingWithNoDirectoryChildren() {}
    
    @Test
    public void testGetDirectoryListingWithInvlaidPath() {
        String invalidPath = "//";
        ZoweApiRestException expected = new PathNameNotValidException(invalidPath);
        ApiError expectedError = expected.getApiError();
        
        RestAssured.given().when().get("?path=" + invalidPath).then()
                .statusCode(HttpStatus.SC_BAD_REQUEST).contentType(ContentType.JSON)
                .body("message", equalTo(expectedError.getMessage()));
    }
    
    //TODO:: Need ability to create a directory without permission
    @Test
    @Ignore()
    public void testGetDirectoryListingWithoutPermission() {}
    
    
}
