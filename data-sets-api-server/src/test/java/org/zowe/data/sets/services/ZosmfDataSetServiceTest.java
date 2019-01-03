/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */
package org.zowe.data.sets.services;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.test.ZoweApiTest;
import org.zowe.api.common.utils.JsonUtils;
import org.zowe.api.common.utils.ResponseUtils;
import org.zowe.data.sets.exceptions.DataSetAlreadyExists;
import org.zowe.data.sets.exceptions.UnauthorisedDataSetException;
import org.zowe.data.sets.model.AllocationUnitType;
import org.zowe.data.sets.model.DataSetCreateRequest;
import org.zowe.data.sets.model.DataSetOrganisationType;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ResponseUtils.class, ZosmfDataSetService.class, RequestBuilder.class, JsonUtils.class,
        ContentType.class })
public class ZosmfDataSetServiceTest extends ZoweApiTest {

    private static final String BASE_URL = "https://dummy.com/zosmf/";

    @Mock
    ZosmfConnector zosmfConnector;

    ZosmfDataSetService dataService;

    @Before
    public void setUp() throws Exception {
        dataService = new ZosmfDataSetService();
        dataService.zosmfConnector = zosmfConnector;
        when(zosmfConnector.getFullUrl(anyString())).thenAnswer(new org.mockito.stubbing.Answer<String>() {
            @Override
            public String answer(org.mockito.invocation.InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return BASE_URL + (String) args[0];
            }
        });
    }

    @Test
    public void list_member_names_should_call_zosmf_and_parse_response_correctly() throws Exception {
        List<String> expected = Arrays.asList("IEFBR14", "JOB1DD");
        String dataSetName = "STEVENH.TEST.JCL";

        HttpResponse response = mockJsonResponse(HttpStatus.SC_OK, loadTestFile("zosmf_getMembers.json"));
        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/ds/%s/member", dataSetName));
        when(zosmfConnector.request(requestBuilder)).thenReturn(response);

        assertEquals(expected, dataService.listDataSetMembers(dataSetName));

        verifyInteractions(requestBuilder);
    }

    @Test
    public void list_member_names_for_unauthorised_user_throws_correct_error() throws Exception {
        String dataSetName = "STEVENH.TEST.JCL";

        Exception expectedException = new UnauthorisedDataSetException(dataSetName);

        checkGetPdsMemberNameExceptionAndVerify(dataSetName, expectedException, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "zosmf_getMembersUnauthorised.json");
    }

    @Test
    public void list_member_names_for_non_existing_pds_throws_correct_error() throws Exception {
        String dataSetName = "STEVENH.TEST.JCL";

        Exception expectedException = new DataSetNotFoundException(dataSetName);
        checkGetPdsMemberNameExceptionAndVerify(dataSetName, expectedException, HttpStatus.SC_NOT_FOUND,
                "getMembers_pdsNotFound.json");
    }

