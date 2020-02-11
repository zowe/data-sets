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
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.zowe.api.common.errors.ApiError;
import org.zowe.unix.files.exceptions.AlreadyExistsException;
import org.zowe.unix.files.exceptions.InvalidPermissionsException;
import org.zowe.unix.files.model.UnixEntityType;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

public class UnixFilesCreateAssetIntegrationTest extends AbstractUnixFilesIntegrationTest {
    
    @Test
    public void testCreateUnixFileUnspecifiedPermissions() throws Exception {
        String path = TEST_DIRECTORY + "/createdFileUnspecifiedPerms";
        unixAssetCreateTest(path, UnixEntityType.FILE);
        
        unixAssetCheckCreatedTest(path);
    }
    
    @Test
    public void testCreateUnixDirectoryUnspecifiedPermissions() throws Exception {
        String path = TEST_DIRECTORY + "/createdDirectoryUnspecifiedPerms";
        unixAssetCreateTest(path, UnixEntityType.DIRECTORY);
        
        unixAssetCheckCreatedTest(path);
    }
    
    @Test
    public void testCreateUnixFileSpecificPermissions() throws Exception {
        String path = TEST_DIRECTORY + "/createdFileSpecifiedPerms";
        unixAssetCreateTest(path, UnixEntityType.FILE, "rwxr--r--");
        
        unixAssetCheckCreatedTest(path, "rwxr--r--");
    }
    
    @Test
    public void testCreateUnixDirectorySpecificPermissions() throws Exception {
        String path = TEST_DIRECTORY + "/createdDirectorySpecifiedPerms";
        unixAssetCreateTest(path, UnixEntityType.DIRECTORY, "rwxr--r--");

        unixAssetCheckCreatedTest(path, "rwxr--r--");
    }

    private String constructRequestBody(UnixEntityType entityType, String permissions) {
        String requestBody = "{\"type\":\"" + entityType.toString() + "\"";
        if ( permissions != null) {
            requestBody += ", \"permissions\":\"" + permissions + "\"";
        }
        requestBody += "}";
        return requestBody;
    }
    
    private void unixAssetCreateTest(String path, UnixEntityType entityType) {
        unixAssetCreateTest(path, entityType, null);
    } 
    private void unixAssetCreateTest(String path, UnixEntityType entityType, String permissions) {
        String requestBody = constructRequestBody(entityType, permissions);
        
        RestAssured.given().header("Authorization", "Bearer " + AUTH_TOKEN).contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(requestBody).when().post(path)
            .then().statusCode(HttpStatus.SC_CREATED)
            .header("Location", BASE_URL + UNIX_FILES_ENDPOINT + path);
    }
    
    private void unixAssetCheckCreatedTest(String path) {
        unixAssetCheckCreatedTest(path, null);
    }
    private void unixAssetCheckCreatedTest(String path, String permissions) {
        Response response = RestAssured.given().header("Authorization", "Bearer " + AUTH_TOKEN)
                .when().get(BASE_URL + UNIX_FILES_ENDPOINT + "?path=" + path);
        response.then().statusCode(HttpStatus.SC_OK);
        if ( permissions != null) {
            JsonPath jsonPath = response.body().jsonPath();
            assertEquals(((String) jsonPath.get("permissionsSymbolic")).substring(1), permissions);
        }
    }
    
    @Test
    public void testCreateUnixFileAlreadyExistsError() throws Exception {
        String path = TEST_DIRECTORY + "/fileAlreadyExists";
        ApiError expectedError = new AlreadyExistsException(path).getApiError();
        
        createUnixAssetWithErrorTest(path, UnixEntityType.FILE, expectedError);
    }
    
    @Test
    public void testCreateUnixFileBadPermissionsError() throws Exception {
        String path = TEST_DIRECTORY + "/dummyFile";
        ApiError expectedError = new InvalidPermissionsException("123---123").getApiError();
        
        createUnixAssetWithErrorTest(path, UnixEntityType.FILE, expectedError, "123---123");
    }
    
    @Test
    public void testCreateUnixFileTooManyPermissionsError() throws Exception {
        String path = TEST_DIRECTORY + "/dummyFile";
        ApiError expectedError = new InvalidPermissionsException("rwxrwxrwxrwx").getApiError();
        
        createUnixAssetWithErrorTest(path, UnixEntityType.FILE, expectedError, "rwxrwxrwxrwx");
    }
    
    @Test
    public void testCreateUnixDirectoryBadPermissionsError() throws Exception {
        String path = TEST_DIRECTORY + "/dummyDir";
        ApiError expectedError = new InvalidPermissionsException("123---123").getApiError();
        
        createUnixAssetWithErrorTest(path, UnixEntityType.DIRECTORY, expectedError, "123---123");
    }
    
    private void createUnixAssetWithErrorTest(String path, UnixEntityType entityType, ApiError expectedError) {
        createUnixAssetWithErrorTest(path, entityType, expectedError, null);
    }
    private void createUnixAssetWithErrorTest(String path, UnixEntityType entityType, ApiError expectedError, String permissions) {
        String requestBody = constructRequestBody(entityType, permissions);
        
        RestAssured.given().header("Authorization", "Bearer " + AUTH_TOKEN).contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(requestBody).when().post(BASE_URL + UNIX_FILES_ENDPOINT + path)
            .then().statusCode(expectedError.getStatus().value())
            .body("message", equalTo(expectedError.getMessage()));
    }
}
