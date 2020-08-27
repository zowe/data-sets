/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2020
 */
package org.zowe.data.sets.services.zosmf;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.client.methods.RequestBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zowe.api.common.errors.ApiError;
import org.zowe.api.common.exceptions.HtmlEscapedZoweApiRestException;
import org.zowe.api.common.exceptions.ZoweApiErrorException;
import org.zowe.api.common.test.services.zosmf.AbstractZosmfRequestRunnerTest;
import org.zowe.data.sets.model.DataSetRenameRequest;

import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PutDataSetRenameZosmfRequestRunner.class })
public class PutDataSetRenameZosmfRequestRunnerTest extends AbstractZosmfRequestRunnerTest {
    
    public static class TestCase {
        int expectedStatus;
        JsonObject expectedResponse;
        String newName;
        String oldName;
        
        int zosmfStatus;
        String zosmfResponse;
        JsonObject zosmfBody;
        String zosmfResource;
    }
    
    public static class TestObj {
        DataSetRenameRequest request;
        RequestBuilder requestBuilder;
        ZoweApiErrorException expectedException;
    }
    
    @Test
    public void put_rename_should_call_zosmf_and_parse_response_correctly() throws Exception {
        String dsn = "TEST.JCL";
        String oldName = String.format("%s(OLD)",dsn);
        String newName = String.format("%s(NEW)",dsn);
        String jsonStr = String.format("{\"request\":\"rename\", \"from-dataset\":{\"dsn\":\"%s\", \"member\":\"%s\"} }", dsn, "OLD");
        
        JsonParser parser = new JsonParser();
        JsonObject requestBody = (JsonObject)parser. parse(jsonStr);
        
        mockResponseCache(org.apache.http.HttpStatus.SC_NO_CONTENT);
        DataSetRenameRequest request = DataSetRenameRequest.builder().newName(newName).build();
        RequestBuilder requestBuilder = mockPutBuilder(String.format("restfiles/ds/%s", newName),requestBody);
        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);
        
        new PutDataSetRenameZosmfRequestRunner(oldName, request, new ArrayList<>()).run(zosmfConnector); 
        verifyInteractions(requestBuilder);
    }
    
    
    @Test
    public void fail_rename_data_set_invalid_member_name() throws Exception {
        TestCase tc = loadTestCase("renameDataSet_memberInvalidName.json");
        TestObj tObj = setUpTest(tc);
        shouldThrow(tObj.expectedException, () -> new PutDataSetRenameZosmfRequestRunner(tc.oldName,tObj.request, new ArrayList<>()).run(zosmfConnector));
        verifyInteractions(tObj.requestBuilder);
    }
    
    @Test
    public void fail_rename_data_set_invalid_data_set_name() throws Exception {
        TestCase tc = loadTestCase("renameDataSet_InvalidDataSetName.json");
        TestObj tObj = setUpTest(tc);
        shouldThrow(tObj.expectedException, () -> new PutDataSetRenameZosmfRequestRunner(tc.oldName,tObj.request, new ArrayList<>()).run(zosmfConnector));
        verifyInteractions(tObj.requestBuilder);
    }
    
    @Test
    public void fail_rename_data_set_member_doesnt_exist() throws Exception {
        TestCase tc = loadTestCase("renameDataSet_memberDoesntExist.json");
        TestObj tObj = setUpTest(tc);
        shouldThrow(tObj.expectedException, () -> new PutDataSetRenameZosmfRequestRunner(tc.oldName,tObj.request, new ArrayList<>()).run(zosmfConnector));
        verifyInteractions(tObj.requestBuilder);
    }
    
    @Test
    public void fail_rename_data_set_doesnt_exist() throws Exception {
        TestCase tc = loadTestCase("renameDataSet_doesntExist.json");
        TestObj tObj = setUpTest(tc);
        shouldThrow(tObj.expectedException, () -> new PutDataSetRenameZosmfRequestRunner(tc.oldName,tObj.request, new ArrayList<>()).run(zosmfConnector));
        verifyInteractions(tObj.requestBuilder);
    }
    
    @Test
    public void fail_rename_data_set_diff_hlq_test_general_exception() throws Exception {
        TestCase tc = loadTestCase("renameDataSet_generalExceptionWithMessage.json");
        TestObj tObj = setUpTest(tc);
        HtmlEscapedZoweApiRestException expectedException = new HtmlEscapedZoweApiRestException(tObj.expectedException.getApiError().getStatus()
                ,tc.expectedResponse.get("message").getAsString());
        shouldThrow(expectedException, () -> new PutDataSetRenameZosmfRequestRunner(tc.oldName,tObj.request, new ArrayList<>()).run(zosmfConnector));
        verifyInteractions(tObj.requestBuilder);
    }

   
    
    
    
    
    private JsonObject loadTestJson(String relativePath) throws IOException {
        String jsonStr =  loadTestFile(relativePath);
        JsonParser parser = new JsonParser();
        return (JsonObject)parser.parse(jsonStr);
    }
    
    private TestCase  loadTestCase(String relativePath) throws IOException {
        JsonObject testJson =  loadTestJson(relativePath);
        TestCase tc = new TestCase();
        
        JsonObject expected = (JsonObject)testJson.get("expected");
        JsonObject zosmf = (JsonObject)testJson.get("zosmf");
        
        tc.oldName = expected.get("oldName").getAsString();
        tc.newName = expected.get("newName").getAsString();
        tc.expectedStatus = expected.get("statusCode").getAsInt();
        tc.expectedResponse = (JsonObject) expected.get("response");
        
        tc.zosmfBody = (JsonObject) zosmf.get("body");
        tc.zosmfResource = zosmf.get("resource").getAsString();
        tc.zosmfResponse = zosmf.get("response").toString();
        tc.zosmfStatus = zosmf.get("statusCode").getAsInt();
        
        return tc;
    }
    
    private TestObj setUpTest(TestCase tc) throws Exception {
        TestObj testObj = new TestObj();
            
        mockJsonResponse(tc.zosmfStatus, tc.zosmfResponse);
        DataSetRenameRequest request = DataSetRenameRequest.builder().newName(tc.newName).build();
        RequestBuilder requestBuilder = mockPutBuilder(tc.zosmfResource,tc.zosmfBody);
        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);
        
        String errorMessage = tc.expectedResponse.get("message").getAsString();
        String errorStatus = tc.expectedResponse.get("status").getAsString();
        ApiError expectedError = ApiError.builder().message(errorMessage).status(org.springframework.http.HttpStatus.valueOf(errorStatus)).build();
        
        testObj.request = request;
        testObj.expectedException = new ZoweApiErrorException(expectedError);
        testObj.requestBuilder = requestBuilder;
        
        return testObj;
    }
    
}