    private void checkGetPdsMemberNameExceptionAndVerify(String pdsName, Exception expectedException, int statusCode,
            String file) throws IOException, Exception {
        HttpResponse response = mockJsonResponse(statusCode, loadTestFile(file));

        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/ds/%s/member", pdsName));

        when(zosmfConnector.request(requestBuilder)).thenReturn(response);

        shouldThrow(expectedException, () -> dataService.listDataSetMembers(pdsName));
        verifyInteractions(requestBuilder);
    }

//    @Test
//    public void list_data_set_names_should_call_zosmf_and_parse_response_correctly() throws Exception {
//        List<String> expected = Arrays.asList("STEVENH.DEMO.COBOL", "STEVENH.PDSE", "STEVENH.REXXSDS", "STEVENH.VSAM",
//                "STEVENH.VSAM.DATA", "STEVENH.VSAM.INDEX");
//        String filter = "STEVENH";
//
//        HttpResponse response = mockResponse(HttpStatus.SC_OK, loadTestFile("zosmf_getDataSets.json"));
//        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/ds?dslevel=%s", filter));
//        when(zosmfConnector.request(requestBuilder)).thenReturn(response);
//
//        assertEquals(expected, dataService.listDataSetNames(filter));
//        verify(zosmfConnector, times(1)).request(requestBuilder);
//        verifyNoMoreInteractions(zosmfConnector);
//    }

//    @Test
//    public void list_member_names_should_call_zosmf_and_parse_response_correctly() throws Exception {
//        List<String> expected = Arrays.asList("IEFBR14", "JOB1DD");
//        String dataSetName = "STEVENH.TEST.JCL";
//
//        HttpResponse response = mockResponse(HttpStatus.SC_OK, loadTestFile("zosmf_getMembers.json"));
//        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/ds/%s/member", dataSetName));
//        when(zosmfConnector.request(requestBuilder)).thenReturn(response);
//
//        assertEquals(expected, dataService.listDataSetMembers(dataSetName));
//
//        verify(zosmfConnector, times(1)).request(requestBuilder);
//        verifyNoMoreInteractions(zosmfConnector);
//    }

//    @Test
//    public void get_data_set_attributes_should_call_zosmf_and_parse_response_correctly() throws Exception {
//
//        DataSetCreateRequest cobolCreate = DataSetCreateRequest.createBuilder().blksize(133)
//                .dsorg(DataSetOrganisationType.PO).lrecl(133).recfm("FB").alcunit(AllocationUnitType.TRACK)
//                .volser("P4P020").build();
//        // DataSetAttributes2 cobol =
//        // DataSetAttributes2.attributesBuilder().blksize(133).dsorg(DataSetOrganisationType.PO)
//        // .lrecl(133).recfm("FB").alcunit(AllocationUnitType.TRACK).volser("P4P020").build();
//        DataSetAttributes rexx = DataSetAttributes.builder().blksize(120).dsorg(DataSetOrganisationType.PS).lrecl(120)
//                .recfm("FB").alcunit(AllocationUnitType.TRACK).volser("P4P023").build();
//        DataSetAttributes vsam = DataSetAttributes.builder().build();
//
//        List<String> expected = Arrays.asList("STEVENH.DEMO.COBOL", "STEVENH.PDSE", "STEVENH.REXXSDS", "STEVENH.VSAM",
//                "STEVENH.VSAM.DATA", "STEVENH.VSAM.INDEX");
//        String filter = "STEVENH";
//
//        HttpResponse response = mockResponse(HttpStatus.SC_OK, loadTestFile("zosmf_getDataSets.json"));
//        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/ds?dslevel=%s", filter));
//        when(zosmfConnector.request(requestBuilder)).thenReturn(response);
//
//        assertEquals(expected, dataService.listDataSetNames(filter));
//        verify(zosmfConnector, times(1)).request(requestBuilder);
//        verifyNoMoreInteractions(zosmfConnector);
//    }
//    
//    @Test
//    // SJH: If zosmf requests end with .* you don't get back any attributes
//    public void test_if_filter_ends_in_period_asterisk_we_strip_it() throws Exception {
//        String filter = "STEVENH";
//
//        HttpResponse response = mockResponse(HttpStatus.SC_OK, "{\"items\":[]}");
//        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/ds?dslevel=%s", filter));
//        when(zosmfConnector.request(requestBuilder)).thenReturn(response);
//
//        dataService.listDataSetNames(filter + ".*");
//        verify(zosmfConnector, times(1)).request(requestBuilder);
//        verifyNoMoreInteractions(zosmfConnector);
//    }

    // TODO - Add tests for dsnType = library for zosmf 2.3 - what else fails on 2.2?

