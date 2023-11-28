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
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.util.StringUtils;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.exceptions.PreconditionFailedException;
import org.zowe.api.common.test.services.zosmf.AbstractZosmfRequestRunnerTest;
import org.zowe.api.common.utils.ResponseCache;
import org.zowe.data.sets.exceptions.UnauthorisedDataSetException;
import org.zowe.data.sets.model.DataSetContent;
import org.zowe.data.sets.model.DataSetContentWithEtag;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PutDataSetContentZosmfRequestRunner.class })
public class PutDataSetContentZosmfRequestRunnerTest extends AbstractZosmfRequestRunnerTest {

    @Test
    public void put_content_should_call_zosmf_and_parse_response_correctly() throws Exception {
        putContentTest("");
    }

    @Test
    public void put_content_with_if_match_should_call_zosmf_and_parse_response_correctly() throws Exception {
        putContentTest("anETag");
    }

    private void putContentTest(String eTag) throws Exception {
        String putETag = "2A7F90DCB9C2F4D4A582E36F859AEF";
        String jclString = "//ATLJ0000 JOB (ADL),'ATLAS',MSGCLASS=X,CLASS=A,TIME=1440\n" + "//*        TEST JOB\n"
                + "//UNIT     EXEC PGM=IEFBR14\n";
        DataSetContent content = new DataSetContent(jclString);
        String dataSetName = "STEVENH.TEST.JCL";
        DataSetContentWithEtag request = new DataSetContentWithEtag(content, eTag);

        ResponseCache responseCache = mockResponseCache(HttpStatus.SC_NO_CONTENT);
        Header header = mock(Header.class);
        when(header.getValue()).thenReturn(putETag);
        when(responseCache.getFirstHeader("ETag")).thenReturn(header);
        RequestBuilder requestBuilder = mockPutBuilder(String.format("restfiles/ds/%s", dataSetName), jclString);
        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        assertEquals(putETag, new PutDataSetContentZosmfRequestRunner(dataSetName, request, new ArrayList<>()).run(zosmfConnector));

        verifyInteractions(requestBuilder);
        verify(requestBuilder).addHeader("Content-type", ContentType.TEXT_PLAIN.getMimeType());
        if (StringUtils.hasText(eTag)) {
            verify(requestBuilder).addHeader("If-Match", eTag);
        } else {
            verify(requestBuilder, never()).addHeader("X-IBM-Intrdr-Class", "A");
        }
    }

    @Test
    public void put_content_for_non_existing_member_works() throws Exception {
        String putETag = "2A7F90DCB9C2F4D4A582E36F859AEF";
        String dataSetName = "STEVENH.TEST.JCL(JUNK)";
        String jclString = "//ATLJ0000 JOB (ADL),'ATLAS',MSGCLASS=X,CLASS=A,TIME=1440\n" + "//*        TEST JOB\n"
                + "//UNIT     EXEC PGM=IEFBR14\n";
        DataSetContent content = new DataSetContent(jclString);
        DataSetContentWithEtag request = new DataSetContentWithEtag(content, "");

        ResponseCache responseCache = mockResponseCache(HttpStatus.SC_CREATED);
        Header header = mock(Header.class);
        when(header.getValue()).thenReturn(putETag);
        when(responseCache.getFirstHeader("ETag")).thenReturn(header);
        RequestBuilder requestBuilder = mockPutBuilder(String.format("restfiles/ds/%s", dataSetName), jclString);
        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        assertEquals(putETag, new PutDataSetContentZosmfRequestRunner(dataSetName, request, new ArrayList<>()).run(zosmfConnector));

        verifyInteractions(requestBuilder);
        verify(requestBuilder).addHeader("Content-type", ContentType.TEXT_PLAIN.getMimeType());
    }

    @Test
    public void put_content_for_unauthorised_user_throws_correct_error() throws Exception {
        String dataSetName = "TSTRADM.JCL(JUNK)";

        Exception expectedException = new UnauthorisedDataSetException(dataSetName);

        checkPutContentExceptionAndVerify(dataSetName, expectedException, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "putContent_unauthorised.json");
    }

    @Test
    public void put_content_for_non_existing_sds_throws_correct_error() throws Exception {
        String dataSetName = "STEVENH.TEST";

        Exception expectedException = new DataSetNotFoundException(dataSetName);
        checkPutContentExceptionAndVerify(dataSetName, expectedException, HttpStatus.SC_NOT_FOUND,
                "putContent_noDataSet.json");
    }

    private void checkPutContentExceptionAndVerify(String dataSetName, Exception expectedException, int statusCode,
            String file) throws IOException, Exception {
        mockJsonResponse(statusCode, loadTestFile(file));

        String jclString = "//ATLJ0000 JOB (ADL),'ATLAS',MSGCLASS=X,CLASS=A,TIME=1440\n" + "//*        TEST JOB\n"
                + "//UNIT     EXEC PGM=IEFBR14\n";

        RequestBuilder requestBuilder = mockPutBuilder(String.format("restfiles/ds/%s", dataSetName), jclString);

        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        DataSetContent content = new DataSetContent(jclString);
        DataSetContentWithEtag request = new DataSetContentWithEtag(content, "");
        shouldThrow(expectedException,
                () -> new PutDataSetContentZosmfRequestRunner(dataSetName, request, new ArrayList<>()).run(zosmfConnector));
        verifyInteractions(requestBuilder);
    }

    @Test
    public void put_content_with_invalid_of_match_throws_correct_error() throws Exception {
        String dataSetName = "TSTRADM.JCL(JUNK)";

        Exception expectedException = new PreconditionFailedException(dataSetName);

        mockResponseCache(HttpStatus.SC_PRECONDITION_FAILED);

        String jclString = "//ATLJ0000 JOB (ADL),'ATLAS',MSGCLASS=X,CLASS=A,TIME=1440\n" + "//*        TEST JOB\n"
                + "//UNIT     EXEC PGM=IEFBR14\n";

        RequestBuilder requestBuilder = mockPutBuilder(String.format("restfiles/ds/%s", dataSetName), jclString);

        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        DataSetContent content = new DataSetContent(jclString);
        DataSetContentWithEtag request = new DataSetContentWithEtag(content, "");
        shouldThrow(expectedException,
                () -> new PutDataSetContentZosmfRequestRunner(dataSetName, request, new ArrayList<>()).run(zosmfConnector));
        verifyInteractions(requestBuilder);
    }
}
