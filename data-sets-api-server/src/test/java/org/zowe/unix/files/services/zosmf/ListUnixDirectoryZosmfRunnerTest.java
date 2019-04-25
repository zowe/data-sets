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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zowe.data.sets.services.zosmf.AbstractZosmfRequestRunnerTest;
import org.zowe.unix.files.exceptions.UnauthorisedDirectoryException;
import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren;
import org.zowe.unix.files.model.UnixDirectoryChild;
import org.zowe.unix.files.model.UnixEntityType;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ListUnixDirectoryZosmfRunnerTest extends AbstractZosmfRequestRunnerTest {
    
    @Test
    public void get_unix_directory_list_should_call_zosmf_and_parse_response_correctly() throws Exception {
        UnixDirectoryChild file = UnixDirectoryChild
            .builder()
            .name("FileA")
            .type(UnixEntityType.FILE)
            .size(12345)
            .lastModified("2019-02-13T16:04:19")
            .link("http://localhost/a/directory/FileA")
            .build();
        UnixDirectoryChild directory = UnixDirectoryChild
            .builder()
            .name("DirectoryA")
            .type(UnixEntityType.DIRECTORY)
            .size(12345)
            .lastModified("2019-02-13T16:04:19")
            .link("http://localhost/a/directory/DirectoryA")
            .build();
        
        List<UnixDirectoryChild> children = Arrays.asList(file, directory);
        
        UnixDirectoryAttributesWithChildren expectedListedDirectory = UnixDirectoryAttributesWithChildren.builder()
                .owner("IBMUSER").group("GROUP1").type(UnixEntityType.DIRECTORY).permissionsSymbolic("dr-x---rwx")
                .size(8192).lastModified("2019-02-03T16:04:19").children(children).build();
        
        String path = "/a/directory";
        
        mockJsonResponse(HttpStatus.SC_OK, loadTestFile("getUnixDirectoryList.json"));
        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/fs?path=%s", path));
        when(zosmfConnector.request(requestBuilder)).thenReturn(response);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        assertEquals(expectedListedDirectory, new ListUnixDirectoryZosmfRunner(path).run(zosmfConnector));
        
        verifyInteractions(requestBuilder, true);
    }
    
    @Test
    public void get_unix_directory_list_unauthorised_throws_correct_error_message() throws Exception {
        String path = "/not/auth";
        
        Exception expectedException = new UnauthorisedDirectoryException(path);
        
        mockJsonResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, loadTestFile("getUnixDirectoryListUnauthorised.json"));
        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/fs?path=%s", path));
        when(zosmfConnector.request(requestBuilder)).thenReturn(response);
        
        shouldThrow(expectedException, () -> new ListUnixDirectoryZosmfRunner(path).run(zosmfConnector));
        verifyInteractions(requestBuilder, true);
    }

}