    @Test
    public void create_example_sds_dataset_should_transform_request_to_zosmf() throws Exception {
        String json = "{\"volser\":\"zmf046\",\"unit\":\"3390\",\"dsorg\":\"PO\",\"primary\":10,\"secondary\":5,\""
                + "dirblk\":10,\"avgblk\":500,\"recfm\":\"FB\",\"blksize\":400,\"lrecl\":80,\"alcunit\":\"TRK\"}";
        JsonObject zosmfRequest = JsonUtils.readAsJsonElement(json).getAsJsonObject();

        create_dataset_and_verify("STEVENH.TEST.SDS", createBaseRequest().dsorg(DataSetOrganisationType.PO),
                zosmfRequest);
    }

    @Test
    public void create_example_pds_dataset_should_transform_request_to_zosmf() throws Exception {
        String json = "{\"volser\":\"zmf046\",\"unit\":\"3390\",\"dsorg\":\"PS\",\"primary\":10,\"secondary\":5,\""
                + "dirblk\":10,\"avgblk\":500,\"recfm\":\"FB\",\"blksize\":400,\"lrecl\":80,\"alcunit\":\"TRK\"}";
        JsonObject zosmfRequest = JsonUtils.readAsJsonElement(json).getAsJsonObject();

        create_dataset_and_verify("STEVENH.TEST.PDS", createBaseRequest().dsorg(DataSetOrganisationType.PS),
                zosmfRequest);
    }

    @Test
    public void create_example_pds_dataset_with_cylinders_should_transform_request_to_zosmf() throws Exception {
        int primary = 300;
        int secondary = 150;
        int dirblk = 20;
        int lrecl = 133;
        int blksize = 0;
        String recfm = "VB";

        String json = "{\"volser\":\"zmf046\",\"unit\":\"3390\",\"dsorg\":\"PS\",\"primary\":" + primary
                + ",\"secondary\":" + secondary + ",\"" + "dirblk\":" + dirblk + ",\"avgblk\":500,\"recfm\":\"" + recfm
                + "\",\"blksize\":" + blksize + ",\"lrecl\":" + lrecl + ",\"alcunit\":\"CYL\"}";
        JsonObject zosmfRequest = JsonUtils.readAsJsonElement(json).getAsJsonObject();

        create_dataset_and_verify("STEVENH.TEST.PDS",
                createBaseRequest().dsorg(DataSetOrganisationType.PS).recfm(recfm).primary(primary).secondary(secondary)
                        .dirblk(dirblk).blksize(blksize).lrecl(lrecl).alcunit(AllocationUnitType.CYLINDER),
                zosmfRequest);
    }

    @Test
    public void create_example_pds_dataset_blocks_fails() throws Exception {
        String json = "{\"volser\":\"zmf046\",\"unit\":\"3390\",\"dsorg\":\"PS\",\"primary\":10,\"secondary\":5,\""
                + "dirblk\":10,\"avgblk\":500,\"recfm\":\"FB\",\"blksize\":400,\"lrecl\":80,\"alcunit\":\"TRK\"}";
        JsonObject zosmfRequest = JsonUtils.readAsJsonElement(json).getAsJsonObject();

        Exception expected = new IllegalArgumentException(
                "Creating data sets with a z/OS MF connector only supports allocation unit type of track and cylinder");
        shouldThrow(expected, () -> create_dataset_and_verify("STEVENH.TEST.PDS",
                createBaseRequest().dsorg(DataSetOrganisationType.PS).alcunit(AllocationUnitType.BLOCK), zosmfRequest));
    }

    private void create_dataset_and_verify(String dataSetName,
            DataSetCreateRequest.DataSetCreateRequestBuilder requestBuilder, JsonObject zosmfRequest)
            throws IOException, ClientProtocolException, Exception {
        DataSetCreateRequest request = requestBuilder.name(dataSetName).build();

        HttpResponse response = mockResponse(HttpStatus.SC_CREATED);

        RequestBuilder builder = mockPostBuilder(String.format("restfiles/ds/%s", dataSetName), zosmfRequest);

        when(zosmfConnector.request(builder)).thenReturn(response);

        String dataSetCreated = dataService.createDataSet(request);
        assertEquals(dataSetName, dataSetCreated);

        verifyInteractions(builder);
    }

