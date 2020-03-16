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
import org.junit.Test;
import org.zowe.api.common.test.services.zosmf.AbstractZosmfRequestRunnerTest;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.unix.files.exceptions.UnauthorisedFileException;
import org.zowe.unix.files.model.UnixFileContent;
import org.zowe.unix.files.model.UnixFileContentWithETag;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Base64;

public class GetUnixFileContentZosmfRunnerTest extends AbstractZosmfRequestRunnerTest {
    
     private void get_unix_file_content_should_call_zosmf_and_parse_response_correctly(boolean decode) throws Exception {
        String path = "/u/directory/file.txt";
        String eTag = "E1B212479173E273A8ACFD682BCBEADE";
        String fileContent = loadTestFile("getUnixFileContent.json");
        String encodedFileContent = Base64.getEncoder().encodeToString(fileContent.getBytes());
        
        UnixFileContent expectedFileContent;
        if(decode) {
            expectedFileContent  = new UnixFileContent(fileContent);
        } else {
            expectedFileContent = new UnixFileContent(encodedFileContent);
        }
        
        UnixFileContentWithETag expected = new UnixFileContentWithETag(expectedFileContent, eTag);

        ResponseCache responseCache = mockTextResponse(HttpStatus.SC_OK, encodedFileContent);

        Header header = mock(Header.class);
        when(header.getValue()).thenReturn(eTag);
        when(responseCache.getFirstHeader("ETag")).thenReturn(header);

        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/fs%s", path));
        when(zosmfConnector.request(requestBuilder)).thenReturn(response);
        
        assertEquals(expected, new GetUnixFileContentZosmfRunner(path, false, decode).run(zosmfConnector));        
        verifyInteractions(requestBuilder, false);
    }
        
        
    @Test
    public void get_unix_file_content_decode_false_should_call_zosmf_and_parse_response_correctly() throws Exception {
        get_unix_file_content_should_call_zosmf_and_parse_response_correctly(false);
    }
    
    @Test
    public void get_unix_file_content_decode_true_should_call_zosmf_and_parse_response_correctly() throws Exception {
        get_unix_file_content_should_call_zosmf_and_parse_response_correctly(true);
    }
    
    @Test
    public void get_unix_file_content_throws_unauthorised_file_error_message() throws Exception {
        String path = "/not/auth/oris.ed";

        Exception expectedException = new UnauthorisedFileException(path);

        mockJsonResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, loadTestFile("getUnixFileContentUnauthorised.json"));
        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/fs%s", path));
        when(zosmfConnector.request(requestBuilder)).thenReturn(response);
        
        shouldThrow(expectedException, () -> new GetUnixFileContentZosmfRunner(path, false, false).run(zosmfConnector));
        verifyInteractions(requestBuilder, false);
    }
}
