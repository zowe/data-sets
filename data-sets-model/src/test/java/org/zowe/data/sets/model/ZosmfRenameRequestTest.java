/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2020
 */

package org.zowe.data.sets.model;

import org.junit.Test;

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