    @Test
    public void create_data_set_which_already_exists_throws_correct_error() throws Exception {
        String dataSetName = "STEVENH.EXISTS";

        Exception expectedException = new DataSetAlreadyExists(dataSetName);
        checkCreateDataSetExceptionAndVerify(dataSetName, expectedException, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "createDataSet_exists.json");
    }

    private void checkCreateDataSetExceptionAndVerify(String dataSetName, Exception expectedException, int statusCode,
            String file) throws IOException, Exception {

        String json = "{\"volser\":\"zmf046\",\"unit\":\"3390\",\"dsorg\":\"PS\",\"primary\":10,\"secondary\":5,\""
                + "dirblk\":10,\"avgblk\":500,\"recfm\":\"FB\",\"blksize\":400,\"lrecl\":80,\"alcunit\":\"TRK\"}";
        JsonObject zosmfRequest = JsonUtils.readAsJsonElement(json).getAsJsonObject();
        DataSetCreateRequest request = createBaseRequest().dsorg(DataSetOrganisationType.PS).name(dataSetName).build();

        HttpResponse response = mockJsonResponse(statusCode, loadTestFile(file));
        RequestBuilder builder = mockPostBuilder(String.format("restfiles/ds/%s", dataSetName), zosmfRequest);

        when(zosmfConnector.request(builder)).thenReturn(response);

        shouldThrow(expectedException, () -> dataService.createDataSet(request));
        verifyInteractions(builder);
    }

    private DataSetCreateRequest.DataSetCreateRequestBuilder createBaseRequest() {
        return DataSetCreateRequest.builder().volser("zmf046").unit("3390").alcunit(AllocationUnitType.TRACK)
                .primary(10).secondary(5).recfm("FB").blksize(400).lrecl(80).dirblk(10).avgblk(500);
    }

//    @Test
//    public void delete_data_set_should_call_zosmf_correctly() throws Exception {
//        String dataSetName = "STEVENH.TEST.JCL";
//        HttpResponse response = mockResponse(HttpStatus.SC_NO_CONTENT);
//        RequestBuilder builder = mockDeleteBuilder(String.format("restfiles/ds/%s", dataSetName));
//
//        when(zosmfConnector.request(builder)).thenReturn(response);
//
//        dataService.deleteDataSet(dataSetName);
//
//        verify(zosmfConnector, times(1)).request(builder);
//        verifyNoMoreInteractions(zosmfConnector);
//    }

    // TODO - refactor with datasets
    private void verifyInteractions(RequestBuilder requestBuilder) throws IOException {
        verify(zosmfConnector, times(1)).request(requestBuilder);
        verify(zosmfConnector, times(1)).getFullUrl(anyString());
        verifyNoMoreInteractions(zosmfConnector);
    }

    private RequestBuilder mockGetBuilder(String relativeUri) {
        RequestBuilder builder = mock(RequestBuilder.class);
        mockStatic(RequestBuilder.class);
        when(RequestBuilder.get(BASE_URL + relativeUri)).thenReturn(builder);
        return builder;
    }

    private RequestBuilder mockDeleteBuilder(String relativeUri) {
        RequestBuilder builder = mock(RequestBuilder.class);
        mockStatic(RequestBuilder.class);
        when(RequestBuilder.delete(BASE_URL + relativeUri)).thenReturn(builder);
        return builder;
    }

    private RequestBuilder mockPutBuilder(String relativeUri, String string) throws Exception {
        StringEntity stringEntity = mock(StringEntity.class);
        PowerMockito.whenNew(StringEntity.class).withArguments(string).thenReturn(stringEntity);
        return mockPutBuilder(relativeUri, stringEntity);
    }

