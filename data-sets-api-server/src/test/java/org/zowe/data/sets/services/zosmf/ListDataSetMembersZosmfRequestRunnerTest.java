/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019, 2020
 */

package org.zowe.data.sets.services.zosmf;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.junit.Test;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.model.ItemsWrapper;
import org.zowe.api.common.test.services.zosmf.AbstractZosmfRequestRunnerTest;
import org.zowe.data.sets.exceptions.UnauthorisedDataSetException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ListDataSetMembersZosmfRequestRunnerTest extends AbstractZosmfRequestRunnerTest {

    @Test
    public void list_member_names_should_call_zosmf_and_parse_response_correctly() throws Exception {
        List<String> members = Arrays.asList("IEFBR14", "JOB1DD");
        ItemsWrapper<String> expected = new ItemsWrapper<>(members);
        String dataSetName = "STEVENH.TEST.JCL";

        mockJsonResponse(HttpStatus.SC_OK, loadTestFile("zosmf_getMembers.json"));
        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/ds/%s/member", dataSetName));
        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        assertEquals(expected, new ListDataSetMembersZosmfRequestRunner(dataSetName, new ArrayList<>()).run(zosmfConnector));

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
        mockJsonResponse(statusCode, loadTestFile(file));

        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/ds/%s/member", pdsName));

        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        shouldThrow(expectedException, () -> new ListDataSetMembersZosmfRequestRunner(pdsName, new ArrayList<>()).run(zosmfConnector));
        verifyInteractions(requestBuilder);
    }
}
