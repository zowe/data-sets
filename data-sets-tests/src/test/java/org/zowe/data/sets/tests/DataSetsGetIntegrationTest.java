/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2016, 2019
 */
package org.zowe.data.sets.tests;

import org.apache.http.HttpStatus;
import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Ignore;
import org.junit.Test;
import org.zowe.data.sets.model.DataSetAttributes;
import org.zowe.data.sets.model.DataSetCreateRequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

public class DataSetsGetIntegrationTest extends AbstractDataSetsIntegrationTest {

    @Test
    public void testGetValidDataset() throws Exception {
        String tempDataSet = HLQ + ".TEST.DELETE";
        DataSetCreateRequest pdsRequest = createPdsRequest(tempDataSet);
        createDataSet(pdsRequest);

        try {

            String pattern = "yyyy/MM/dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            String today = simpleDateFormat.format(new Date());

            DataSetAttributes expected = DataSetAttributes.builder().blockSize(pdsRequest.getBlockSize())
                .deviceType("3390").creationDate(today).name(tempDataSet).migrated(false)
                .dataSetOrganization(pdsRequest.getDataSetOrganization()).expirationDate("***None***")
                .recordLength(pdsRequest.getRecordLength()).allocationUnit(pdsRequest.getAllocationUnit())
                .recordFormat(pdsRequest.getRecordFormat()).allocatedSize(10).used(10).build();

            List<DataSetAttributes> actual = getDataSetsDetails(tempDataSet).then().statusCode(HttpStatus.SC_OK).extract()
                .body().jsonPath().getList("", DataSetAttributes.class);

            // We can't tell the value of some attributes
            for (DataSetAttributes dataSetAttributes : actual) {
                dataSetAttributes.setCatalogName(null);
                dataSetAttributes.setVolumeSerial(null);
            }

            assertThat(actual, hasItem(expected));
        } finally {
            deleteDataSet(tempDataSet);
        }
    }

    // TODO - add better test of multiple data sets?

    @Test
    public void testGetInvalidDatasets() throws Exception {
        getDataSetsDetails(INVALID_DATASET_NAME).then().statusCode(HttpStatus.SC_OK).body("$", IsEmptyCollection.empty());
    }

    @Test
    // TODO - need to create the unauthorised dataset in setup script
    @Ignore("Task 19604")
    public void testGetUnauthorisedDatasetMembers() throws Exception {
        getDataSetsDetails(UNAUTHORIZED_DATASET).then().statusCode(HttpStatus.SC_FORBIDDEN);
    }
}