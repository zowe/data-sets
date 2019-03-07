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

import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.zowe.data.sets.model.AllocationUnitType;
import org.zowe.data.sets.model.DataSetContent;
import org.zowe.data.sets.model.DataSetCreateRequest;
import org.zowe.data.sets.model.DataSetOrganisationType;
import org.zowe.tests.AbstractHttpIntegrationTest;

public abstract class AbstractDataSetsIntegrationTest extends AbstractHttpIntegrationTest {

    static final String DATASETS_ROOT_ENDPOINT = "datasets";

    static final String HLQ = USER.toUpperCase();
    static final String INVALID_DATASET_NAME = HLQ + ".TEST.INVALID";
    static final String UNAUTHORIZED_DATASET = "IBMUSER.NOWRITE.CNTL";
    static final String HEX_IN_QUOTES_REGEX = "^\"[0-9A-F]+\"$";
    static final String DEFAULT_MEMBER_CONTENT = "//ATLJ0000 JOB (ADL),'ATLAS',MSGCLASS=X,CLASS=A,TIME=1440\n//*        TEST JOB\n";

    @BeforeClass
    public static void setUpEndpoint() throws Exception {
        RestAssured.basePath = DATASETS_ROOT_ENDPOINT;
    }

    static void createPdsWithMembers(String pdsName, String... memberNames) {
        DataSetContent content = new DataSetContent(DEFAULT_MEMBER_CONTENT);

        createDataSet(createPdsRequest(pdsName)).then().statusCode(HttpStatus.SC_CREATED);
        for (String member : memberNames) {
            putDataSetContent(getDataSetMemberPath(pdsName, member), content).then()
                    .statusCode(HttpStatus.SC_NO_CONTENT);
        }
    }

    static Response getMembers(String dataSetName) {
        return RestAssured.given().when().get(dataSetName + "/members");
    }

    static Response getDataSetsDetails(String dataSetFilter) {
        return RestAssured.given().when().get(dataSetFilter);
    }

    static Response getDataSets(String dataSetFilter) {
        return RestAssured.given().when().get(dataSetFilter + "/list");
    }

    static Response createDataSet(DataSetCreateRequest attributes) {
        return RestAssured.given().contentType("application/json").body(attributes).when().post();
    }

    static Response getDataSetContent(String dataSetName) {
        return RestAssured.given().when().get(dataSetName + "/content");
    }

    static Response putDataSetContent(String dataSetName, DataSetContent body) {
        return RestAssured.given().contentType("application/json").body(body).when().put(dataSetName + "/content");
    }

    static Response putDataSetContent(String dataSetName, DataSetContent body, String etag) {
        return RestAssured.given().contentType("application/json").body(body).header("If-Match", etag).when()
                .put(dataSetName + "/content");
    }

    static DataSetCreateRequest createPdsRequest(String dataSetName) {
        DataSetCreateRequest defaultJclPdsRequest = DataSetCreateRequest.builder().name(dataSetName).blockSize(400)
                .primary(10).recordLength(80).secondary(5).directoryBlocks(20)
                .dataSetOrganization(DataSetOrganisationType.PO).recordFormat("FB").allocationUnit(AllocationUnitType.TRACK)
                .build();
        return defaultJclPdsRequest;
    }

    static DataSetCreateRequest createSdsRequest(String dataSetName) {
        DataSetCreateRequest sdsRequest = createPdsRequest(dataSetName);
        sdsRequest.setDirectoryBlocks(0); // SJH: if directory block != 0 zosmf interprets as PDS
        sdsRequest.setDataSetOrganization(DataSetOrganisationType.PS);
        return sdsRequest;
    }

    static Response deleteDataSet(String dataSetName) {
        return RestAssured.given().when().delete(dataSetName);
    }

    static String getDataSetMemberPath(String pds, String member) {
        return pds + "(" + member + ")";
    }
}
