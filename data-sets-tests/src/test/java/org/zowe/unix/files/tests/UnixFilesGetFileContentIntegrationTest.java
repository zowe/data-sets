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
import io.restassured.http.ContentType;

import org.apache.http.HttpStatus;
import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.hamcrest.text.MatchesPattern;
import org.junit.Test;
import org.zowe.api.common.errors.ApiError;
import org.zowe.unix.files.exceptions.FileNotFoundException;
import org.zowe.unix.files.exceptions.UnauthorisedFileException;

import java.util.Base64;

import static org.hamcrest.CoreMatchers.equalTo;

public class UnixFilesGetFileContentIntegrationTest extends AbstractUnixFilesIntegrationTest {
    
    private final String multiLineTestString = "Hello world\nhello world on new line.\n";
    private final String encodedMultiLineTestString = Base64.getEncoder().encodeToString(multiLineTestString.getBytes());

    @Test
    public void testGetUnixFileContentWithEtag() {
        RestAssured.given().header(AUTH_HEADER).header("X-Return-Etag", "true").when().get(TEST_DIRECTORY + "/fileWithAccess")
            .then().statusCode(HttpStatus.SC_OK)
            .header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX))
            .body("content", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(multiLineTestString + "\n"));
    }

    @Test
    public void testGetUnixFileContentWithGzip() {
        RestAssured.given().header(AUTH_HEADER).when().get(TEST_DIRECTORY + "/fileWithAccess")
                .then().statusCode(HttpStatus.SC_OK)
                .header("Content-Encoding", "gzip")
                .body("content", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(multiLineTestString + "\n"));
    }
    
    @Test
    public void testGetUnixFileContentWithConvertTrue() {
         RestAssured.given().header("Convert", true).header(AUTH_HEADER).header("X-Return-Etag", "true")
             .when().get(TEST_DIRECTORY + "/fileWithAccessAscii")
            .then().statusCode(HttpStatus.SC_OK)
            .header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX))
            .body("content", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(encodedMultiLineTestString));
    }
    
    @Test
    public void testGetUnixFileContentBinaryWithConvertTrue() {
        String binary255ToBase64 = "AAAA/w==";
        RestAssured.given().header("Convert", true).header(AUTH_HEADER).header("X-Return-Etag", "true")
            .when().get(TEST_DIRECTORY + "/binaryExample/file.bin")
            .then().statusCode(HttpStatus.SC_OK)
            .header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX))
            .body("content", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(binary255ToBase64));
    }

    @Test
    public void testGetUnixFileContentWithConvertFalse() {
         RestAssured.given().header("Convert", false).header(AUTH_HEADER).header("X-Return-Etag", "true")
             .when().get(TEST_DIRECTORY + "/fileWithAccessEbcdic")
            .then().statusCode(HttpStatus.SC_OK)
            .header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX))
            .body("content", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(multiLineTestString + "\n"));
    }
    
    @Test
    public void testGetUnixFileContentAsciiTaggedFileWithConvertNull() {
         RestAssured.given().header(AUTH_HEADER).header("X-Return-Etag", "true").when().get(TEST_DIRECTORY + "/fileWithAccessAscii")
            .then().statusCode(HttpStatus.SC_OK)
            .header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX))
            .body("content", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(multiLineTestString + "\r\n"));
    }
    
    @Test
    public void testGetUnixFileContentEbcdicTaggedFileWithConvertNull() {
         RestAssured.given().header(AUTH_HEADER).header("X-Return-Etag", "true").when().get(TEST_DIRECTORY + "/fileWithAccessEbcdic")
            .then().statusCode(HttpStatus.SC_OK)
            .header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX))
            .body("content", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(multiLineTestString + "\n"));
    }
    
    @Test
    public void testGetUnifFileContentUnauthorised() {
        String unauthorisedFile = TEST_DIRECTORY + "/fileWithoutAccess";
        ApiError expectedError = new UnauthorisedFileException(unauthorisedFile).getApiError();
        
        RestAssured.given().header(AUTH_HEADER).when().get(unauthorisedFile)
            .then().statusCode(HttpStatus.SC_FORBIDDEN).header("Content-Encoding", "gzip")
            .body("message", equalTo(expectedError.getMessage()));
    }
    
    @Test
    public void testGetUnixFileFileNotFound() {
        String invalidPath = "/u/zzzzzztxt";
        ApiError expectedError = new FileNotFoundException(invalidPath).getApiError();
        
        RestAssured.given().header(AUTH_HEADER).when().get(invalidPath)
            .then().statusCode(HttpStatus.SC_NOT_FOUND).header("Content-Encoding", "gzip").contentType(ContentType.JSON)
            .body("message", equalTo(expectedError.getMessage()));
    }
    
}