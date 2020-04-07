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
import org.zowe.api.common.test.services.zosmf.AbstractZosmfRequestRunnerTest;
import org.zowe.unix.files.exceptions.FileNotFoundException;
import org.zowe.unix.files.exceptions.NotAnEmptyDirectoryException;
import org.zowe.unix.files.exceptions.UnauthorisedFileException;

import static org.mockito.Mockito.when;

public class DeleteUnixFileZosmfRequestRunnerTest extends AbstractZosmfRequestRunnerTest {

    @Test
    public void delete_unix_file_should_call_zosmf_correctly() throws Exception {
        String filename = "/u/nakul/testFileOrEmptyDirectory";

        mockResponseCache(HttpStatus.SC_NO_CONTENT);

        RequestBuilder builder = mockDeleteBuilder(String.format("restfiles/fs%s", filename));

        when(zosmfConnector.executeRequest(builder)).thenReturn(response);

        new DeleteUnixFileZosmfRunner(filename).run(zosmfConnector);

        verifyInteractions(builder);
    }

    @Test
    public void delete_unix_file_non_empty_driectory_with_option() throws Exception {
        String filename = "/u/nakul/testNonEmptyDirectory";

        mockResponseCache(HttpStatus.SC_NO_CONTENT);

        RequestBuilder builder = mockDeleteBuilder(String.format("restfiles/fs%s", filename));

        when(zosmfConnector.executeRequest(builder)).thenReturn(response);

        new DeleteUnixFileZosmfRunner(filename, true).run(zosmfConnector);

        verifyInteractions(builder);
    }

    @Test
    public void delete_unix_file_non_empty_driectory_without_option() throws Exception {
        String filename = "/u/nakul/testNonEmptyDirectory";

        Exception expectedException = new NotAnEmptyDirectoryException(filename);
        mockJsonResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, loadTestFile("deleteUnixFile_nonEmptyDirectory.json"));

        RequestBuilder requestBuilder = mockDeleteBuilder(String.format("restfiles/fs%s", filename));

        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        shouldThrow(expectedException, () -> new DeleteUnixFileZosmfRunner(filename).run(zosmfConnector));
        verifyInteractions(requestBuilder);
    }

    @Test
    public void delete_unix_file_for_non_existing_unix_file_should_throw_exception() throws Exception {
        String filename = "/u/nakul/testNonExistingFile";

        Exception expectedException = new FileNotFoundException(filename);
        mockJsonResponse(HttpStatus.SC_NOT_FOUND, loadTestFile("deleteUnixFile_doesntExist.json"));

        RequestBuilder requestBuilder = mockDeleteBuilder(String.format("restfiles/fs%s", filename));

        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        shouldThrow(expectedException, () -> new DeleteUnixFileZosmfRunner(filename).run(zosmfConnector));
        verifyInteractions(requestBuilder);
    }

    @Test
    public void delete_unix_file_for_no_write_permission_unix_file_should_throw_exception() throws Exception {
        String filename = "/u/nakul/testNotAccessibleFile";

        Exception expectedException = new UnauthorisedFileException(filename);
        mockJsonResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, loadTestFile("deleteUnixFile_permissionDenied.json"));

        RequestBuilder requestBuilder = mockDeleteBuilder(String.format("restfiles/fs%s", filename));

        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        shouldThrow(expectedException, () -> new DeleteUnixFileZosmfRunner(filename).run(zosmfConnector));
        verifyInteractions(requestBuilder);
    }
}
