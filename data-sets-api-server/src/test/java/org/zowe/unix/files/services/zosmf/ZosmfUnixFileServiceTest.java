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
    
    @Before
    public void setUp() throws Exception {
        zosmfUnixFilesService = new ZosmfUnixFilesService1();
        zosmfUnixFilesService.zosmfConnector = zosmfConnector;
    }    
    
    @Test
    public void testGetUnixDirectoryListRunnerValueCorrectlyReturned() throws Exception {
        String path = "/a/path"; 
        
        ListUnixDirectoryZosmfRunner runner = mock(ListUnixDirectoryZosmfRunner.class);
        PowerMockito.whenNew(ListUnixDirectoryZosmfRunner.class).withArguments(path, "").thenReturn(runner);
        zosmfUnixFilesService.listUnixDirectory(path, "");
        
        verify(runner).run(zosmfConnector);
    }
    
    @Test
    public void testGetUnixDirectoryListRunnerExceptionThrown() throws Exception {
        String path = "/a/path";
        
        ZoweApiRestException expectedException = new UnauthorisedDirectoryException(path);
        
        ListUnixDirectoryZosmfRunner runner = mock(ListUnixDirectoryZosmfRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(ListUnixDirectoryZosmfRunner.class).withArguments(path, "").thenReturn(runner);
        shouldThrow(expectedException, () -> zosmfUnixFilesService.listUnixDirectory(path, ""));
    }
}
