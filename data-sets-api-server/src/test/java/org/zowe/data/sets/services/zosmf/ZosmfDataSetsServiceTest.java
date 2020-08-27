/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2020
 */
package org.zowe.data.sets.services.zosmf;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zowe.api.common.connectors.zosmf.ZosmfConnectorLtpaAuth;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.model.ItemsWrapper;
import org.zowe.api.common.test.ZoweApiTest;
import org.zowe.data.sets.model.*;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZosmfDataSetServiceV1.class})
public class ZosmfDataSetsServiceTest extends ZoweApiTest {

    @Mock
    ZosmfConnectorLtpaAuth zosmfConnector;

    ZosmfDataSetServiceV1 dataService;

    @Before
    public void setUp() throws Exception {
        dataService = new ZosmfDataSetServiceV1();
        dataService.zosmfConnector = zosmfConnector;
    }

    @Test
    public void testListDataSetMembersRunnerValueCorrectlyReturned() throws Exception {
        String dataSetName = "DATA.SET.NAME";

        List<String> members = Arrays.asList("MEMBER1", "MEMBER2", "MEMBER3");
        ItemsWrapper<String> expected = new ItemsWrapper<>(members);

        ListDataSetMembersZosmfRequestRunner runner = mock(ListDataSetMembersZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenReturn(expected);
        PowerMockito.whenNew(ListDataSetMembersZosmfRequestRunner.class).withArguments(dataSetName, new ArrayList<>()).thenReturn(runner);
        assertEquals(expected, dataService.listDataSetMembers(dataSetName));
    }

    @Test
    public void testListDataSetMembersRunnerExceptionThrown() throws Exception {
        String dataSetName = "DATA.SET.NAME";

        ZoweApiRestException expectedException = new DataSetNotFoundException(dataSetName);

        ListDataSetMembersZosmfRequestRunner runner = mock(ListDataSetMembersZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(ListDataSetMembersZosmfRequestRunner.class).withArguments(dataSetName, new ArrayList<>()).thenReturn(runner);

        shouldThrow(expectedException, () -> dataService.listDataSetMembers(dataSetName));
    }

    @Test
    public void testListDataSetsRunnerValueCorrectlyReturned() throws Exception {
        String filter = "DATA.SET.NAME*";

        DataSet att1 = DataSet.builder().name("dataset1").build();
        DataSet att2 = DataSet.builder().name("dataset2").build();

        ItemsWrapper<DataSet> expected = new ItemsWrapper<>(Arrays.asList(att1, att2));

        ListDataSetsZosmfRequestRunner runner = mock(ListDataSetsZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenReturn(expected);
        PowerMockito.whenNew(ListDataSetsZosmfRequestRunner.class).withArguments(filter, new ArrayList<>()).thenReturn(runner);
        assertEquals(expected, dataService.listDataSets(filter));
    }

    @Test
    public void testListDataSetAttributesRunnerValueCorrectlyReturned() throws Exception {
        String filter = "DATA.SET.NAME*";

        DataSetAttributes att1 = DataSetAttributes.builder().name("dataset1").build();
        DataSetAttributes att2 = DataSetAttributes.builder().name("dataset2").build();

        ItemsWrapper<DataSetAttributes> expected = new ItemsWrapper<>(Arrays.asList(att1, att2));

        ListDataSetsAttributesZosmfRequestRunner runner = mock(ListDataSetsAttributesZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenReturn(expected);
        PowerMockito.whenNew(ListDataSetsAttributesZosmfRequestRunner.class).withArguments(filter, new ArrayList<>()).thenReturn(runner);
        assertEquals(expected, dataService.listDataSetAttributes(filter));
    }

    @Test
    public void testListDataSetsRunnerExceptionThrown() throws Exception {
        String filter = "DATA.SET.NAME";

        ZoweApiRestException expectedException = new DataSetNotFoundException(filter);

        ListDataSetsZosmfRequestRunner runner = mock(ListDataSetsZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(ListDataSetsZosmfRequestRunner.class).withArguments(filter, new ArrayList<>()).thenReturn(runner);

        shouldThrow(expectedException, () -> dataService.listDataSets(filter));
    }

    @Test
    public void testListDataSetAttributesRunnerExceptionThrown() throws Exception {
        String filter = "DATA.SET.NAME";

        ZoweApiRestException expectedException = new DataSetNotFoundException(filter);

        ListDataSetsAttributesZosmfRequestRunner runner = mock(ListDataSetsAttributesZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(ListDataSetsAttributesZosmfRequestRunner.class).withArguments(filter, new ArrayList<>()).thenReturn(runner);

        shouldThrow(expectedException, () -> dataService.listDataSetAttributes(filter));
    }

    @Test
    public void testGetDataSetContentRunnerValueCorrectlyReturned() throws Exception {
        String dataSetName = "DATA.SET.NAME";

        DataSetContentWithEtag expected = new DataSetContentWithEtag(new DataSetContent("record"), "EEEE");

        GetDataSetContentZosmfRequestRunner runner = mock(GetDataSetContentZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenReturn(expected);
        PowerMockito.whenNew(GetDataSetContentZosmfRequestRunner.class).withArguments(dataSetName, new ArrayList<>()).thenReturn(runner);
        assertEquals(expected, dataService.getContent(dataSetName));
    }

    @Test
    public void testGetDataSetContentRunnerExceptionThrown() throws Exception {
        String dataSetName = "DATA.SET.NAME";

        ZoweApiRestException expectedException = new DataSetNotFoundException(dataSetName);

        GetDataSetContentZosmfRequestRunner runner = mock(GetDataSetContentZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(GetDataSetContentZosmfRequestRunner.class).withArguments(dataSetName, new ArrayList<>()).thenReturn(runner);

        shouldThrow(expectedException, () -> dataService.getContent(dataSetName));
    }

    @Test
    public void testPutDataSetContentRunnerValueCorrectlyReturned() throws Exception {
        String dataSetName = "DATA.SET.NAME";
        DataSetContentWithEtag content = new DataSetContentWithEtag(new DataSetContent("record"), "EEEE");

        String etag = "EFEEF";

        PutDataSetContentZosmfRequestRunner runner = mock(PutDataSetContentZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenReturn(etag);
        PowerMockito.whenNew(PutDataSetContentZosmfRequestRunner.class).withArguments(dataSetName, content, new ArrayList<>())
                .thenReturn(runner);
        assertEquals(etag, dataService.putContent(dataSetName, content));
    }

    @Test
    public void testPutDataSetContentRunnerExceptionThrown() throws Exception {
        String dataSetName = "DATA.SET.NAME";
        DataSetContentWithEtag content = new DataSetContentWithEtag(new DataSetContent("record"), "EEEE");

        ZoweApiRestException expectedException = new DataSetNotFoundException(dataSetName);

        PutDataSetContentZosmfRequestRunner runner = mock(PutDataSetContentZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(PutDataSetContentZosmfRequestRunner.class).withArguments(dataSetName, content, new ArrayList<>())
                .thenReturn(runner);
        shouldThrow(expectedException, () -> dataService.putContent(dataSetName, content));
    }

    @Test
    public void testCreateDataSetRunnerValueCorrectlyReturned() throws Exception {
        String name = "DATA.SET.NAME";
        DataSetCreateRequest request = DataSetCreateRequest.builder().name(name).build();

        CreateDataSetZosmfRequestRunner runner = mock(CreateDataSetZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenReturn(name);
        PowerMockito.whenNew(CreateDataSetZosmfRequestRunner.class).withArguments(request, new ArrayList<>()).thenReturn(runner);
        assertEquals(name, dataService.createDataSet(request));
    }

    @Test
    public void testCreateDataSetRunnerExceptionThrown() throws Exception {
        String name = "DATA.SET.NAME";
        DataSetCreateRequest request = DataSetCreateRequest.builder().name(name).build();

        ZoweApiRestException expectedException = new DataSetNotFoundException(name);

        CreateDataSetZosmfRequestRunner runner = mock(CreateDataSetZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(CreateDataSetZosmfRequestRunner.class).withArguments(request, new ArrayList<>()).thenReturn(runner);
        shouldThrow(expectedException, () -> dataService.createDataSet(request));
    }

    @Test
    public void testDeleteDataSetRunnerValueCorrectlyReturned() throws Exception {
        String name = "DATA.SET.NAME";

        DeleteDataSetZosmfRequestRunner runner = mock(DeleteDataSetZosmfRequestRunner.class);
        PowerMockito.whenNew(DeleteDataSetZosmfRequestRunner.class).withArguments(name, new ArrayList<>()).thenReturn(runner);
        dataService.deleteDataSet(name);

        verify(runner).run(zosmfConnector);
    }

    @Test
    public void testDeleteDataSetRunnerExceptionThrown() throws Exception {
        String name = "DATA.SET.NAME";

        ZoweApiRestException expectedException = new DataSetNotFoundException(name);

        DeleteDataSetZosmfRequestRunner runner = mock(DeleteDataSetZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(DeleteDataSetZosmfRequestRunner.class).withArguments(name, new ArrayList<>()).thenReturn(runner);
        shouldThrow(expectedException, () -> dataService.deleteDataSet(name));
    }
    
    @Test
    public void testGetIbmHeadersFromRequest() throws Exception {
        List<Header> testHeaders = new ArrayList<Header>();
        testHeaders.add(new BasicHeader("X-IBM-ONE", "test"));
        testHeaders.add(new BasicHeader("X-IBM-TWO", "test2"));
        testHeaders.add(new BasicHeader("X-TEST-TWO", "test3"));

        List<String> headerNames = new ArrayList<String>();
        headerNames.add("X-IBM-ONE");
        headerNames.add("X-IBM-TWO");
        Enumeration<String> enumerationHeaderNames = Collections.enumeration(headerNames); 

        HttpServletRequest request = mock(HttpServletRequest.class);
        dataService.setRequest(request);

        when(request.getHeaderNames()).thenReturn(enumerationHeaderNames);
        request = mockRequestGetHeaders(testHeaders, request);

        List<Header> expectedHeaders = new ArrayList<Header>();
        expectedHeaders.add(new BasicHeader("X-IBM-ONE", "test"));
        expectedHeaders.add(new BasicHeader("X-IBM-TWO", "test2"));
        assertTrue("Actual headers do not match Expected", testHeadersMatch(dataService.getIbmHeadersFromRequest(), expectedHeaders));
    }

    public HttpServletRequest mockRequestGetHeaders(List<Header> headers, HttpServletRequest request) {
        for (Header header : headers) {
            when(request.getHeader(header.getName())).thenReturn(header.getValue());
        }
        return request;
    }

    public boolean testHeadersMatch(List<Header> list, List<Header> expectedHeaders) {
        if (list.size() != expectedHeaders.size()) { return false; } 
        for (int i = 0; i < list.size(); i++) {
            BasicHeader header1 = (BasicHeader) list.get(i);
            BasicHeader header2 = (BasicHeader) expectedHeaders.get(i);
            if (header1.getName() != header2.getName()  || header1.getValue() != header2.getValue()) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void testGetIbmHeadersFromRequestNullRequest() throws Exception {
        assertEquals(dataService.getIbmHeadersFromRequest(), new ArrayList<Header>());
    }
}
