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

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.junit.Test;
import org.zowe.api.common.model.ItemsWrapper;
import org.zowe.api.common.test.services.zosmf.AbstractZosmfRequestRunnerTest;
import org.zowe.data.sets.model.AllocationUnitType;
import org.zowe.data.sets.model.DataSetAttributes;
import org.zowe.data.sets.model.DataSetOrganisationType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListDataSetAttributesZosmfRequestRunnerTest extends AbstractZosmfRequestRunnerTest {

    @Test
    public void get_data_set_attributes_should_call_zosmf_and_parse_response_correctly() throws Exception {

        DataSetAttributes stevenh = DataSetAttributes.builder().catalogName("ICFCAT.MV3B.MCAT").name("STEVENH")
            .migrated(false).volumeSerial("3BSS01").build();

        DataSetAttributes cobol = DataSetAttributes.builder().blockSize(32718).catalogName("ICFCAT.MV3B.CATALOGA")
            .creationDate("2019/01/09").deviceType("3390").name("STEVENH.DEMO.COBOL").migrated(false)
            .dataSetOrganization(DataSetOrganisationType.PO_E).expirationDate("***None***").recordLength(133)
            .allocationUnit(AllocationUnitType.BLOCK).recordFormat("FBA").allocatedSize(201).used(0)
            .volumeSerial("3BP001").build();

        DataSetAttributes jcl = DataSetAttributes.builder().blockSize(6160).catalogName("ICFCAT.MV3B.CATALOGA")
            .creationDate("2018/12/18").deviceType("3390").name("STEVENH.DEMO.JCL").migrated(false)
            .dataSetOrganization(DataSetOrganisationType.PO).expirationDate("***None***").recordLength(80)
            .allocationUnit(AllocationUnitType.CYLINDER).recordFormat("FB").allocatedSize(15).used(6)
            .volumeSerial("3BP001").build();

        DataSetAttributes migrated = DataSetAttributes.builder().name("STEVENH.DEMO.MIGRATED").migrated(true).build();

        DataSetAttributes sds = DataSetAttributes.builder().blockSize(1500).catalogName("ICFCAT.MV3B.CATALOGA")
            .creationDate("2018/07/25").deviceType("3390").name("STEVENH.USER.LOG").migrated(false)
            .dataSetOrganization(DataSetOrganisationType.PS).expirationDate("***None***").recordLength(150)
            .allocationUnit(AllocationUnitType.TRACK).recordFormat("FB").allocatedSize(1).used(100)
            .volumeSerial("3BP001").build();

        DataSetAttributes vsam = DataSetAttributes.builder().catalogName("ICFCAT.MV3B.CATALOGA").name("STEVENH.VSAM")
            .dataSetOrganization(DataSetOrganisationType.VSAM).migrated(false).build();

        DataSetAttributes vsamData = DataSetAttributes.builder().catalogName("ICFCAT.MV3B.CATALOGA")
            .creationDate("2019/01/09").deviceType("3390").name("STEVENH.VSAM.DATA")
            .dataSetOrganization(DataSetOrganisationType.VSAM).expirationDate("***None***").migrated(false)
            .allocatedSize(45).allocationUnit(AllocationUnitType.CYLINDER).volumeSerial("3BP001").build();

        DataSetAttributes vsamIndex = DataSetAttributes.builder().catalogName("ICFCAT.MV3B.CATALOGA")
            .creationDate("2019/01/09").deviceType("3390").name("STEVENH.VSAM.INDEX").migrated(false)
            .dataSetOrganization(DataSetOrganisationType.VSAM).expirationDate("***None***")
            .allocationUnit(AllocationUnitType.TRACK).allocatedSize(1).volumeSerial("3BP001").build();

        List<DataSetAttributes> dataSets = Arrays.asList(stevenh, cobol, jcl, migrated, sds, vsam, vsamData, vsamIndex);
        ItemsWrapper<DataSetAttributes> expected = new ItemsWrapper<DataSetAttributes>(dataSets);
        String filter = "STEVENH*";

        mockJsonResponse(HttpStatus.SC_OK, loadTestFile("getDataSetsAttributes.json"));
        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/ds?dslevel=%s", filter));
        when(zosmfConnector.request(requestBuilder)).thenReturn(response);

        assertEquals(expected, new ListDataSetsAttributesZosmfRequestRunner(filter).run(zosmfConnector));

        verify(requestBuilder).addHeader("X-IBM-Attributes", "base");
        verifyInteractions(requestBuilder, true);
    }

    @Test
    public void get_data_set_attributes_no_results_should_call_zosmf_and_parse_response_correctly() throws Exception {
        ItemsWrapper<DataSetAttributes> expected = new ItemsWrapper<DataSetAttributes>(Collections.emptyList());
        String filter = "STEVENH*";

        mockJsonResponse(HttpStatus.SC_OK, loadTestFile("getDataSets_noResults.json"));
        RequestBuilder requestBuilder = mockGetBuilder(String.format("restfiles/ds?dslevel=%s", filter));
        when(zosmfConnector.request(requestBuilder)).thenReturn(response);

        assertEquals(expected, new ListDataSetsAttributesZosmfRequestRunner(filter).run(zosmfConnector));

        verify(requestBuilder).addHeader("X-IBM-Attributes", "base");
        verifyInteractions(requestBuilder, true);
    }

    // TODO - error tests get datasets once we can work out what they are
}
