/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019, 2020
 */
package org.zowe.unix.files.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.zowe.api.common.errors.ApiError;
import org.zowe.api.common.exceptions.ZoweApiErrorException;
import org.zowe.api.common.test.controller.ApiControllerTest;
import org.zowe.api.common.utils.JsonUtils;
import org.zowe.unix.files.model.UnixCreateAssetRequest;
import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren;
import org.zowe.unix.files.model.UnixDirectoryChild;
import org.zowe.unix.files.model.UnixEntityType;
import org.zowe.unix.files.model.UnixFileContent;
import org.zowe.unix.files.model.UnixFileContentWithETag;
import org.zowe.unix.files.services.UnixFilesService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ServletUriComponentsBuilder.class })
public class UnixFilesControllerTest extends ApiControllerTest {

    private static final String ENDPOINT_ROOT = "/api/v1/unixfiles";
    private static final String URI_BASE = "http://localhost/api/v1/unixfiles";

    @Mock
    private UnixFilesService unixFilesService;

    @InjectMocks
    private UnixFilesControllerV1 unixFilesController;

    @Override
    public Object getController() {
        return unixFilesController;
    }

    @Test
    public void get_directory_listing_success() throws Exception {
        UnixDirectoryChild file = UnixDirectoryChild.builder().name("FileA").type(UnixEntityType.FILE).link("somelink")
            .build();
        UnixDirectoryChild directory = UnixDirectoryChild.builder().name("DirectoryA").type(UnixEntityType.DIRECTORY)
            .link("somelink").build();

        List<UnixDirectoryChild> children = Arrays.asList(file, directory);

        UnixDirectoryAttributesWithChildren listedDirectory = UnixDirectoryAttributesWithChildren.builder()
            .owner("IBMUSER").group("GROUP1").type(UnixEntityType.DIRECTORY).permissionsSymbolic("dr-x---rwx")
            .size(8192).lastModified("2019-02-03T16:04:19").children(children).build();
        String path = "/u/ibmuser";

        when(unixFilesService.listUnixDirectory(path, URI_BASE)).thenReturn(listedDirectory);

        mockMvc.perform(get(ENDPOINT_ROOT + "?path={path}", path)).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string(JsonUtils.convertToJsonString(listedDirectory)));

