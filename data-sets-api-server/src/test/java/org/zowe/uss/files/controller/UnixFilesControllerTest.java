/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.uss.files.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.zowe.api.common.errors.ApiError;
import org.zowe.api.common.exceptions.ZoweApiErrorException;
import org.zowe.api.common.exceptions.ZoweRestExceptionHandler;
import org.zowe.api.common.utils.JsonUtils;
import org.zowe.api.common.utils.ZosUtils;
import org.zowe.unix.files.controller.UnixFilesController;
import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren;
import org.zowe.unix.files.model.UnixDirectoryChild;
import org.zowe.unix.files.model.UnixEntityType;
import org.zowe.unix.files.services.UnixFilesService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ZosUtils.class, ServletUriComponentsBuilder.class })
public class UnixFilesControllerTest {
    
    private static final String ENDPOINT_ROOT = "/api/v1/unixfiles";

    // TODO LATER - move up into ApiControllerTest - https://github.com/zowe/explorer-api-common/issues/11
    // Same Comment in DataSetsControllerTest
    private static final String DUMMY_USER = "A_USER";

    private MockMvc mockMvc;

    @Mock
    private UnixFilesService unixFilesService;

    @InjectMocks
    private UnixFilesController unixFilesController;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(unixFilesController)
            .setControllerAdvice(new ZoweRestExceptionHandler()).build();
        PowerMockito.mockStatic(ZosUtils.class);
        when(ZosUtils.getUsername()).thenReturn(DUMMY_USER);
    }
    
    @Test
    public void get_directory_listing_success() throws Exception {
        UnixDirectoryChild file = UnixDirectoryChild.builder().name("FileA").type(UnixEntityType.FILE)
                .link("somelink").build();
        UnixDirectoryChild directory = UnixDirectoryChild.builder().name("DirectoryA").type(UnixEntityType.DIRECTORY)
                .link("somelink").build();
        
        List<UnixDirectoryChild> children = Arrays.asList(file, directory);
        
        UnixDirectoryAttributesWithChildren listedDirectory = UnixDirectoryAttributesWithChildren.builder()
                .owner("IBMUSER").group("GROUP1").type(UnixEntityType.DIRECTORY).permissionsSymbolic("dr-x---rwx")
                .size(8192).lastModified("2019-02-03T16:04:19").children(children).build();
        String path = "/u/ibmuser";
        
        when(unixFilesService.listUnixDirectory(path)).thenReturn(listedDirectory);
        
        mockMvc.perform(get(ENDPOINT_ROOT + "?path={path}", path)).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().string(JsonUtils.convertToJsonString(listedDirectory)));
        
        verify(unixFilesService, times(1)).listUnixDirectory(path);
        verifyNoMoreInteractions(unixFilesService);
    }
    
    @Test
    public void get_directory_listing_with_exception_should_be_converted_to_error_message() throws Exception {
        String invalidPath = "/not/authorised";
        
        String errorMessage = String.format("You are not authorised to access directory ''{0}''", invalidPath);
        ApiError expectedError = ApiError.builder().message(errorMessage).status(HttpStatus.FORBIDDEN).build();
        
        when(unixFilesService.listUnixDirectory(invalidPath)).thenThrow(new ZoweApiErrorException(expectedError));
        
        mockMvc.perform(get(ENDPOINT_ROOT + "?path={path}", invalidPath))
            .andExpect(status().is(expectedError.getStatus().value()))
            .andExpect(jsonPath("$.status").value(expectedError.getStatus().name()))
            .andExpect(jsonPath("$.message").value(errorMessage));
        
        verify(unixFilesService, times(1)).listUnixDirectory(invalidPath);
        verifyNoMoreInteractions(unixFilesService);
    }
}
