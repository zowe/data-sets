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

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.junit.Test;
import org.zowe.data.sets.services.zosmf.AbstractZosmfRequestRunnerTest;
import org.zowe.unix.files.exceptions.PathNameNotValidException;
import org.zowe.unix.files.exceptions.UnauthorisedFileException;
import org.zowe.unix.files.model.UnixFileContent;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class GetUnixFileContentRunnerTest extends AbstractZosmfRequestRunnerTest {
    
    @Test
    public void get_unix_file_content_should_call_zosmf_and_parse_response_correctly() throws Exception {
        UnixFileContent expectedFileContent = new UnixFileContent(loadTestFile("getUnixFileContent.json"));

        String path = "/u/directory/file.txt";
        
        mockTextResponse(HttpStatus.SC_OK, loadTestFile("getUnixFileContent.json"));
        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/fs%s", path));
        when(zosmfConnector.request(requestBuilder)).thenReturn(response);
        
        assertEquals(expectedFileContent, new GetUnixFileContentRunner(path).run(zosmfConnector));
        
        verifyInteractions(requestBuilder, false);
    }
    
    @Test
    public void get_unix_file_content_throws_unauthorised_file_error_message() throws Exception {
        String path = "/not/auth/oris.ed";
        
        Exception expectedException = new UnauthorisedFileException(path);
        
        mockJsonResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, loadTestFile("getUnixFileContentUnauthorised.json"));
        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/fs%s", path));
        when(zosmfConnector.request(requestBuilder)).thenReturn(response);
        
        shouldThrow(expectedException, () -> new GetUnixFileContentRunner(path).run(zosmfConnector));
        verifyInteractions(requestBuilder, false);
    }
    
    @Test
    public void get_unix_file_content_throws_not_valid_path_error_message() throws Exception {
        String path = "/not/validPath//";
        
        Exception expectedException = new PathNameNotValidException(path);
        
        mockJsonResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, loadTestFile("getUnixFileInvalidPath.json"));
        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/fs%s", path));
        when(zosmfConnector.request(requestBuilder)).thenReturn(response);
        
        shouldThrow(expectedException, () -> new GetUnixFileContentRunner(path).run(zosmfConnector));
        verifyInteractions(requestBuilder, false);
    }
}
