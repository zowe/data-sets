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

import io.restassured.http.ContentType;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.zowe.api.common.errors.ApiError;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.data.sets.exceptions.InvalidDirectoryBlockException;
import org.zowe.data.sets.model.DataSetAttributes;
import org.zowe.data.sets.model.DataSetCreateRequest;
import org.zowe.data.sets.model.DataSetOrganisationType;

import java.util.List;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

public class DataSetsCreateIntegrationTests extends AbstractDataSetsIntegrationTest {

    private static final String TEST_DATA_SET = HLQ + ".TEST.DELETE";

    // TODO - use junit 5 and nest the cleanup?
    private String cleanUp = null;

    @After
    public void cleanUp() throws Exception {
        if (cleanUp != null) {
            deleteDataSet(cleanUp);
        }
    }

    @Test
    public void testCreatePds() throws Exception {
        DataSetCreateRequest pdsRequest = createPdsRequest(TEST_DATA_SET);
        cleanUp = TEST_DATA_SET;
        createDataSet(pdsRequest).then().statusCode(HttpStatus.SC_CREATED)
                .header("Location", endsWith(DATASETS_ROOT_ENDPOINT + "/" + TEST_DATA_SET)).body(equalTo(""));

        List<DataSetAttributes> actual = getDataSetsDetails(TEST_DATA_SET).then().statusCode(HttpStatus.SC_OK).extract().body()
                .jsonPath().getList("", DataSetAttributes.class);
        assertEquals("Should have created the correct type", DataSetOrganisationType.PO,
                actual.get(0).getDataSetOrganization());
    }

    @Test
    public void testTryingToCreateExistingPdsThrowsError() throws Exception {
        DataSetCreateRequest pdsRequest = createPdsRequest(TEST_DATA_SET);
        cleanUp = TEST_DATA_SET;
        createDataSet(pdsRequest);

        // TODO - work out how to decipher the dynamic allocation error codes
//        ZoweApiRestException expected = new DataSetAlreadyExists(VALID_DATASET_NAME);
//        ApiError expectedError = expected.getApiError();

        createDataSet(pdsRequest).then().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR).contentType(ContentType.JSON);
//            .body("status", equalTo(expectedError.getStatus().name()))
//            .body("message", equalTo(expectedError.getMessage()));
    }

    @Test
    public void testCreateSds() throws Exception {
        DataSetCreateRequest sdsRequest = createSdsRequest(TEST_DATA_SET);
        cleanUp = TEST_DATA_SET;
        createDataSet(sdsRequest).then().statusCode(HttpStatus.SC_CREATED)
                .header("Location", endsWith(DATASETS_ROOT_ENDPOINT + "/" + TEST_DATA_SET)).body(equalTo(""));

        List<DataSetAttributes> actual = getDataSetsDetails(TEST_DATA_SET).then().statusCode(HttpStatus.SC_OK).extract().body()
                .jsonPath().getList("", DataSetAttributes.class);
        assertEquals("Should have created the correct type", DataSetOrganisationType.PS,
                actual.get(0).getDataSetOrganization());

    }

    @Test
    public void testPostDatasetWithInvalidRequestFails() throws Exception {
        ZoweApiRestException expected = new InvalidDirectoryBlockException(TEST_DATA_SET);
        ApiError expectedError = expected.getApiError();

        DataSetCreateRequest sdsRequestWithDirBlk = createSdsRequest(TEST_DATA_SET);
        cleanUp = TEST_DATA_SET;
        sdsRequestWithDirBlk.setDirectoryBlocks(10);
        createDataSet(sdsRequestWithDirBlk).then().statusCode(expectedError.getStatus().value())
                .contentType(ContentType.JSON).body("status", equalTo(expectedError.getStatus().name()))
                .body("message", equalTo(expectedError.getMessage()));
    }
}
