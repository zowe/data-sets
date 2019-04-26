/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.unix.files.services.zosmf;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zowe.api.common.exceptions.PreconditionFailedException;
import org.zowe.api.common.test.services.zosmf.AbstractZosmfRequestRunnerTest;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.unix.files.exceptions.FileNotFoundException;
import org.zowe.unix.files.exceptions.UnauthorisedFileException;
import org.zowe.unix.files.model.UnixFileContent;
import org.zowe.unix.files.model.UnixFileContentWithETag;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PutUnixFileContentZosmfRunner.class })
<<<<<<< HEAD:data-sets-api-server/src/test/java/org/zowe/unix/files/services/zosmf/PutUnixFileContentRunnerTest.java
public class PutUnixFileContentRunnerTest extends AbstractZosmfRequestRunnerTest {

=======
public class PutUnixFileContentZosmfRunnerTest extends AbstractZosmfRequestRunnerTest {
    
>>>>>>> origin/master:data-sets-api-server/src/test/java/org/zowe/unix/files/services/zosmf/PutUnixFileContentZosmfRunnerTest.java
    @Test
    public void put_unix_file_content_should_call_zosmf_and_parse_response_correctly() throws Exception {
        putContentTest(null, false);
    }

    @Test
    public void put_unix_file_content_with_if_match_should_call_zosmf_and_parse_response_correctly() throws Exception {
        putContentTest("AR8Q34G89H348AWH490GH8H4GN90QH", false);
    }

    @Test
    public void put_unix_file_content_with_convert_should_call_zosmf_and_parse_response_correctly() throws Exception {
        putContentTest(null, true);
    }

    private void putContentTest(String inputETag, Boolean convert) throws Exception {
        String path = "/file";
        String eTag = "2A7F90DCB9C2F4D4A582E36F859AEF";
        UnixFileContent content = new UnixFileContent("hello world");
        UnixFileContentWithETag contentWithETag = new UnixFileContentWithETag(content, inputETag);

        ResponseCache responseCache = mockResponseCache(HttpStatus.SC_NO_CONTENT);
        Header header = mock(Header.class);
        when(header.getValue()).thenReturn(eTag);
        when(responseCache.getFirstHeader("ETag")).thenReturn(header);

        RequestBuilder requestBuilder = mockPutBuilder("restfiles/fs" + path, content.getContent());
        when(zosmfConnector.request(requestBuilder)).thenReturn(response);

        assertEquals(eTag, new PutUnixFileContentZosmfRunner(path, contentWithETag, convert).run(zosmfConnector));

        verifyInteractions(requestBuilder);
        verify(requestBuilder).addHeader("Content-type", ContentType.TEXT_PLAIN.getMimeType());
        if (null != inputETag) {
            verify(requestBuilder).addHeader("If-Match", inputETag);
        }
        if (convert) {
            verify(requestBuilder).addHeader("X-IBM-Data-Type", "binary");
        }
    }

    @Test
    public void put_unix_file_with_wrong_if_match_throws_correct_error() throws Exception {
        String path = "/noFile/here";
        Exception exception = new PreconditionFailedException(path);

        putContentTestWithException(path, exception, HttpStatus.SC_PRECONDITION_FAILED, null);
    }

    @Test
    public void put_unix_file_with_unauthorised_file_throws_correct_error() throws Exception {
        String path = "/not/auth/file";
        Exception exception = new UnauthorisedFileException(path);

        putContentTestWithException(path, exception, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "getUnixFileContentUnauthorised.json");
    }

    @Test
    public void put_unix_file_with_not_found_file_throws_correct_error() throws Exception {
        String path = "/not/found/file";
        Exception exception = new FileNotFoundException(path);

        putContentTestWithException(path, exception, HttpStatus.SC_NOT_FOUND, "getUnixFileContentNotFound.json");
    }

    private void putContentTestWithException(String path, Exception exception, int returnCode, String testFile)
            throws Exception {
        UnixFileContent content = new UnixFileContent("hello world");
        UnixFileContentWithETag contentWithETag = new UnixFileContentWithETag(content, null);

        if (null != testFile) {
            mockJsonResponse(returnCode, loadTestFile(testFile));
        } else {
            mockResponseCache(returnCode);
        }
        RequestBuilder requestBuilder = mockPutBuilder("restfiles/fs" + path, content.getContent());
        when(zosmfConnector.request(requestBuilder)).thenReturn(response);

        shouldThrow(exception,
                () -> new PutUnixFileContentZosmfRunner(path, contentWithETag, false).run(zosmfConnector));
        verifyInteractions(requestBuilder, false);
    }
}
