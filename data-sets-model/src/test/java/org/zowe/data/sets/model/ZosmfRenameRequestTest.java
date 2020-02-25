package org.zowe.data.sets.model;

import org.junit.Test;
import org.zowe.data.sets.model.ZosmfRenameRequest;

import static org.junit.Assert.assertEquals;

public class ZosmfRenameRequestTest {
    
    @Test
    public void test_ZosmfRenameRequest_model() throws Exception {
        String dsn = "TEST.JCL"; 
        String withoutMember = dsn;
        
        String withoutMemberReqString = String.format("{\"request\":\"rename\",\"from-dataset\":{\"dsn\":\"%s\"}}", dsn);
        
        ZosmfRenameRequest zosmfRenameReq = ZosmfRenameRequest.createFromDataSetRenameRequest(withoutMember);
        assertEquals(dsn, zosmfRenameReq.getDsn());
        assertEquals("", zosmfRenameReq.getMember());
        assertEquals(withoutMemberReqString, zosmfRenameReq.buildJson().toString());
        assertEquals(withoutMemberReqString, zosmfRenameReq.toString());
        
        String member = "OLD";
        String withMember = String.format("%s(%s)", dsn, member);
        String withtMemberReqString = String.format("{\"request\":\"rename\",\"from-dataset\":{\"dsn\":\"%s\",\"member\":\"%s\"}}", dsn, member);
        
        zosmfRenameReq = ZosmfRenameRequest.createFromDataSetRenameRequest(withMember);
        assertEquals(dsn, zosmfRenameReq.getDsn());
        assertEquals(member, zosmfRenameReq.getMember());
        assertEquals(withtMemberReqString, zosmfRenameReq.buildJson().toString());
        assertEquals(withtMemberReqString, zosmfRenameReq.toString());
    }

}
