package org.zowe.data.sets.tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import org.junit.Before;

public abstract class AbstractDataSetsIntegrationTest extends AbstractHttpIntegrationTest {

    static final String DATASETS_ROOT_ENDPOINT = "datasets";

    static final String HLQ = USER.toUpperCase();
    static final String TEST_JCL_PDS = HLQ + ".TEST.JCL";
    static final String INVALID_DATASET_NAME = HLQ + ".TEST.INVALID";
    static final String UNAUTHORIZED_DATASET = "IBMUSER.NOWRITE.CNTL";

    @Before
    public void setUpEndpoint() {
        RestAssured.basePath = DATASETS_ROOT_ENDPOINT;
    }

    protected static Response getMembers(String dataSetName) {
        return RestAssured.given().when().get(dataSetName + "/members");
    }
}
