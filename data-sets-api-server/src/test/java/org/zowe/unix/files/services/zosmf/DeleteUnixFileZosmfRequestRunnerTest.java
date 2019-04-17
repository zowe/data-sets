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
import org.zowe.unix.files.exceptions.FileNotFoundException;
import org.zowe.unix.files.exceptions.PermissionDeniedFileException;
import org.zowe.unix.files.services.zosmf.DeleteUnixFileRunner;

import static org.mockito.Mockito.when;

public class DeleteUnixFileZosmfRequestRunnerTest extends AbstractZosmfRequestRunnerTest {

    @Test
    public void delete_unix_file_should_call_zosmf_correctly() throws Exception {
        String filename = "/u/nakul/testFile";
        
        mockResponseCache(HttpStatus.SC_NO_CONTENT);
        
        RequestBuilder builder = mockDeleteBuilder(String.format("restfiles/fs%s", filename));

        when(zosmfConnector.request(builder)).thenReturn(response);

        new DeleteUnixFileRunner(filename).run(zosmfConnector);

        verifyInteractions(builder);
    }

    @Test
    public void delete_unix_file_for_non_existing_unix_file_should_throw_exception() throws Exception {
        String filename = "/u/nakul/testFile";
        
        Exception expectedException = new FileNotFoundException(filename);
        mockJsonResponse(HttpStatus.SC_NOT_FOUND, loadTestFile("deleteUnixFile_doesntExist.json"));

        RequestBuilder requestBuilder = mockDeleteBuilder(String.format("restfiles/fs%s", filename));

        when(zosmfConnector.request(requestBuilder)).thenReturn(response);

        shouldThrow(expectedException, () -> new DeleteUnixFileRunner(filename).run(zosmfConnector));
        verifyInteractions(requestBuilder);
    }
    
    @Test
    public void delete_unix_file_for_no_write_permission_unix_file_should_throw_exception() throws Exception {
        String filename = "/u/nakul/testFile";
        
        Exception expectedException = new PermissionDeniedFileException(filename);
        mockJsonResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, loadTestFile("deleteUnixFile_permissionDenied.json"));

        RequestBuilder requestBuilder = mockDeleteBuilder(String.format("restfiles/fs%s", filename));

        when(zosmfConnector.request(requestBuilder)).thenReturn(response);

        shouldThrow(expectedException, () -> new DeleteUnixFileRunner(filename).run(zosmfConnector));
        verifyInteractions(requestBuilder);
    }
}
