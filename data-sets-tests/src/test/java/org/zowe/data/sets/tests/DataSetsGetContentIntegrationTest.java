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
import org.hamcrest.text.IsEqualIgnoringWhiteSpace;
import org.hamcrest.text.MatchesPattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.errors.ApiError;
import org.zowe.api.common.exceptions.ZoweApiRestException;

import static org.hamcrest.CoreMatchers.equalTo;

public class DataSetsGetContentIntegrationTest extends AbstractDataSetsIntegrationTest {

    private static final String TEST_PDS = HLQ + ".TEMP.GETCONT.JCL";

    @BeforeClass
    public static void createDataSets() throws Exception {
        createPdsWithMembers(TEST_PDS, "MEMBER1");
    }

    @AfterClass
    public static void cleanup() {
        deleteDataSet(TEST_PDS);
    }

    @Test
    public void testGetMemberContent() throws Exception {
        getDataSetContent(getDataSetMemberPath(TEST_PDS, "MEMBER1")).then().statusCode(HttpStatus.SC_OK)
            .header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX))
            .body("records", IsEqualIgnoringWhiteSpace.equalToIgnoringWhiteSpace(DEFAULT_MEMBER_CONTENT + "\n")); // SJH: zosmf appends newline
    }

    // SJH: Sequential GET tested in PUT integration tests

    @Test
    public void testGetInvalidDataSetContent() throws Exception {
        ZoweApiRestException expected = new DataSetNotFoundException(INVALID_DATASET_NAME);

        ApiError expectedError = expected.getApiError();

        getDataSetContent(INVALID_DATASET_NAME).then().statusCode(expectedError.getStatus().value())
            .contentType(ContentType.JSON).body("status", equalTo(expectedError.getStatus().name()))
            .body("message", equalTo(expectedError.getMessage()));
    }

    @Test
    // TODO - need to create the unauthorised dataset in setup script
    @Ignore("Task 19604")
    public void testGetUnauthorisedDatasetContent() throws Exception {
        getDataSetContent(UNAUTHORIZED_DATASET).then().statusCode(HttpStatus.SC_FORBIDDEN);
    }
}