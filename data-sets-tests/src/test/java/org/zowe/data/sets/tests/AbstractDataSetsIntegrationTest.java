/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */
package org.zowe.data.sets.tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.junit.BeforeClass;
import org.zowe.data.sets.model.AllocationUnitType;
import org.zowe.data.sets.model.DataSetContent;
import org.zowe.data.sets.model.DataSetCreateRequest;
import org.zowe.data.sets.model.DataSetOrganisationType;

public abstract class AbstractDataSetsIntegrationTest extends AbstractHttpIntegrationTest {

    static final String DATASETS_ROOT_ENDPOINT = "datasets";

    static final String HLQ = USER.toUpperCase();
    static final String TEST_JCL_PDS = HLQ + ".TEST.JCL";
    static final String INVALID_DATASET_NAME = HLQ + ".TEST.INVALID";
    static final String UNAUTHORIZED_DATASET = "IBMUSER.NOWRITE.CNTL";

    @BeforeClass
    public static void setUpEndpoint() {
        RestAssured.basePath = DATASETS_ROOT_ENDPOINT;
    }

    protected static Response getMembers(String dataSetName) {
        return RestAssured.given().when().get(dataSetName + "/members");
    }

    protected static Response getDataSets(String dataSetFilter) {
        return RestAssured.given().when().get(dataSetFilter);
    }

    protected static Response createDataSet(DataSetCreateRequest attributes) {
        return RestAssured.given().contentType("application/json").body(attributes).when().post();
    }

    protected static Response getDataSetContent(String dataSetName) {
        return RestAssured.given().when().get(dataSetName + "/content");
    }

    protected static Response putDataSetContent(String dataSetName, DataSetContent body) {
        return RestAssured.given().contentType("application/json").body(body).when().put(dataSetName + "/content");
    }

    static DataSetCreateRequest createPdsRequest(String dataSetName) {
        DataSetCreateRequest defaultJclPdsRequest = DataSetCreateRequest.builder().name(dataSetName).blksize(400)
            .primary(10).lrecl(80).secondary(5).dirblk(21).dsorg(DataSetOrganisationType.PO).recfm("FB")
            .alcunit(AllocationUnitType.TRACK).build();
        return defaultJclPdsRequest;
    }

    protected static DataSetCreateRequest createSdsRequest(String dataSetName) {
        DataSetCreateRequest sdsRequest = createPdsRequest(dataSetName);
        sdsRequest.setDsorg(DataSetOrganisationType.PS);
        return sdsRequest;
    }

    protected static Response deleteDataSet(String dataSetName) {
        return RestAssured.given().when().delete(dataSetName);
    }

    static String getDataSetMemberPath(String pds, String member) {
        return pds + "(" + member + ")";
    }

    static String getTestJclMemberPath(String member) {
        return getDataSetMemberPath(TEST_JCL_PDS, member);
    }
}
