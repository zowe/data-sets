/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.unix.files.services.zosmf;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.junit.Test;
import org.zowe.data.sets.services.zosmf.AbstractZosmfRequestRunnerTest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class GetUnixFileChtagRunnerTest extends AbstractZosmfRequestRunnerTest{
    
    @Test
    public void get_unix_file_chtag_should_call_zosmf_and_parse_response_correctly() throws Exception {
        String path = "/u/file";
        String codepage = "m IBM-1047    T=off /u/jcain/newFile.txt";
        String zosmfResponse = "{\"stdout\":[\"" + codepage + "\"]}";
        
        mockJsonResponse(HttpStatus.SC_OK, zosmfResponse);
        
        RequestBuilder requestBuilder = mockPutBuilder("restfiles/fs" + path, "{ \"request\": \"chtag\", \"action\": \"list\" }");
        when(zosmfConnector.request(requestBuilder)).thenReturn(response);
        
        assertEquals(codepage, new GetUnixFileChtagRunner(path).run(zosmfConnector));
        
        verifyInteractions(requestBuilder, false);
    }
}
