package org.zowe.unix.files.services.zosmf;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.test.ZoweApiTest;
import org.zowe.unix.files.exceptions.UnauthorisedDirectoryException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ UnixFilesZosmfService.class })
public class UnixFileZosmfServiceTest extends ZoweApiTest {

    @Mock
    ZosmfConnector zosmfConnector;

    UnixFilesZosmfService unixFilesZosmfService;
    
    @Before
    public void setUp() throws Exception {
        unixFilesZosmfService = new UnixFilesZosmfService();
        unixFilesZosmfService.zosmfConnector = zosmfConnector;
    }    
    
    @Test
    public void testGetUnixDirectoryListRunnerValueCorrectlyReturned() throws Exception {
        String path = "/a/path"; 
        
        ListUnixDirectoryZosmfRunner runner = mock(ListUnixDirectoryZosmfRunner.class);
        PowerMockito.whenNew(ListUnixDirectoryZosmfRunner.class).withArguments(path).thenReturn(runner);
        unixFilesZosmfService.listUnixDirectory(path);
        
        verify(runner).run(zosmfConnector);
    }
    
    @Test
    public void testGetUnixDirectoryListRunnerExceptionThrown() throws Exception {
        String path = "/a/path";
        
        ZoweApiRestException expectedException = new UnauthorisedDirectoryException(path);
        
        ListUnixDirectoryZosmfRunner runner = mock(ListUnixDirectoryZosmfRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(ListUnixDirectoryZosmfRunner.class).withArguments(path).thenReturn(runner);
        shouldThrow(expectedException, () -> unixFilesZosmfService.listUnixDirectory(path));
    }
}
