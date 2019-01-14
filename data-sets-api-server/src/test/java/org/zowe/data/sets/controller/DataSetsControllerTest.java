/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.zowe.api.common.errors.ApiError;
import org.zowe.api.common.exceptions.ZoweApiErrorException;
import org.zowe.api.common.exceptions.ZoweRestExceptionHandler;
import org.zowe.api.common.utils.JsonUtils;
import org.zowe.api.common.utils.ZosUtils;
import org.zowe.data.sets.model.AllocationUnitType;
import org.zowe.data.sets.model.DataSetAttributes;
import org.zowe.data.sets.model.DataSetContent;
import org.zowe.data.sets.model.DataSetCreateRequest;
import org.zowe.data.sets.model.DataSetOrganisationType;
import org.zowe.data.sets.services.DataSetService;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
@PrepareForTest({ ZosUtils.class, ServletUriComponentsBuilder.class })
public class DataSetsControllerTest {

    private static final String ENDPOINT_ROOT = "/api/v1/datasets";

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

        mockMvc.perform(get(ENDPOINT_ROOT + "/{dsn}/members", pdsName)).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string(JsonUtils.convertToJsonString(memberList)));

        verify(dataSetService, times(1)).listDataSetMembers(pdsName);
        verifyNoMoreInteractions(dataSetService);
    }

    @Test
    public void get_data_set_member_names_empty_body() throws Exception {

        String pdsName = "TEST.JCL";

        when(dataSetService.listDataSetMembers(pdsName)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(ENDPOINT_ROOT + "/{dsn}/members", pdsName)).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andExpect(content().string("[]"));

        verify(dataSetService, times(1)).listDataSetMembers(pdsName);
        verifyNoMoreInteractions(dataSetService);
    }

    @Test
    public void get_data_set_member_names_with_exception_should_be_converted_to_error_message() throws Exception {

        String invalidPdsName = "TEST.JCL";

        String errorMessage = MessageFormat.format("No partitioned data set {0} was found", invalidPdsName);
        ApiError expectedError = ApiError.builder().message(errorMessage).status(HttpStatus.BAD_REQUEST).build();

        when(dataSetService.listDataSetMembers(invalidPdsName)).thenThrow(new ZoweApiErrorException(expectedError));

        mockMvc.perform(get(ENDPOINT_ROOT + "/{dsn}/members", invalidPdsName))
            .andExpect(status().is(expectedError.getStatus().value()))
            .andExpect(jsonPath("$.status").value(expectedError.getStatus().name()))
            .andExpect(jsonPath("$.message").value(errorMessage));

        verify(dataSetService, times(1)).listDataSetMembers(invalidPdsName);
        verifyNoMoreInteractions(dataSetService);
    }

    @Test
    public void get_data_set_content_success() throws Exception {

        String memberName = "TEST.JCL(MEMBER)";
        DataSetContent expected = new DataSetContent("Test\nFile");

        when(dataSetService.getContent(memberName)).thenReturn(expected);

        mockMvc.perform(get(ENDPOINT_ROOT + "/{dsn}/content", memberName)).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string(JsonUtils.convertToJsonString(expected)));

        verify(dataSetService, times(1)).getContent(memberName);
        verifyNoMoreInteractions(dataSetService);
    }

    @Test
    public void get_data_set_content_with_exception_should_be_converted_to_error_message() throws Exception {

        String invalidPdsName = "TEST.JCL";

        String errorMessage = MessageFormat.format("No data set {0} was found", invalidPdsName);
        ApiError expectedError = ApiError.builder().message(errorMessage).status(HttpStatus.BAD_REQUEST).build();

        when(dataSetService.getContent(invalidPdsName)).thenThrow(new ZoweApiErrorException(expectedError));

        mockMvc.perform(get(ENDPOINT_ROOT + "/{dsn}/content", invalidPdsName))
            .andExpect(status().is(expectedError.getStatus().value()))
            .andExpect(jsonPath("$.status").value(expectedError.getStatus().name()))
            .andExpect(jsonPath("$.message").value(errorMessage));

        verify(dataSetService, times(1)).getContent(invalidPdsName);
        verifyNoMoreInteractions(dataSetService);
    }

    @Test
    public void put_data_set_content_success() throws Exception {

        String memberName = "TEST.JCL(MEMBER)";
        DataSetContent request = new DataSetContent("Test\nFile");

        mockMvc
            .perform(put(ENDPOINT_ROOT + "/{dsn}/content", memberName)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(JsonUtils.convertToJsonString(request)))
            .andExpect(status().isNoContent()).andExpect(content().string(""));

        verify(dataSetService, times(1)).putContent(memberName, request);
        verifyNoMoreInteractions(dataSetService);
    }

    @Test
    public void put_data_set_content_with_exception_should_be_converted_to_error_message() throws Exception {

        String invalidPdsName = "TEST.JCL";
        DataSetContent request = new DataSetContent("Test\nFile");

        String errorMessage = MessageFormat.format("No data set {0} was found", invalidPdsName);
        ApiError expectedError = ApiError.builder().message(errorMessage).status(HttpStatus.BAD_REQUEST).build();

        doThrow(new ZoweApiErrorException(expectedError)).when(dataSetService).putContent(invalidPdsName, request);

        mockMvc
            .perform(put(ENDPOINT_ROOT + "/{dsn}/content", invalidPdsName)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(JsonUtils.convertToJsonString(request)))
            .andExpect(status().is(expectedError.getStatus().value()))
            .andExpect(jsonPath("$.status").value(expectedError.getStatus().name()))
            .andExpect(jsonPath("$.message").value(errorMessage));

        verify(dataSetService, times(1)).putContent(invalidPdsName, request);
        verifyNoMoreInteractions(dataSetService);
    }

    @Test
    public void put_data_set_content_with_wrong_type_converted_to_error_message() throws Exception {

        String invalidPdsName = "TEST.JCL";

        mockMvc.perform(put(ENDPOINT_ROOT + "/{dsn}/content", invalidPdsName)
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content("JUNK")).andExpect(status().isBadRequest());

        verifyNoMoreInteractions(dataSetService);
    }

    // TODO LATER - validation dataset names and add getMember validation test

    @Test
    public void test_get_data_sets_success() throws Exception {

        DataSetAttributes cobol = DataSetAttributes.builder().blockSize(133)
            .dataSetOrganization(DataSetOrganisationType.PO).recordLength(133).recordFormat("FB")
            .allocationUnit(AllocationUnitType.TRACK).volumeSerial("P4P020").build();
        DataSetAttributes rexx = DataSetAttributes.builder().blockSize(120)
            .dataSetOrganization(DataSetOrganisationType.PS).recordLength(120).recordFormat("FB")
            .allocationUnit(AllocationUnitType.TRACK).volumeSerial("P4P023").build();
        DataSetAttributes vsam = DataSetAttributes.builder().build();

        List<DataSetAttributes> dataSetsList = Arrays.asList(cobol, rexx, vsam);
        String filter = "TEST";

        when(dataSetService.listDataSets(filter)).thenReturn(dataSetsList);

        mockMvc.perform(get(ENDPOINT_ROOT + "/{filter}", filter)).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(content().string(JsonUtils.convertToJsonString(dataSetsList)));

        verify(dataSetService, times(1)).listDataSets(filter);
        verifyNoMoreInteractions(dataSetService);
    }

    @Test
    public void test_get_data_sets_empty_body() throws Exception {

        String dummy = "junk";

        when(dataSetService.listDataSets(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get(ENDPOINT_ROOT + "/{filter}", dummy)).andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andExpect(content().string("[]"));

        verify(dataSetService, times(1)).listDataSets(dummy);
        verifyNoMoreInteractions(dataSetService);
    }

    @Test
    public void get_data_sets_with_exception_should_be_converted_to_error_message() throws Exception {

        String invalidPdsName = "TEST.JCL";

        String errorMessage = MessageFormat.format("No partitioned data set {0} was found", invalidPdsName);
        ApiError expectedError = ApiError.builder().message(errorMessage).status(HttpStatus.BAD_REQUEST).build();

        when(dataSetService.listDataSets(invalidPdsName)).thenThrow(new ZoweApiErrorException(expectedError));

        mockMvc.perform(get(ENDPOINT_ROOT + "/{filter}", invalidPdsName))
            .andExpect(status().is(expectedError.getStatus().value()))
            .andExpect(jsonPath("$.status").value(expectedError.getStatus().name()))
            .andExpect(jsonPath("$.message").value(errorMessage));

        verify(dataSetService, times(1)).listDataSets(invalidPdsName);
        verifyNoMoreInteractions(dataSetService);
    }

    // TODO - consider returning model object in response body, like jobs?
    @Test
    public void create_data_set_works_and_returns_location() throws Exception {

        String dataSetName = "DSNAME";

        DataSetCreateRequest request = DataSetCreateRequest.builder().name(dataSetName).build();

        when(dataSetService.createDataSet(request)).thenReturn(dataSetName);

        URI locationUri = new URI("https://dataSetsURI/datasets/" + dataSetName);
        mockDataSetUriConstruction(dataSetName, locationUri);

        mockMvc
            .perform(post(ENDPOINT_ROOT).contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(JsonUtils.convertToJsonString(request)))
            .andExpect(status().isCreated()).andExpect(header().string("Location", locationUri.toString()));

        verify(dataSetService, times(1)).createDataSet(request);
        verifyNoMoreInteractions(dataSetService);
    }

    @Test
    public void test_delete_calls_service_properly() throws Exception {
        String dummy = "junk";

        mockMvc.perform(delete(ENDPOINT_ROOT + "/{dsn}", dummy)).andExpect(status().isNoContent())
            .andExpect(jsonPath("$").doesNotExist());

        verify(dataSetService, times(1)).deleteDataSet(dummy);
        verifyNoMoreInteractions(dataSetService);
    }

    @Test
    public void delete_data_set_with_exception_should_be_converted_to_error_message() throws Exception {
        String dummy = "junk";

        ApiError expectedError = ApiError.builder().message("Delete went wrong").status(HttpStatus.I_AM_A_TEAPOT)
            .build();

        doThrow(new ZoweApiErrorException(expectedError)).when(dataSetService).deleteDataSet(dummy);

        mockMvc.perform(delete(ENDPOINT_ROOT + "/{dsn}", dummy)).andExpect(status().isIAmATeapot())
            .andExpect(jsonPath("$.status").value(expectedError.getStatus().name()))
            .andExpect(jsonPath("$.message").value(expectedError.getMessage()));

        verify(dataSetService, times(1)).deleteDataSet(dummy);
        verifyNoMoreInteractions(dataSetService);
    }

    // TODO MAYBE - can we merge with job?
    private void mockDataSetUriConstruction(String dataSetName, URI uriValue) {
        ServletUriComponentsBuilder servletUriBuilder = mock(ServletUriComponentsBuilder.class);
        PowerMockito.mockStatic(ServletUriComponentsBuilder.class);
        when(ServletUriComponentsBuilder.fromCurrentRequest()).thenReturn(servletUriBuilder);
        UriComponentsBuilder uriBuilder = mock(UriComponentsBuilder.class);
        when(servletUriBuilder.path("/{dataSetName}")).thenReturn(uriBuilder);
        UriComponents uriComponents = mock(UriComponents.class);
        when(uriBuilder.buildAndExpand(dataSetName)).thenReturn(uriComponents);
        when(uriComponents.toUri()).thenReturn(uriValue);
    }

}
