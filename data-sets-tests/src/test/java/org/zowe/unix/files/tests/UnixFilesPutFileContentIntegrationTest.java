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

import org.apache.http.HttpStatus;
import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.hamcrest.text.MatchesPattern;
import org.junit.Test;
import org.zowe.api.common.errors.ApiError;
import org.zowe.api.common.exceptions.PreconditionFailedException;
import org.zowe.unix.files.exceptions.FileNotFoundException;
import org.zowe.unix.files.exceptions.UnauthorisedFileException;
import org.zowe.unix.files.model.UnixFileContent;

import java.util.Base64;

import static org.hamcrest.CoreMatchers.equalTo;

public class UnixFilesPutFileContentIntegrationTest extends AbstractUnixFilesIntegrationTest {

    @Test
    public void testPutUnixFileContentWithEtag() {
        final UnixFileContent content = new UnixFileContent("New testable content \\n testPutUnixFileContent");

        RestAssured.given().header(AUTH_HEADER).header("X-Return-Etag", "true").contentType("application/json").body(content)
            .when().put(TEST_DIRECTORY + "/editableFile")
            .then().statusCode(HttpStatus.SC_NO_CONTENT)
            .header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX));

        RestAssured.given().header(AUTH_HEADER).when().get(TEST_DIRECTORY + "/editableFile")
            .then().statusCode(HttpStatus.SC_OK)
            .body("content", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(content.getContent()));
    }

    @Test
    public void testPutUnixFileContentWithGzip() {
        final UnixFileContent content = new UnixFileContent("New testable content \\n testPutUnixFileContent");

        RestAssured.given().header(AUTH_HEADER).contentType("application/json").body(content)
                .when().put(TEST_DIRECTORY + "/editableFile")
                .then().statusCode(HttpStatus.SC_NO_CONTENT)
                .header("Content-Encoding", "gzip");

        RestAssured.given().header(AUTH_HEADER).when().get(TEST_DIRECTORY + "/editableFile")
                .then().statusCode(HttpStatus.SC_OK)
                .body("content", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(content.getContent()));
    }

    @Test
    public void testPutUnixFileContentFileNotFound() {
        String path = TEST_DIRECTORY + "/randomName";
        ApiError expectedError = new FileNotFoundException(path).getApiError();

        testPutUnixFileWithError(path, expectedError, false);
    }

    @Test
    public void testPutUnixFileContentUnauthorised() {
        String path = TEST_DIRECTORY + "/fileWithoutAccess";
        ApiError expectedError = new UnauthorisedFileException(path).getApiError();

        testPutUnixFileWithError(path, expectedError, false);

    }

    private void testPutUnixFileWithError(String path, ApiError expectedError, boolean ifMatch) {
        final UnixFileContent content = new UnixFileContent("New testable content");

        if (ifMatch) {
            RestAssured.given().header("If-Match", "wrong").header(AUTH_HEADER)
                .contentType("application/json").body(content).when().put(path)
                .then().statusCode(expectedError.getStatus().value())
                .body("message", equalTo(expectedError.getMessage()));
        } else {
            RestAssured.given().header(AUTH_HEADER)
                .contentType("application/json").body(content).when().put(path)
                .then().statusCode(expectedError.getStatus().value())
                .body("message", equalTo(expectedError.getMessage()));
        }
    }

    @Test
    public void testPutUnixFileContentWithCorrectIfMatch() {
        final UnixFileContent content = new UnixFileContent("New testable content \\n testPutUnixFileContentWithCorrectIfMatch");

        String eTag = RestAssured.given().header(AUTH_HEADER).header("X-Return-Etag", "true").when().get(TEST_DIRECTORY + "/editableFile")
            .then().statusCode(HttpStatus.SC_OK)
            .extract().header("ETag");

        RestAssured.given().header("If-Match", eTag).header(AUTH_HEADER).header("X-Return-Etag", "true")
            .contentType("application/json").body(content).when().put(TEST_DIRECTORY + "/editableFile")
            .then().statusCode(HttpStatus.SC_NO_CONTENT)
            .header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX));

        RestAssured.given().header(AUTH_HEADER).when().get(TEST_DIRECTORY + "/editableFile")
            .then().statusCode(HttpStatus.SC_OK)
            .body("content", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(content.getContent()));
    }

    @Test
    public void testPutUnixFileContentWithWrongIfMatch() {
        String path = TEST_DIRECTORY + "/editableFile";
        ApiError expectedError = new PreconditionFailedException(path).getApiError();

        testPutUnixFileWithError(path, expectedError, true);
    }

    @Test
    public void testPutUnixFileContentWithConvertTrueAndAsciiFile() {
        final String fileContent = "New testable content \\n testPutUnixFileContentWithConvertTrueAndAsciiFile";
        final UnixFileContent content = new UnixFileContent(fileContent);

        RestAssured.given().contentType("application/json").body(content).header("Convert", true).header(AUTH_HEADER)
            .header("X-Return-Etag", "true")
            .when().put(TEST_DIRECTORY + "/editableAsciiTaggedFile")
            .then().statusCode(HttpStatus.SC_NO_CONTENT)
            .header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX));

        final String encodedFileContent = Base64.getEncoder().encodeToString(fileContent.getBytes());
        RestAssured.given().header("Convert", true).header(AUTH_HEADER).when().get(TEST_DIRECTORY + "/editableAsciiTaggedFile")
            .then().statusCode(HttpStatus.SC_OK)
            .body("content", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(encodedFileContent));
    }

    @Test
    public void testPutUnixFileContentWithConvertNullAndAsciiFile() {
        final String fileContent = "New testable content \\n testPutUnixFileContentWithConvertTrueAndAsciiFile";
        final UnixFileContent content = new UnixFileContent(fileContent);

        RestAssured.given().header(AUTH_HEADER).header("X-Return-Etag", "true").contentType("application/json").body(content)
            .when().put(TEST_DIRECTORY + "/editableAsciiTaggedFile")
            .then().statusCode(HttpStatus.SC_NO_CONTENT)
            .header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX));

        final String encodedFileContent = Base64.getEncoder().encodeToString(fileContent.getBytes());
        RestAssured.given().header("Convert", true).header(AUTH_HEADER).when().get(TEST_DIRECTORY + "/editableAsciiTaggedFile")
            .then().statusCode(HttpStatus.SC_OK)
            .body("content", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(encodedFileContent));
    }

    @Test
    public void testPutUnixFileContentWithConvertFalseAndEbcdicFile() {
        final UnixFileContent content = new UnixFileContent("New testable content \\n testPutUnixFileContentWithConvertFalseAndEbcdicFile");

        RestAssured.given().header("Convert", false).header(AUTH_HEADER).header("X-Return-Etag", "true")
            .contentType("application/json").body(content).when().put(TEST_DIRECTORY + "/editableEbcdicTaggedFile")
            .then().statusCode(HttpStatus.SC_NO_CONTENT)
            .header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX));

        RestAssured.given().header(AUTH_HEADER).when().get(TEST_DIRECTORY + "/editableEbcdicTaggedFile")
            .then().statusCode(HttpStatus.SC_OK)
            .body("content", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(content.getContent()));
    }

    @Test
    public void testPutUnixFileContentWithConvertNullAndEbcdicFile() {
        final UnixFileContent content = new UnixFileContent("New testable content \\n testPutUnixFileContentWithConvertNullAndEbcdicFile");

        RestAssured.given().header(AUTH_HEADER).header("X-Return-Etag", "true").contentType("application/json").body(content)
            .when().put(TEST_DIRECTORY + "/editableEbcdicTaggedFile")
            .then().statusCode(HttpStatus.SC_NO_CONTENT)
            .header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX));

        RestAssured.given().header(AUTH_HEADER).when().get(TEST_DIRECTORY + "/editableEbcdicTaggedFile")
            .then().statusCode(HttpStatus.SC_OK)
            .body("content", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(content.getContent()));
    }
}
