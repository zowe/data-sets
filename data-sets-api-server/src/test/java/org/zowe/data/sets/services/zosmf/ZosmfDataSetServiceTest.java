/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */
package org.zowe.data.sets.services.zosmf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zowe.api.common.connectors.zosmf.ZosmfConnector;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.model.ItemsWrapper;
import org.zowe.api.common.test.ZoweApiTest;
import org.zowe.data.sets.model.DataSetAttributes;
import org.zowe.data.sets.model.DataSetContent;
import org.zowe.data.sets.model.DataSetContentWithEtag;
import org.zowe.data.sets.model.DataSetCreateRequest;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ZosmfDataSetService.class })
public class ZosmfDataSetServiceTest extends ZoweApiTest {

    @Mock
    ZosmfConnector zosmfConnector;

    ZosmfDataSetService dataService;

    @Before
    public void setUp() throws Exception {
        dataService = new ZosmfDataSetService();
        dataService.zosmfConnector = zosmfConnector;
    }

    @Test
    public void testListDataSetMembersRunnerValueCorrectlyReturned() throws Exception {
        String dataSetName = "DATA.SET.NAME";

        List<String> members = Arrays.asList("MEMBER1", "MEMBER2", "MEMBER3");
        ItemsWrapper<String> expected = new ItemsWrapper<String>(members);

        ListDataSetMembersZosmfRequestRunner runner = mock(ListDataSetMembersZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenReturn(expected);
        PowerMockito.whenNew(ListDataSetMembersZosmfRequestRunner.class).withArguments(dataSetName).thenReturn(runner);
        assertEquals(expected, dataService.listDataSetMembers(dataSetName));
    }

    @Test
    public void testListDataSetMembersRunnerExceptionThrown() throws Exception {
        String dataSetName = "DATA.SET.NAME";

        ZoweApiRestException expectedException = new DataSetNotFoundException(dataSetName);

        ListDataSetMembersZosmfRequestRunner runner = mock(ListDataSetMembersZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(ListDataSetMembersZosmfRequestRunner.class).withArguments(dataSetName).thenReturn(runner);

        shouldThrow(expectedException, () -> dataService.listDataSetMembers(dataSetName));
    }

    @Test
    public void testListDataSetsRunnerValueCorrectlyReturned() throws Exception {
        String filter = "DATA.SET.NAME*";

        DataSetAttributes att1 = DataSetAttributes.builder().name("dataset1").build();
        DataSetAttributes att2 = DataSetAttributes.builder().name("dataset2").build();

        ItemsWrapper<DataSetAttributes> expected = new ItemsWrapper(Arrays.asList(att1, att2));

        ListDataSetsZosmfRequestRunner runner = mock(ListDataSetsZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenReturn(expected);
        PowerMockito.whenNew(ListDataSetsZosmfRequestRunner.class).withArguments(filter).thenReturn(runner);
        assertEquals(expected, dataService.listDataSets(filter));
    }

    @Test
    public void testListDataSetsRunnerExceptionThrown() throws Exception {
        String filter = "DATA.SET.NAME";

        ZoweApiRestException expectedException = new DataSetNotFoundException(filter);

        ListDataSetsZosmfRequestRunner runner = mock(ListDataSetsZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(ListDataSetsZosmfRequestRunner.class).withArguments(filter).thenReturn(runner);

        shouldThrow(expectedException, () -> dataService.listDataSets(filter));
    }

    @Test
    public void testGetDataSetContentRunnerValueCorrectlyReturned() throws Exception {
        String dataSetName = "DATA.SET.NAME";

        DataSetContentWithEtag expected = new DataSetContentWithEtag(new DataSetContent("record"), "EEEE");

        GetDataSetContentZosmfRequestRunner runner = mock(GetDataSetContentZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenReturn(expected);
        PowerMockito.whenNew(GetDataSetContentZosmfRequestRunner.class).withArguments(dataSetName).thenReturn(runner);
        assertEquals(expected, dataService.getContent(dataSetName));
    }

    @Test
    public void testGetDataSetContentRunnerExceptionThrown() throws Exception {
        String dataSetName = "DATA.SET.NAME";

        ZoweApiRestException expectedException = new DataSetNotFoundException(dataSetName);

        GetDataSetContentZosmfRequestRunner runner = mock(GetDataSetContentZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(GetDataSetContentZosmfRequestRunner.class).withArguments(dataSetName).thenReturn(runner);

        shouldThrow(expectedException, () -> dataService.getContent(dataSetName));
    }

    @Test
    public void testPutDataSetContentRunnerValueCorrectlyReturned() throws Exception {
        String dataSetName = "DATA.SET.NAME";
        DataSetContentWithEtag content = new DataSetContentWithEtag(new DataSetContent("record"), "EEEE");

        String etag = "EFEEF";

        PutDataSetContentZosmfRequestRunner runner = mock(PutDataSetContentZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenReturn(etag);
        PowerMockito.whenNew(PutDataSetContentZosmfRequestRunner.class).withArguments(dataSetName, content)
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
        PowerMockito.whenNew(PutDataSetContentZosmfRequestRunner.class).withArguments(dataSetName, content)
            .thenReturn(runner);
        shouldThrow(expectedException, () -> dataService.putContent(dataSetName, content));
    }

    @Test
    public void testCreateDataSetRunnerValueCorrectlyReturned() throws Exception {
        String name = "DATA.SET.NAME";
        DataSetCreateRequest request = DataSetCreateRequest.builder().name(name).build();

        CreateDataSetZosmfRequestRunner runner = mock(CreateDataSetZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenReturn(name);
        PowerMockito.whenNew(CreateDataSetZosmfRequestRunner.class).withArguments(request).thenReturn(runner);
        assertEquals(name, dataService.createDataSet(request));
    }

    @Test
    public void testCreateDataSetRunnerExceptionThrown() throws Exception {
        String name = "DATA.SET.NAME";
        DataSetCreateRequest request = DataSetCreateRequest.builder().name(name).build();

        ZoweApiRestException expectedException = new DataSetNotFoundException(name);

        CreateDataSetZosmfRequestRunner runner = mock(CreateDataSetZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(CreateDataSetZosmfRequestRunner.class).withArguments(request).thenReturn(runner);
        shouldThrow(expectedException, () -> dataService.createDataSet(request));
    }

    @Test
    public void testDeleteDataSetRunnerValueCorrectlyReturned() throws Exception {
        String name = "DATA.SET.NAME";

        DeleteDataSetZosmfRequestRunner runner = mock(DeleteDataSetZosmfRequestRunner.class);
        PowerMockito.whenNew(DeleteDataSetZosmfRequestRunner.class).withArguments(name).thenReturn(runner);
        dataService.deleteDataSet(name);

        verify(runner).run(zosmfConnector);
    }

    @Test
    public void testDeleteDataSetRunnerExceptionThrown() throws Exception {
        String name = "DATA.SET.NAME";

        ZoweApiRestException expectedException = new DataSetNotFoundException(name);

        DeleteDataSetZosmfRequestRunner runner = mock(DeleteDataSetZosmfRequestRunner.class);
        when(runner.run(zosmfConnector)).thenThrow(expectedException);
        PowerMockito.whenNew(DeleteDataSetZosmfRequestRunner.class).withArguments(name).thenReturn(runner);
        shouldThrow(expectedException, () -> dataService.deleteDataSet(name));
    }
}