    private RequestBuilder mockPutBuilder(String relativeUri, JsonObject json) throws Exception {
        StringEntity stringEntity = mock(StringEntity.class);
        PowerMockito.whenNew(StringEntity.class).withArguments(json.toString(), ContentType.APPLICATION_JSON)
                .thenReturn(stringEntity);

        return mockPutBuilder(relativeUri, stringEntity);
    }

    private RequestBuilder mockPutBuilder(String relativeUri, StringEntity stringEntity) throws Exception {
        RequestBuilder builder = mock(RequestBuilder.class);

        mockStatic(RequestBuilder.class);
        when(RequestBuilder.put(BASE_URL + relativeUri)).thenReturn(builder);
        when(builder.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")).thenReturn(builder);
        when(builder.setEntity(stringEntity)).thenReturn(builder);
        return builder;
    }

    private RequestBuilder mockPostBuilder(String relativeUri, String string) throws Exception {
        StringEntity stringEntity = mock(StringEntity.class);
        PowerMockito.whenNew(StringEntity.class).withArguments(string).thenReturn(stringEntity);
        return mockPostBuilder(relativeUri, stringEntity);
    }

    private RequestBuilder mockPostBuilder(String relativeUri, JsonObject json) throws Exception {
        StringEntity stringEntity = mock(StringEntity.class);
        PowerMockito.whenNew(StringEntity.class).withArguments(json.toString(), ContentType.APPLICATION_JSON)
                .thenReturn(stringEntity);

        return mockPostBuilder(relativeUri, stringEntity);
    }

    private RequestBuilder mockPostBuilder(String relativeUri, StringEntity stringEntity) throws Exception {
        RequestBuilder builder = mock(RequestBuilder.class);

        mockStatic(RequestBuilder.class);
        when(RequestBuilder.post(BASE_URL + relativeUri)).thenReturn(builder);
        when(builder.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")).thenReturn(builder);
        when(builder.setEntity(stringEntity)).thenReturn(builder);
        return builder;
    }

    private HttpResponse mockJsonResponse(int statusCode, String jsonString) throws IOException {

        HttpEntity entity = new StringEntity(jsonString);
        HttpResponse response = mockResponse(statusCode);
        when(response.getEntity()).thenReturn(entity);

        JsonElement json = new Gson().fromJson(jsonString, JsonElement.class);
        when(ResponseUtils.getEntityAsJson(response)).thenReturn(json);

        ContentType contentType = mock(ContentType.class);
        mockStatic(ContentType.class);
        when(ContentType.get(entity)).thenReturn(contentType);
        when(contentType.getMimeType()).thenReturn(ContentType.APPLICATION_JSON.getMimeType());

        if (json.isJsonArray()) {
            when(ResponseUtils.getEntityAsJsonArray(response)).thenReturn(json.getAsJsonArray());
        } else if (json.isJsonObject()) {
            when(ResponseUtils.getEntityAsJsonObject(response)).thenReturn(json.getAsJsonObject());
        }

        return response;
    }

    private HttpResponse mockTextResponse(int statusCode, String text) throws IOException {

        HttpEntity entity = new StringEntity(text);
        HttpResponse response = mockResponse(statusCode);
        when(response.getEntity()).thenReturn(entity);

        when(ResponseUtils.getEntity(response)).thenReturn(text);

        ContentType contentType = mock(ContentType.class);
        mockStatic(ContentType.class);
        when(ContentType.get(entity)).thenReturn(contentType);
        when(contentType.getMimeType()).thenReturn(ContentType.TEXT_PLAIN.getMimeType());

        return response;
    }

    private HttpResponse mockResponse(int statusCode) throws IOException {
        HttpResponse response = mock(HttpResponse.class);
        mockStatic(ResponseUtils.class);
        when(ResponseUtils.getStatus(response)).thenReturn(statusCode);
        return response;
    }

    public String loadTestFile(String relativePath) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/zosmfResponses/" + relativePath));
        return new String(encoded, Charset.forName("UTF8"));
    }
}
