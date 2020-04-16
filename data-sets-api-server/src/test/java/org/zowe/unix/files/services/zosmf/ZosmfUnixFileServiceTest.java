/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019, 2020
 */
package org.zowe.unix.files.services.zosmf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zowe.api.common.connectors.zosmf.ZosmfConnectorLtpaAuth;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.test.ZoweApiTest;
import org.zowe.unix.files.exceptions.UnauthorisedDirectoryException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ZosmfUnixFilesService1.class })
public class ZosmfUnixFileServiceTest extends ZoweApiTest {

    @Mock
    ZosmfConnectorLtpaAuth zosmfConnector;

    ZosmfUnixFilesService1 zosmfUnixFilesService;
    
    private final String UNIX_PATH = "/a/path";
    
    @Before
    public void setUp() throws Exception {
        zosmfUnixFilesService = new ZosmfUnixFilesService1();
        zosmfUnixFilesService.zosmfConnector = zosmfConnector;
    }    
    
    @Test
    public void testGetUnixDirectoryListRunnerValueCorrectlyReturned() throws Exception {
        ListUnixDirectoryZosmfRunner runner = mock(ListUnixDirectoryZosmfRunner.class);
        PowerMockito.whenNew(ListUnixDirectoryZosmfRunner.class).withArguments(UNIX_PATH, "").thenReturn(runner);
        zosmfUnixFilesService.listUnixDirectory(UNIX_PATH, "");
        
        verify(runner).run(zosmfConnector);
    }
    
    @Test
    public void testGetUnixDirectoryListRunnerExceptionThrown() throws Exception {
        ZoweApiRestException expectedException = new UnauthorisedDirectoryException(UNIX_PATH);
        
        ListUnixDirectoryZosmfRunner runner = mock(ListUnixDirectoryZosmfRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(ListUnixDirectoryZosmfRunner.class).withArguments(UNIX_PATH, "").thenReturn(runner);
        shouldThrow(expectedException, () -> zosmfUnixFilesService.listUnixDirectory(UNIX_PATH, ""));
    }
    
    @Test
    public void testGetUnixFileContentZosmfRunnerCorrectResponse() throws Exception {
        GetUnixFileContentZosmfRunner runner = mock(GetUnixFileContentZosmfRunner.class);
        PowerMockito.whenNew(GetUnixFileContentZosmfRunner.class).withArguments(UNIX_PATH, false, false).thenReturn(runner);
        zosmfUnixFilesService.getUnixFileContentWithETag(UNIX_PATH, false, false);
        
        verify(runner).run(zosmfConnector);
    }
    
    @Test
    public void testGetUnixFileContentZosmfRunnerExceptionThrown() throws Exception {
        ZoweApiRestException expectedException = new UnauthorisedDirectoryException(UNIX_PATH);
        
        GetUnixFileContentZosmfRunner runner = mock(GetUnixFileContentZosmfRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(GetUnixFileContentZosmfRunner.class).withArguments(UNIX_PATH, false, false).thenReturn(runner);
        shouldThrow(expectedException, () -> zosmfUnixFilesService.getUnixFileContentWithETag(UNIX_PATH, false, false));
    }
}
