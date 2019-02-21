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

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.errors.ApiError;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.data.sets.model.DataSetCreateRequest;

import static org.hamcrest.CoreMatchers.equalTo;

public class DataSetsDeleteIntegrationTests extends AbstractDataSetsIntegrationTest {

    private static final String TEST_PDS = HLQ + ".A" + RandomStringUtils.randomAlphanumeric(7);

    @BeforeClass
    public static void createDataSets() throws Exception {
        createPdsWithMembers(TEST_PDS, "MEMBER1");
    }

    @AfterClass
    public static void cleanup() {
        deleteDataSet(TEST_PDS);
    }

    @Test
    public void testDeleteSdsWorks() throws Exception {
        String tempPath = HLQ + ".TEMP";
        createAndDelete(createSdsRequest(tempPath));
    }

    @Test
    public void testDeletePdsWorks() throws Exception {
        String tempPath = HLQ + ".TEMP";
        createAndDelete(createPdsRequest(tempPath));
    }

    private void createAndDelete(DataSetCreateRequest request) {
        createDataSet(request).then().statusCode(HttpStatus.SC_CREATED);
        deleteDataSet(request.getName()).then().statusCode(HttpStatus.SC_NO_CONTENT).body(equalTo(""));
    }

    @Test
    public void testDeletePdsMemberWorks() throws Exception {
        String memberPath = getDataSetMemberPath(TEST_PDS, "MEMBER1");
        deleteDataSet(memberPath).then().statusCode(HttpStatus.SC_NO_CONTENT).body(equalTo(""));
    }

    @Test
    // TODO - need to create the unauthorised dataset in setup script
    @Ignore("Task 19604")
    public void testDeleteFileWithoutAccess() throws Exception {
        deleteDataSet(UNAUTHORIZED_DATASET).then().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void testDeleteDatasetsInvalidDataset() throws Exception {
        System.out.println("testDeleteDatasetsInvalidDataset");
        ZoweApiRestException expected = new DataSetNotFoundException(INVALID_DATASET_NAME);
        ApiError expectedError = expected.getApiError();

        verifyExceptionReturn(expectedError, deleteDataSet(INVALID_DATASET_NAME));
    }
}
