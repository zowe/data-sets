package org.zowe.data.sets.services.zosmf;

import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zowe.data.sets.exceptions.UnauthorisedDirectoryException;
import org.zowe.data.sets.model.UnixDirectoryAttributesWithChildren;
import org.zowe.data.sets.model.UnixDirectoryChild;
import org.zowe.data.sets.model.UnixEntityType;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ListUnixDirectoryZosmfRunnerTest extends AbstractZosmfRequestRunnerTest {
    
    @Test
    public void get_unix_directory_list_should_call_zosmf_and_parse_response_correctly() throws Exception {
        UnixDirectoryChild file = UnixDirectoryChild.builder().name("FileA").type(UnixEntityType.FILE)
                .link("http://localhost/a/directory/FileA").build();
        UnixDirectoryChild directory = UnixDirectoryChild.builder().name("DirectoryA").type(UnixEntityType.DIRECTORY)
                .link("http://localhost/a/directory/DirectoryA").build();
        
        List<UnixDirectoryChild> children = Arrays.asList(file, directory);
        
        UnixDirectoryAttributesWithChildren listedDirectory = UnixDirectoryAttributesWithChildren.builder()
                .owner("IBMUSER").group("GROUP1").type(UnixEntityType.DIRECTORY).permissionsSymbolic("dr-x---rwx")
                .size(8192).lastModified("2019-02-03T16:04:19").children(children).build();
        
        String path = "/a/directory";
        
        mockJsonResponse(HttpStatus.SC_OK, loadTestFile("getUnixDirectoryList.json"));
        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/fs?path=%s", path));
        when(zosmfConnector.request(requestBuilder)).thenReturn(response);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        
        assertEquals(listedDirectory, new ListUnixDirectoryZosmfRunner(path).run(zosmfConnector));
        
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
