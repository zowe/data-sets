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
import org.zowe.unix.files.model.UnixCreateAssetRequest;
import org.zowe.unix.files.model.UnixEntityType;
import org.zowe.unix.files.model.UnixFileContent;
import org.zowe.unix.files.model.UnixFileContentWithETag;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
    private final UnixFileContentWithETag UNIX_FILE_CONTENT = new UnixFileContentWithETag(new UnixFileContent("new content"), "etag");
    
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
    
    @Test
    public void testPutUnixFileContentZosmfRunnerCorrectResponse() throws Exception {
        PutUnixFileContentZosmfRunner runner = mock(PutUnixFileContentZosmfRunner.class);
        PowerMockito.whenNew(PutUnixFileContentZosmfRunner.class).withArguments(UNIX_PATH, UNIX_FILE_CONTENT, false).thenReturn(runner);
        zosmfUnixFilesService.putUnixFileContent(UNIX_PATH, UNIX_FILE_CONTENT, false);
        
        verify(runner).run(zosmfConnector);
    }
    
    @Test
    public void testPutUnixFileContentZosmfRunnerExceptionThrown() throws Exception {
        ZoweApiRestException expectedException = new UnauthorisedDirectoryException(UNIX_PATH);
        
        PutUnixFileContentZosmfRunner runner = mock(PutUnixFileContentZosmfRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(PutUnixFileContentZosmfRunner.class).withArguments(UNIX_PATH, UNIX_FILE_CONTENT, false).thenReturn(runner);
        shouldThrow(expectedException, () -> zosmfUnixFilesService.putUnixFileContent(UNIX_PATH, UNIX_FILE_CONTENT, false));
    }
    
    @Test
    public void testShouldUnixFileConvertShouldReturnTrueForISO8859() throws Exception {
        setupTestShouldUnixFileConvert("ISO8859-1");
        assertTrue(zosmfUnixFilesService.shouldUnixFileConvert(UNIX_PATH));
    }
    
    @Test
    public void testShouldUnixFileConvertShouldReturnTrueForIBM850() throws Exception {
        setupTestShouldUnixFileConvert("IBM-850");
        assertTrue(zosmfUnixFilesService.shouldUnixFileConvert(UNIX_PATH));
    }
    
    @Test
    public void testShouldUnixFileConvertShouldReturnTrueForUTF() throws Exception {
        setupTestShouldUnixFileConvert("UTF-8");
        assertTrue(zosmfUnixFilesService.shouldUnixFileConvert(UNIX_PATH));
    }
    
    @Test
    public void testShouldUnixFileConvertShouldReturnFalseForIBM1047() throws Exception {
        setupTestShouldUnixFileConvert("IBM-1047");
        assertFalse(zosmfUnixFilesService.shouldUnixFileConvert(UNIX_PATH));
    }
    
    public void setupTestShouldUnixFileConvert(String codepage) throws Exception {
        GetUnixFileChtagZosmfRunner runner = mock(GetUnixFileChtagZosmfRunner.class);
        PowerMockito.whenNew(GetUnixFileChtagZosmfRunner.class).withArguments(UNIX_PATH).thenReturn(runner);
        when(runner.run(zosmfConnector)).thenReturn(codepage);
    }
    
    @Test
    public void testGetUnixFileChtagZosmfRunnerCorrectResponse() throws Exception {
        GetUnixFileChtagZosmfRunner runner = mock(GetUnixFileChtagZosmfRunner.class);
        PowerMockito.whenNew(GetUnixFileChtagZosmfRunner.class).withArguments(UNIX_PATH).thenReturn(runner);
        zosmfUnixFilesService.getUnixFileChtag(UNIX_PATH);
        
        verify(runner).run(zosmfConnector);
    }
    
    @Test
    public void testGetUnixFileChtagZosmfRunnerExceptionThrown() throws Exception {
        ZoweApiRestException expectedException = new UnauthorisedDirectoryException(UNIX_PATH);
        
        GetUnixFileChtagZosmfRunner runner = mock(GetUnixFileChtagZosmfRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(GetUnixFileChtagZosmfRunner.class).withArguments(UNIX_PATH).thenReturn(runner);
        shouldThrow(expectedException, () -> zosmfUnixFilesService.getUnixFileChtag(UNIX_PATH));
    }
    
    @Test
    public void testDeleteUnixFileZosmfRunnerCorrectResponse() throws Exception {
        DeleteUnixFileZosmfRunner runner = mock(DeleteUnixFileZosmfRunner.class);
        PowerMockito.whenNew(DeleteUnixFileZosmfRunner.class).withArguments(UNIX_PATH, true).thenReturn(runner);
        zosmfUnixFilesService.deleteUnixFileContent(UNIX_PATH, true);
        
        verify(runner).run(zosmfConnector);
    }
    
    @Test
    public void testDeleteUnixFileZosmfRunnerExceptionThrown() throws Exception {
        ZoweApiRestException expectedException = new UnauthorisedDirectoryException(UNIX_PATH);
        
        DeleteUnixFileZosmfRunner runner = mock(DeleteUnixFileZosmfRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(DeleteUnixFileZosmfRunner.class).withArguments(UNIX_PATH, true).thenReturn(runner);
        shouldThrow(expectedException, () -> zosmfUnixFilesService.deleteUnixFileContent(UNIX_PATH, true));
    }
    
    @Test
    public void testCreateUnixAssetZosmfRunnerCorrectResponse() throws Exception {
        UnixCreateAssetRequest request = new UnixCreateAssetRequest(UnixEntityType.FILE, "-rwxrwxrwx");
        
        CreateUnixAssetZosmfRunner runner = mock(CreateUnixAssetZosmfRunner.class);
        PowerMockito.whenNew(CreateUnixAssetZosmfRunner.class).withArguments(UNIX_PATH, request).thenReturn(runner);
        zosmfUnixFilesService.createUnixAsset(UNIX_PATH, request);
        
        verify(runner).run(zosmfConnector);
    }
    
    @Test
    public void testCreateUnixAssetZosmfRunnerExceptionThrown() throws Exception {
        ZoweApiRestException expectedException = new UnauthorisedDirectoryException(UNIX_PATH);
        UnixCreateAssetRequest request = new UnixCreateAssetRequest(UnixEntityType.FILE, "-rwxrwxrwx");
        
        CreateUnixAssetZosmfRunner runner = mock(CreateUnixAssetZosmfRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(CreateUnixAssetZosmfRunner.class).withArguments(UNIX_PATH, request).thenReturn(runner);
        shouldThrow(expectedException, () -> zosmfUnixFilesService.createUnixAsset(UNIX_PATH, request));
    }
    
}
