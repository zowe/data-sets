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
import org.junit.Ignore;
import org.junit.Test;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.errors.ApiError;
import org.zowe.api.common.exceptions.ZoweApiRestException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;

public class DataSetsGetMembersIntegrationTest extends AbstractDataSetsIntegrationTest {

    @Test
    public void testGetValidDatasetMembers() throws Exception {
        getMembers(TEST_JCL_PDS).then().statusCode(HttpStatus.SC_OK).body("$", hasItems(JOB_IEFBR14, JOB_WITH_STEPS));
    }

    @Test
    public void testGetInvalidDatasetMembers() throws Exception {
        ZoweApiRestException expected = new DataSetNotFoundException(INVALID_DATASET_NAME);

        ApiError expectedError = expected.getApiError();

        getMembers(INVALID_DATASET_NAME).then().statusCode(expectedError.getStatus().value())
            .contentType(ContentType.JSON).body("status", equalTo(expectedError.getStatus().name()))
            .body("message", equalTo(expectedError.getMessage()));
    }

    @Test
    // TODO - need to create the unauthorised dataset in setup script
    @Ignore("Task 19604")
    public void testGetUnauthoriszedDatasetMembers() throws Exception {
        getMembers(UNAUTHORIZED_DATASET).then().statusCode(HttpStatus.SC_FORBIDDEN);
    }
}