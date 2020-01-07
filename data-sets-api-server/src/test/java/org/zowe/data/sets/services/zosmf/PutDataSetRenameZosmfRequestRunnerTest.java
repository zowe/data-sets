/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.data.sets.services.zosmf;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.client.methods.RequestBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zowe.api.common.test.services.zosmf.AbstractZosmfRequestRunnerTest;
import org.zowe.data.sets.model.DataSetRenameRequest;
import org.zowe.data.sets.model.ZosmfRenameRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PutDataSetRenameZosmfRequestRunner.class })
public class PutDataSetRenameZosmfRequestRunnerTest extends AbstractZosmfRequestRunnerTest {
    
    

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
        when(zosmfConnector.request(requestBuilder)).thenReturn(response);
        
        new PutDataSetRenameZosmfRequestRunner(oldName, request).run(zosmfConnector); 
        verifyInteractions(requestBuilder);
    }

    @Test
    public void rename_name_breakdown() throws Exception {
        String dsn = "TEST.JCL"; 
        String withoutMember = dsn;
        
        String withoutMemberReqString = String.format("{\"request\":\"rename\", \"from-dataset\":{\"dsn\":\"%s\"} }", dsn);
        
        ZosmfRenameRequest zosmfRenameReq = ZosmfRenameRequest.createFromDataSetRenameRequest(withoutMember);
        assertEquals(dsn, zosmfRenameReq.getDsn());
        assertEquals("", zosmfRenameReq.getMember());
        assertEquals(withoutMemberReqString, zosmfRenameReq.toString());
        
        String member = "OLD";
        String withMember = String.format("%s(%s)", dsn, member);
        String withtMemberReqString = String.format("{\"request\":\"rename\", \"from-dataset\":{\"dsn\":\"%s\", \"member\":\"%s\"} }", dsn, member);
        zosmfRenameReq = ZosmfRenameRequest.createFromDataSetRenameRequest(withMember);
        assertEquals(dsn, zosmfRenameReq.getDsn());
        assertEquals(member, zosmfRenameReq.getMember());
        assertEquals(withtMemberReqString, zosmfRenameReq.toString());
    }
    
}
