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

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.junit.Test;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.test.services.zosmf.AbstractZosmfRequestRunnerTest;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.data.sets.exceptions.DataSetLockedException;
import org.zowe.data.sets.exceptions.UnauthorisedDataSetException;
import org.zowe.data.sets.model.DataSetContent;
import org.zowe.data.sets.model.DataSetContentWithEtag;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GetDataSetContentZosmfRequestRunnerTest extends AbstractZosmfRequestRunnerTest {

    @Test
    public void get_content_should_call_zosmf_and_parse_response_correctly() throws Exception {
        String headerTag = "2A7F90DCB9C2F4D4A582E36F859AE41F";
        DataSetContent content = new DataSetContent(
                "//ATLJ0000 JOB (ADL),'ATLAS',MSGCLASS=X,CLASS=A,TIME=1440" + System.lineSeparator()
                        + "//*        TEST JOB" + System.lineSeparator() + "//UNIT     EXEC PGM=IEFBR14");
        String dataSetName = "STEVENH.TEST.JCL";

        DataSetContentWithEtag expected = new DataSetContentWithEtag(content, headerTag);

        ResponseCache responseCache = mockTextResponse(HttpStatus.SC_OK, loadTestFile("getContent.json"));

        Header header = mock(Header.class);
        when(header.getValue()).thenReturn(headerTag);
        when(responseCache.getFirstHeader("ETag")).thenReturn(header);

        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/ds/%s", dataSetName));
        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        assertEquals(expected, new GetDataSetContentZosmfRequestRunner(dataSetName, new ArrayList<>()).run(zosmfConnector));

        verifyInteractions(requestBuilder);
        verify(requestBuilder).addHeader("X-IBM-Return-Etag", "true");

    }

    @Test
    public void get_content_should_work_even_if_no_etag_header_zosmf_and_parse_response_correctly() throws Exception {
        DataSetContent content = new DataSetContent(
                "//ATLJ0000 JOB (ADL),'ATLAS',MSGCLASS=X,CLASS=A,TIME=1440" + System.lineSeparator()
                        + "//*        TEST JOB" + System.lineSeparator() + "//UNIT     EXEC PGM=IEFBR14");
        String dataSetName = "STEVENH.TEST.JCL";

        DataSetContentWithEtag expected = new DataSetContentWithEtag(content, null);

        mockTextResponse(HttpStatus.SC_OK, loadTestFile("getContent.json"));

        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/ds/%s", dataSetName));
        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        assertEquals(expected, new GetDataSetContentZosmfRequestRunner(dataSetName, new ArrayList<>()).run(zosmfConnector));

        verifyInteractions(requestBuilder);
        verify(requestBuilder).addHeader("X-IBM-Return-Etag", "true");

    }

    @Test
    public void get_content_for_unauthorised_user_throws_correct_error() throws Exception {
        String dataSetName = "TSTRADM.JCL(JUNK)";

        Exception expectedException = new UnauthorisedDataSetException(dataSetName);

        checkGetContentExceptionAndVerify(dataSetName, expectedException, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "getContent_unauthorised.json");
    }

    @Test
    public void get_content_for_non_existing_sds_throws_correct_error() throws Exception {
        String dataSetName = "STEVENH.TEST";

        Exception expectedException = new DataSetNotFoundException(dataSetName);
        checkGetContentExceptionAndVerify(dataSetName, expectedException, HttpStatus.SC_NOT_FOUND,
                "getContent_noDataSet.json");
    }

    @Test
    public void get_content_for_non_existing_member_throws_correct_error() throws Exception {
        String dataSetName = "STEVENH.TEST.JCL(JUNK)";

        Exception expectedException = new DataSetNotFoundException(dataSetName);
        checkGetContentExceptionAndVerify(dataSetName, expectedException, HttpStatus.SC_NOT_FOUND,
                "getContent_noMember.json");
    }

    @Test
    public void get_content_for_locked_data_set_throws_correct_error() throws Exception {
        String dataSetName = "STEVENH.TEST";
        Exception expectedException = new DataSetLockedException(dataSetName, "MV3B", "STEVENH", "00DE");
        checkGetContentExceptionAndVerify(dataSetName, expectedException, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "dataSet_locked.json");
    }

    private void checkGetContentExceptionAndVerify(String dataSetName, Exception expectedException, int statusCode,
            String file) throws IOException, Exception {
        mockJsonResponse(statusCode, loadTestFile(file));

        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/ds/%s", dataSetName));

        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        shouldThrow(expectedException, () -> new GetDataSetContentZosmfRequestRunner(dataSetName, new ArrayList<>()).run(zosmfConnector));
        verifyInteractions(requestBuilder);
    }
}