        verify(unixFilesService, times(1)).listUnixDirectory(path, URI_BASE);
        verifyNoMoreInteractions(unixFilesService);
    }

    @Test
    public void get_directory_listing_with_exception_should_be_converted_to_error_message() throws Exception {
        String invalidPath = "/not/authorised";

        String errorMessage = String.format("You are not authorised to access directory ''{0}''", invalidPath);
        ApiError expectedError = ApiError.builder().message(errorMessage).status(HttpStatus.FORBIDDEN).build();

        when(unixFilesService.listUnixDirectory(invalidPath, URI_BASE)).thenThrow(new ZoweApiErrorException(expectedError));

        mockMvc.perform(get(ENDPOINT_ROOT + "?path={path}", invalidPath))
            .andExpect(status().is(expectedError.getStatus().value()))
            .andExpect(jsonPath("$.status").value(expectedError.getStatus().name()))
            .andExpect(jsonPath("$.message").value(errorMessage));

        verify(unixFilesService, times(1)).listUnixDirectory(invalidPath, URI_BASE);
        verifyNoMoreInteractions(unixFilesService);
    }

    @Test
    public void get_unix_file_content_with_no_etag() throws Exception {
        String path = "/file";
        UnixFileContent fileContent = new UnixFileContent("Some file content");

        UnixFileContentWithETag fileContentWithETag = new UnixFileContentWithETag(fileContent, null);

        when(unixFilesService.getUnixFileContentWithETag(any(), anyBoolean(), anyBoolean())).thenReturn(fileContentWithETag);

        mockMvc.perform(get(ENDPOINT_ROOT + path))
                .andExpect(status().isOk())
                .andExpect(content().string(JsonUtils.convertToJsonString(fileContent)))
                .andExpect(header().string("ETag", equalTo(null)));

        verify(unixFilesService, times(1)).getUnixFileContentWithETag(any(), anyBoolean(), anyBoolean());
    }

    @Test
    public void get_unix_file_content_convert_false_success() throws Exception {
        String path = "/file";
        String eTag = "\"E1B212479173E273A8ACFD682BCBEADE\"";
        UnixFileContent fileContent = new UnixFileContent("Some file content");

        UnixFileContentWithETag fileContentWithETag = new UnixFileContentWithETag(fileContent, eTag);

        when(unixFilesService.getUnixFileContentWithETag(path, false, false)).thenReturn(fileContentWithETag);

        mockMvc.perform(get(ENDPOINT_ROOT + path)
                .header("Convert", false)
                .header("X-Return-Etag", "true"))
            .andExpect(status().isOk())
            .andExpect(content().string(JsonUtils.convertToJsonString(fileContent)))
            .andExpect(header().string("ETag", equalTo(eTag)));

        verify(unixFilesService, times(1)).getUnixFileContentWithETag(path, false, false);
        verifyNoMoreInteractions(unixFilesService);
    }

    @Test
    public void get_unix_file_content_convert_null_shouldUnixConvert_false_success() throws Exception {
        String path = "/file";
        String eTag = "\"E1B212479173E273A8ACFD682BCBEADE\"";
        UnixFileContent fileContent = new UnixFileContent("Some file content");

        UnixFileContentWithETag fileContentWithETag = new UnixFileContentWithETag(fileContent, eTag);

        when(unixFilesService.getUnixFileContentWithETag(path, false, false)).thenReturn(fileContentWithETag);
        when(unixFilesService.shouldUnixFileConvert(path)).thenReturn(false);


        mockMvc.perform(get(ENDPOINT_ROOT + path)
                .header("X-Return-Etag", "true"))
            .andExpect(status().isOk())
            .andExpect(content().string(JsonUtils.convertToJsonString(fileContent)))
            .andExpect(header().string("ETag", equalTo(eTag)));

        verify(unixFilesService, times(1)).shouldUnixFileConvert(path);
        verify(unixFilesService, times(1)).getUnixFileContentWithETag(path, false, false);
        verifyNoMoreInteractions(unixFilesService);
    }

    @Test
    public void get_unix_file_content_convert_null_shouldUnixConvert_true_success() throws Exception {
        String path = "/file";
        String eTag = "\"E1B212479173E273A8ACFD682BCBEADE\"";
        UnixFileContent fileContent = new UnixFileContent("Some file content");

        UnixFileContentWithETag fileContentWithETag = new UnixFileContentWithETag(fileContent, eTag);

        when(unixFilesService.getUnixFileContentWithETag(path, true, true)).thenReturn(fileContentWithETag);
        when(unixFilesService.shouldUnixFileConvert(path)).thenReturn(true);


        mockMvc.perform(get(ENDPOINT_ROOT + path)
                .header("X-Return-Etag", "true"))
            .andExpect(status().isOk())
            .andExpect(content().string(JsonUtils.convertToJsonString(fileContent)))
            .andExpect(header().string("ETag", equalTo(eTag)));

        verify(unixFilesService, times(1)).shouldUnixFileConvert(path);
        verify(unixFilesService, times(1)).getUnixFileContentWithETag(path, true, true);
        verifyNoMoreInteractions(unixFilesService);
    }

    @Test
    public void get_unix_file_content_convert_true_success() throws Exception {
        String path = "/file";
        String eTag = "\"E1B212479173E273A8ACFD682BCBEADE\"";
        UnixFileContent fileContent = new UnixFileContent("Some file content");

        UnixFileContentWithETag fileContentWithETag = new UnixFileContentWithETag(fileContent, eTag);

        when(unixFilesService.getUnixFileContentWithETag(path, true, false)).thenReturn(fileContentWithETag);

        mockMvc.perform(get(ENDPOINT_ROOT + path)
                .header("Convert", true)
                .header("X-Return-Etag", "true"))
            .andExpect(status().isOk())
            .andExpect(content().string(JsonUtils.convertToJsonString(fileContent)))
            .andExpect(header().string("ETag", equalTo(eTag)));

        verify(unixFilesService, times(1)).getUnixFileContentWithETag(path, true, false);
        verifyNoMoreInteractions(unixFilesService);
    }

    @Test
    public void get_unix_file_content_with_exception_should_be_converted_to_error_message() throws Exception {
        String path = "/notAuth";

        String errorMessage = String.format("You are not authorised to access file %s", path);
        ApiError expectedError = ApiError.builder().message(errorMessage).status(HttpStatus.FORBIDDEN).build();

        when(unixFilesService.getUnixFileContentWithETag(path, false, false))
            .thenThrow(new ZoweApiErrorException(expectedError));

        mockMvc.perform(get(ENDPOINT_ROOT + path)
                .header("Convert", false))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.status").value(expectedError.getStatus().name()))
            .andExpect(jsonPath("$.message").value(errorMessage));

        verify(unixFilesService, times(1)).getUnixFileContentWithETag(path, false, false);
        verifyNoMoreInteractions(unixFilesService);
    }

    @Test
    public void put_unix_file_content_success() throws Exception {
        String path = "/file";
        UnixFileContent fileContent = new UnixFileContent("Some file content");
        UnixFileContentWithETag fileContentWithETag = new UnixFileContentWithETag(fileContent, null);
        String eTag = "\"E1B212479173E273A8ACFD682BCBEADE\"";

        when(unixFilesService.putUnixFileContent(path, fileContentWithETag, false)).thenReturn(eTag);

        mockMvc
            .perform(put(ENDPOINT_ROOT + path)
                .header("Convert", false)
                .header("X-Return-Etag", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.convertToJsonString(fileContent.getContent())))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""))
            .andExpect(header().string("ETag", eTag));

        verify(unixFilesService, times(1)).putUnixFileContent(path, fileContentWithETag, false);
        verify(unixFilesService, times(1)).getUnixFileContentWithETag(path, false, false);
        verifyNoMoreInteractions(unixFilesService);
    }

    @Test
    public void put_unix_file_content_success_with_if_match() throws Exception {
        String path = "/file";
        String ifMatch = "\"29387FH925H72H4527459G2974GH849F\"";
        String eTag = "\"E1B212479173E273A8ACFD682BCBEADE\"";
        UnixFileContent fileContent = new UnixFileContent("Some file content");
        UnixFileContentWithETag fileContentWithETag = new UnixFileContentWithETag(fileContent, ifMatch);

        when(unixFilesService.putUnixFileContent(path, fileContentWithETag, false)).thenReturn(eTag);

        mockMvc.perform(put(ENDPOINT_ROOT + path)
                .header("Convert", false)
                .header("If-Match", ifMatch)
                .header("X-Return-Etag", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.convertToJsonString(fileContent.getContent())))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""))
            .andExpect(header().string("ETag", eTag));

        verify(unixFilesService, times(1)).putUnixFileContent(path, fileContentWithETag, false);
        verify(unixFilesService, times(1)).getUnixFileContentWithETag(path, false, false);
        verifyNoMoreInteractions(unixFilesService);
    }

    @Test
    public void put_unix_file_content_success_without_convert_set() throws Exception {
        String path = "/file";
        UnixFileContent fileContent = new UnixFileContent("Some file content");
        UnixFileContentWithETag fileContentWithETag = new UnixFileContentWithETag(fileContent, null);
        String eTag = "\"E1B212479173E273A8ACFD682BCBEADE\"";

        when(unixFilesService.putUnixFileContent(path, fileContentWithETag, false)).thenReturn(eTag);
        when(unixFilesService.shouldUnixFileConvert(path)).thenReturn(false);

        mockMvc
            .perform(put(ENDPOINT_ROOT + path)
                .header("X-Return-Etag", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.convertToJsonString(fileContent.getContent())))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""))
            .andExpect(header().string("ETag", eTag));

        verify(unixFilesService, times(1)).putUnixFileContent(path, fileContentWithETag, false);
        verify(unixFilesService, times(1)).getUnixFileContentWithETag(path, false, false);
        verify(unixFilesService, times(1)).shouldUnixFileConvert(path);
        verifyNoMoreInteractions(unixFilesService);
    }

    @Test
    public void put_unix_file_content_with_exception_should_be_converted_to_error_message() throws Exception {
        String path = "/directory";
        UnixFileContent fileContent = new UnixFileContent("Some file content");
        UnixFileContentWithETag fileContentWithETag = new UnixFileContentWithETag(fileContent, null);

        String errorMessage = String.format("Precondition (eg. ETag) failed trying to edit %s", path);
        ApiError expectedError = ApiError.builder().message(errorMessage).status(HttpStatus.PRECONDITION_FAILED)
            .build();

        when(unixFilesService.putUnixFileContent(path, fileContentWithETag, false))
            .thenThrow(new ZoweApiErrorException(expectedError));

        mockMvc
            .perform(put(ENDPOINT_ROOT + path).header("Convert", false).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.convertToJsonString(fileContent.getContent())))
            .andExpect(status().isPreconditionFailed())
            .andExpect(jsonPath("$.status").value(expectedError.getStatus().name()))
            .andExpect(jsonPath("$.message").value(errorMessage));

        verify(unixFilesService, times(1)).putUnixFileContent(path, fileContentWithETag, false);
        verify(unixFilesService, times(1)).getUnixFileContentWithETag(path, false, false);
        verifyNoMoreInteractions(unixFilesService);
    }

    @Test
    public void put_unix_file_content_with_chtag_exception_should_be_converted_to_error_message() throws Exception {
        String path = "/directory";
        UnixFileContent fileContent = new UnixFileContent("Some file content");

        String errorMessage = String.format("Requested file %s is a directory", path);
        ApiError expectedError = ApiError.builder().message(errorMessage).status(HttpStatus.BAD_REQUEST).build();

        when(unixFilesService.shouldUnixFileConvert(path)).thenThrow(new ZoweApiErrorException(expectedError));

        mockMvc
            .perform(put(ENDPOINT_ROOT + path).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.convertToJsonString(fileContent.getContent())))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(expectedError.getStatus().name()))
            .andExpect(jsonPath("$.message").value(errorMessage));

        verify(unixFilesService, times(1)).getUnixFileContentWithETag(path, false, false);
        verify(unixFilesService, times(1)).shouldUnixFileConvert(path);
        verifyNoMoreInteractions(unixFilesService);
    }

    @Test
    public void put_unix_file_content_when_not_found_should_be_converted_to_error_message() throws Exception {
        String path = "/directory";
        UnixFileContent fileContent = new UnixFileContent("Some file content");

        String errorMessage = String.format("Requested file %s not found", path);
        ApiError expectedError = ApiError.builder().message(errorMessage).status(HttpStatus.NOT_FOUND).build();

        when(unixFilesService.getUnixFileContentWithETag(path, false, false))
            .thenThrow(new ZoweApiErrorException(expectedError));

        mockMvc
            .perform(put(ENDPOINT_ROOT + path).contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.convertToJsonString(fileContent.getContent())))
            .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(expectedError.getStatus().name()))
            .andExpect(jsonPath("$.message").value(errorMessage));

        verify(unixFilesService, times(1)).getUnixFileContentWithETag(path, false, false);
        verifyNoMoreInteractions(unixFilesService);
    }


    @Test
    public void test_delete_calls_service_properly() throws Exception {
        String dummy = "/junk";

        mockMvc.perform(delete(ENDPOINT_ROOT + "{dsn}", dummy)).andExpect(status().isNoContent())
                .andExpect(jsonPath("$").doesNotExist());

        verify(unixFilesService, times(1)).deleteUnixFileContent(dummy,false);
        verifyNoMoreInteractions(unixFilesService);
    }

    @Test
    public void delete_unix_file_with_exception_should_be_converted_to_error_message() throws Exception {
        String path = "/junk";

        String errorMessage = String.format("Requested file ''{0}'' not found", path);
        ApiError expectedError = ApiError.builder().message(errorMessage).status(HttpStatus.NOT_FOUND)
                .build();

        doThrow(new ZoweApiErrorException(expectedError)).when(unixFilesService).deleteUnixFileContent(path,false);

        mockMvc.perform(delete(ENDPOINT_ROOT + "{dsn}", path)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(expectedError.getStatus().name()))
                .andExpect(jsonPath("$.message").value(expectedError.getMessage()));

        verify(unixFilesService, times(1)).deleteUnixFileContent(path,false);
        verifyNoMoreInteractions(unixFilesService);
    }


    @Test
    public void post_unix_file_succss() throws Exception {
        String path = "/u/newFile";
        String permissions = "rwxrwxrwx";
        UnixCreateAssetRequest createRequest = new UnixCreateAssetRequest(UnixEntityType.FILE, permissions);

        mockMvc.perform(post(ENDPOINT_ROOT + path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.convertToJsonString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost" + ENDPOINT_ROOT + path));

        verify(unixFilesService, times(1)).createUnixAsset(path, createRequest);
        verifyNoMoreInteractions(unixFilesService);
    }

    @Test
    public void post_unix_file_with_conflict_exception_should_be_converted_to_error_message() throws Exception {
        String path = "/u/newFile";
        String permissions = "rwxrwxrwx";
        UnixCreateAssetRequest createRequest = new UnixCreateAssetRequest(UnixEntityType.FILE, permissions);

        String errorMessage = String.format("%s already exists", path);
        ApiError expectedError = ApiError.builder().message(errorMessage).status(HttpStatus.CONFLICT).build();


        doThrow(new ZoweApiErrorException(expectedError)).when(unixFilesService).createUnixAsset(path, createRequest);

        mockMvc.perform(post(ENDPOINT_ROOT + path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.convertToJsonString(createRequest)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(expectedError.getStatus().name()))
            .andExpect(jsonPath("$.message").value(errorMessage));

        verify(unixFilesService, times(1)).createUnixAsset(path, createRequest);
        verifyNoMoreInteractions(unixFilesService);
    }
}
