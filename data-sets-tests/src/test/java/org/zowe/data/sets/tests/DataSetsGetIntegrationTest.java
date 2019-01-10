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
import org.junit.BeforeClass;
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

    // TODO - push up?
    @BeforeClass
    public static void initialiseDatasetsIfNescessary() throws Exception {
        if (getMembers(TEST_JCL_PDS)
            .statusCode() != HttpStatus.SC_OK) {
            // TODO NOW - create a pds and member if they don't exist
//            createDataSet(createPdsRequest(TEST_JCL_PDS));
//            createPdsMember(getTestJclMemberPath(JOB_IEFBR14), new String(Files
//                .readAllBytes(Paths
//                    .get("testFiles/jobIEFBR14"))));
//            createPdsMember(getTestJclMemberPath(JOB_WITH_STEPS), new String(Files
//                .readAllBytes(Paths
//                    .get("testFiles/jobWithSteps"))));
        }
    }

    @Test
    public void testGetValidDataset() throws Exception {
        String tempDataSet = HLQ + ".TEST.DELETE";
        DataSetCreateRequest pdsRequest = createPdsRequest(tempDataSet);
        createDataSet(pdsRequest);

        try {

            String pattern = "yyyy/MM/dd";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            String today = simpleDateFormat
                .format(new Date());

            DataSetAttributes expected = DataSetAttributes
                .builder()
                .blksize(pdsRequest
                    .getBlksize())
                .catnm(null) // wildcard
                .dev("3390")
                .cdate(today)
                .name(tempDataSet)
                .migrated(false)
                .dsorg(pdsRequest
                    .getDsorg())
                .edate("***None***")
                .lrecl(pdsRequest
                    .getLrecl())
                .spacu(pdsRequest
                    .getAlcunit())
                .recfm(pdsRequest
                    .getRecfm())
                .sizex(10)
                .used(10)
                .volser(null) // wildcard
                .build();

            List<DataSetAttributes> actual = getDataSets(tempDataSet)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .body()
                .jsonPath()
                .getList("", DataSetAttributes.class);

            // We can't tell the value of some attributes
            for (DataSetAttributes dataSetAttributes : actual) {
                dataSetAttributes
                    .setCatnm(null);
                dataSetAttributes
                    .setVolser(null);
            }

            assertThat(actual, hasItem(expected));
        } finally {
            deleteDataSet(tempDataSet);
        }
    }

    // TODO - add better test of multiple data sets?

    @Test
    public void testGetInvalidDatasets() throws Exception {
        getDataSets(INVALID_DATASET_NAME)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("$", IsEmptyCollection
                .empty());
    }

    @Test
    // TODO - need to create the unauthorised dataset in setup script
    @Ignore("Task 19604")
    public void testGetUnauthoriszedDatasetMembers() throws Exception {
        getDataSets(UNAUTHORIZED_DATASET)
            .then()
            .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    private String generateRegexPattern(String initial) {
        return initial
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("*", ".*");
    }

}