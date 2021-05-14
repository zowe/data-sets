/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2020
 */
package org.zowe.data.sets.tests;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.zowe.data.sets.model.AllocationUnitType;
import org.zowe.data.sets.model.DataSetContent;
import org.zowe.data.sets.model.DataSetCreateRequest;
import org.zowe.data.sets.model.DataSetOrganisationType;
import org.zowe.tests.AbstractFilesIntegrationTest;

public abstract class AbstractDataSetsIntegrationTest extends AbstractFilesIntegrationTest {

    static final String DATASETS_ROOT_ENDPOINT = "datasets";

    static final String HLQ = USER.toUpperCase();
    static final String INVALID_DATASET_NAME = HLQ + ".TEST.INVALID";
    static final String UNAUTHORIZED_DATASET = "IBMUSER.NOWRITE.CNTL";
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
        return RestAssured.given().header(AUTH_HEADER).when().get(dataSetName + "/members");
    }

    static Response getDataSetsDetails(String dataSetFilter) {
        return RestAssured.given().header(AUTH_HEADER).when().get(dataSetFilter);
    }

    static Response getDataSets(String dataSetFilter) {
        return RestAssured.given().header(AUTH_HEADER).when().get(dataSetFilter + "/list");
    }

    static Response createDataSet(DataSetCreateRequest attributes) {
        return RestAssured.given().header(AUTH_HEADER).contentType("application/json").body(attributes).when().post();
    }

    static Response getDataSetContent(String dataSetName) {
        RequestSpecification requestSpecification = new RequestSpecBuilder().setUrlEncodingEnabled(false).build();
        return RestAssured.given().spec(requestSpecification).header(AUTH_HEADER).when().get(dataSetName + "/content");
    }

    static Response getDataSetContentWithEtag(String dataSetName) {
        RequestSpecification requestSpecification = new RequestSpecBuilder().setUrlEncodingEnabled(false).build();
        return RestAssured.given().spec(requestSpecification).header(AUTH_HEADER).header("X-Return-Etag", "true").when().get(dataSetName + "/content");
    }

    static Response putDataSetContent(String dataSetName, DataSetContent body) {
        RequestSpecification requestSpecification = new RequestSpecBuilder().setUrlEncodingEnabled(false).build();
        return RestAssured.given().spec(requestSpecification).header(AUTH_HEADER).contentType("application/json")
                .body(body).when().put(dataSetName + "/content");
    }

    static Response putDataSetContent(String dataSetName, DataSetContent body, String etag) {
        RequestSpecification requestSpecification = new RequestSpecBuilder().setUrlEncodingEnabled(false).build();
        return RestAssured.given().spec(requestSpecification).contentType("application/json").body(body)
                .header("If-Match", etag).header(AUTH_HEADER).when()
                .put(dataSetName + "/content");
    }

    static Response putDataSetContentReturnEtag(String dataSetName, DataSetContent body, String etag) {
        RequestSpecification requestSpecification = new RequestSpecBuilder().setUrlEncodingEnabled(false).build();
        return RestAssured.given().spec(requestSpecification).contentType("application/json").body(body)
                .header("If-Match", etag).header(AUTH_HEADER).header("X-Return-Etag", "true").when()
                .put(dataSetName + "/content");
    }

    static DataSetCreateRequest createPdsRequest(String dataSetName) {
        return createRequestWithDataSetOrganisation(dataSetName, DataSetOrganisationType.PO);
    }

    static DataSetCreateRequest createPdseRequest(String dataSetName) {
        return createRequestWithDataSetOrganisation(dataSetName, DataSetOrganisationType.PO_E);
    }

    static DataSetCreateRequest createSdsRequest(String dataSetName) {
        DataSetCreateRequest sdsRequest = createRequestWithDataSetOrganisation(dataSetName, DataSetOrganisationType.PS);
        sdsRequest.setDirectoryBlocks(0); // SJH: if directory block != 0 zosmf interprets as PDS
        return sdsRequest;
    }

    static DataSetCreateRequest createRequestWithDataSetOrganisation(String dataSetName, DataSetOrganisationType dsorg) {
        DataSetCreateRequest defaultJclPdsRequest = DataSetCreateRequest.builder().name(dataSetName).blockSize(400)
                .primary(10).recordLength(80).secondary(5).directoryBlocks(20)
                .dataSetOrganization(dsorg).recordFormat("FB").allocationUnit(AllocationUnitType.TRACK)
                .build();
        return defaultJclPdsRequest;
    }

    static Response deleteDataSet(String dataSetName) {
        RequestSpecification requestSpecification = new RequestSpecBuilder().setUrlEncodingEnabled(false).build();
        return RestAssured.given().spec(requestSpecification).header(AUTH_HEADER).when().delete(dataSetName);
    }

    static String getDataSetMemberPath(String pds, String member) {
        return pds + "(" + member + ")";
    }

}
