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

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.zowe.api.common.exceptions.ZoweRestExceptionHandler;
import org.zowe.api.common.utils.JsonUtils;
import org.zowe.api.common.utils.ZosUtils;
import org.zowe.data.sets.model.UnixFileAtributes;
import org.zowe.data.sets.services.DataSetService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ZosUtils.class, ServletUriComponentsBuilder.class })
public class USSFilesControllerTest {
    
    private static final String ENDPOINT_ROOT = "/api/v1/ussFiles";

    // TODO LATER - move up into ApiControllerTest - https://github.com/zowe/explorer-api-common/issues/11
    // Same Comment in DataSetsControllerTest
    private static final String DUMMY_USER = "A_USER";

    private MockMvc mockMvc;

    @Mock
    private DataSetService dataSetService;

    @InjectMocks
    private USSFilesControllerTest ussFilesController;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(ussFilesController)
            .setControllerAdvice(new ZoweRestExceptionHandler()).build();
        PowerMockito.mockStatic(ZosUtils.class);
        when(ZosUtils.getUsername()).thenReturn(DUMMY_USER);
    }
    
    @Test
    public void get_directory_listing_success() throws Exception {
        UnixFileAtributes file = UnixFileAtributes.builder().name("FileA")
                .accessMode("-r--r--r--").size(12345).userId("317").user(DUMMY_USER)
                .groupId("234").group("IZUUSR").lastModified("2019-02-13T16:04:19").build();
        UnixFileAtributes directory = UnixFileAtributes.builder().name("DirectoryA")
                .accessMode("drwxrwxrwx").size(12345).userId("317").user(DUMMY_USER)
                .groupId("234").group("IZUADMIN").lastModified("2019-02-13T16:04:19").build();
        
        List<UnixFileAtributes> directoryListing = Arrays.asList(file, directory);
        String path = "/u/ibmuser";
        
        when(dataSetService.listUnixDirectory(path)).thenReturn(directoryListing);
        
        mockMvc.perform(get(ENDPOINT_ROOT + "?path={path}", path)).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().string(JsonUtils.convertToJsonString(directoryListing)));
        
        verify(dataSetService, times(1)).listUnixDirectory(path);
        verifyNoMoreInteractions(dataSetService);
    }
    
    //@Test
    public void get_directory_listing_with_exception_should_be_converted_to_error_message() {}    
    
}
