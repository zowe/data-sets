/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018
 */
package org.zowe.data.sets.controller;

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
import org.zowe.data.sets.services.DataSetService;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
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
public class DataSetsControllerTest {

    // TODO - refactor and merge with JobControllerTest?
    private static final String DUMMY_USER = "A_USER";

    private MockMvc mockMvc;

    @Mock
    private DataSetService dataSetService;

    @InjectMocks
    private DataSetsController datasetsController;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(datasetsController)
                .setControllerAdvice(new ZoweRestExceptionHandler()).build();
        PowerMockito.mockStatic(ZosUtils.class);
        when(ZosUtils.getUsername()).thenReturn(DUMMY_USER);
    }

    @Test
    public void get_data_set_member_names_success() throws Exception {

        List<String> memberList = Arrays.asList("MEMBER1", "MEMBER2");
        String pdsName = "TEST.JCL";

        when(dataSetService.listDataSetMembers(pdsName)).thenReturn(memberList);

        mockMvc.perform(get("/api/v1/datasets/{dsn}/members", pdsName)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().string(JsonUtils.convertToJsonString(memberList)));

        verify(dataSetService, times(1)).listDataSetMembers(pdsName);
        verifyNoMoreInteractions(dataSetService);
    }

    @Test
    public void get_data_set_member_names_empty_body() throws Exception {

        String pdsName = "TEST.JCL";

        when(dataSetService.listDataSetMembers(pdsName)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/datasets/{dsn}/members", pdsName)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().string("[]"));

        verify(dataSetService, times(1)).listDataSetMembers(pdsName);
        verifyNoMoreInteractions(dataSetService);
    }

    @Test
    public void get_data_set_member_names_with_exception_should_be_converted_to_error_message() throws Exception {

        String invalidPdsName = "TEST.JCL";

        String errorMessage = MessageFormat.format("No partitioned data set {0} was found", invalidPdsName);
        ApiError expectedError = ApiError.builder().message(errorMessage).status(HttpStatus.BAD_REQUEST).build();

        when(dataSetService.listDataSetMembers(invalidPdsName)).thenThrow(new ZoweApiErrorException(expectedError));

        mockMvc.perform(get("/api/v1/datasets/{dsn}/members", invalidPdsName))
                .andExpect(jsonPath("$.status").value(expectedError.getStatus().name()))
                .andExpect(jsonPath("$.message").value(errorMessage));

        verify(dataSetService, times(1)).listDataSetMembers(invalidPdsName);
        verifyNoMoreInteractions(dataSetService);
    }

    // TODO LATER - validation dataset names and add getMember validation test

//    @Test
//    public void test_get_data_set_names_success() throws Exception {
//
//        List<String> dataSetNameList = Arrays.asList("TEST", "TEST2");
//        String expectedJsonString = JsonUtils.convertToJsonString(dataSetNameList);
//        String filter = "TEST";
//
//        when(dataSetService.listDataSetNames(filter)).thenReturn(dataSetNameList);
//
//        MvcResult result = mockMvc.perform(get("/api/datasets/{filter}", filter)).andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andReturn();
//
//        verify(dataSetService, times(1)).listDataSetNames(anyString());
//        verifyNoMoreInteractions(dataSetService);
//        JSONAssert.assertEquals(expectedJsonString, result.getResponse().getContentAsString(), false);
//    }
//
//    @Test
//    public void test_get_data_set_names_empty_body() throws Exception {
//
//        String dummy = "junk";
//
//        when(dataSetService.listDataSetNames(anyString())).thenReturn(Collections.emptyList());
//
//        MvcResult result = mockMvc.perform(get("/api/datasets/{filter}", dummy)).andExpect(status().isOk()).andReturn();
//
//        verify(dataSetService, times(1)).listDataSetNames(anyString());
//        verifyNoMoreInteractions(dataSetService);
//        JSONAssert.assertEquals("[]", result.getResponse().getContentAsString(), false);
//    }
//

//
//    @Test
//    public void test_post_new_data_set_returns_location() throws Exception {
//
//        String dataSetName = "DSNAME";
//
//        DataSetCreateRequest attributes = DataSetCreateRequest.createBuilder().build();
//        when(dataSetService.createDataSet(attributes)).thenReturn(dataSetName);
//
//        String dataSetRequest = JsonUtils.convertToJsonString(attributes);
//        this.mockMvc.perform(post("/api/datasets").contentType(MediaType.APPLICATION_JSON).content(dataSetRequest))
//                .andExpect(status().isCreated())
//                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/api/datasets/" + dataSetName));
//
//        when(dataSetService.listDataSetMembers(anyString())).thenReturn(Collections.emptyList());
//
//        verify(dataSetService, times(1)).createDataSet(attributes);
//        verifyNoMoreInteractions(dataSetService);
//    }
//
//    @Test
//    public void test_delete_calls_service_properly() throws Exception {
//        String dummy = "junk";
//
//        mockMvc.perform(delete("/api/datasets/{dsn}", dummy)).andExpect(status().isNoContent()).andDo(print());
//
//        verify(dataSetService, times(1)).deleteDataSet(dummy);
//        verifyNoMoreInteractions(dataSetService);
//    }

}
