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
import org.zowe.api.common.model.ItemsWrapper;
import org.zowe.api.common.test.services.zosmf.AbstractZosmfRequestRunnerTest;
import org.zowe.data.sets.model.DataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListDataSetZosmfRequestRunnerTest extends AbstractZosmfRequestRunnerTest {

    @Test
    public void get_data_set_should_call_zosmf_and_parse_response_correctly() throws Exception {

        DataSet stevenh = DataSet.builder().name("STEVENH").migrated(false).build();

        DataSet cobol = DataSet.builder().name("STEVENH.DEMO.COBOL").migrated(false).build();

        DataSet jcl = DataSet.builder().name("STEVENH.DEMO.JCL").migrated(false).build();

        DataSet migrated = DataSet.builder().name("STEVENH.DEMO.MIGRATED").migrated(true).build();

        DataSet sds = DataSet.builder().name("STEVENH.USER.LOG").migrated(false).build();

        DataSet vsam = DataSet.builder().name("STEVENH.VSAM").migrated(false).build();

        DataSet vsamData = DataSet.builder().name("STEVENH.VSAM.DATA").migrated(false).build();

        DataSet vsamIndex = DataSet.builder().name("STEVENH.VSAM.INDEX").migrated(false).build();

        List<DataSet> dataSets = Arrays.asList(stevenh, cobol, jcl, migrated, sds, vsam, vsamData, vsamIndex);
        ItemsWrapper<DataSet> expected = new ItemsWrapper<>(dataSets);
        String filter = "STEVENH*";

        mockJsonResponse(HttpStatus.SC_OK, loadTestFile("getDataSets.json"));
        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/ds?dslevel=%s", filter));
        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        ItemsWrapper<DataSet> actual = new ListDataSetsZosmfRequestRunner(filter, new ArrayList<>()).run(zosmfConnector); 
        assertEquals(expected, actual);

        verify(requestBuilder).addHeader("X-IBM-Attributes", "dsname");
        verifyInteractions(requestBuilder, true);
    }

    @Test
    public void get_data_set_no_results_should_call_zosmf_and_parse_response_correctly() throws Exception {
        ItemsWrapper<DataSet> expected = new ItemsWrapper<>(Collections.emptyList());
        String filter = "STEVENH*";

        mockJsonResponse(HttpStatus.SC_OK, loadTestFile("getDataSets_noResults.json"));
        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/ds?dslevel=%s", filter));
        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        assertEquals(expected, new ListDataSetsZosmfRequestRunner(filter, new ArrayList<>()).run(zosmfConnector));

        verify(requestBuilder).addHeader("X-IBM-Attributes", "dsname");
        verifyInteractions(requestBuilder, true);
    }

    // TODO - error tests get datasets once we can work out what they are
}
