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
import org.zowe.data.sets.model.DataSet;
import org.zowe.data.sets.model.DataSetCreateRequest;

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
            DataSet expected = DataSet.builder().name(tempDataSet).migrated(false).build();

            List<DataSet> actual = getDataSets(tempDataSet).then().statusCode(HttpStatus.SC_OK).extract()
                    .body().jsonPath().getList("items", DataSet.class);
            assertThat(actual, hasItem(expected));
        } finally {
            deleteDataSet(tempDataSet);
        }
    }

    // TODO - add better test of multiple data sets?

    @Test
    public void testGetInvalidDatasets() throws Exception {
        getDataSets(INVALID_DATASET_NAME).then().statusCode(HttpStatus.SC_OK).body("items", IsEmptyCollection.empty());
    }

    @Test
    // TODO - need to create the unauthorised dataset in setup script
    @Ignore("Task 19604")
    public void testGetUnauthorisedDatasetMembers() throws Exception {
        getDataSets(UNAUTHORIZED_DATASET).then().statusCode(HttpStatus.SC_FORBIDDEN);
    }
}