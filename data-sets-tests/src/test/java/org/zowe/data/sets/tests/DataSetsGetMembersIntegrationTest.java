package org.zowe.data.sets.tests;

import io.restassured.http.ContentType;

import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.errors.ApiError;
import org.zowe.api.common.exceptions.ZoweApiRestException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;

public class DataSetsGetMembersIntegrationTest extends AbstractDataSetsIntegrationTest {

    // TODO - push up?
    @BeforeClass
    public static void initialiseDatasetsIfNescessary() throws Exception {
        if (getMembers(TEST_JCL_PDS).statusCode() != HttpStatus.SC_OK) {
            // TODO - create a pds and member if they don't exist
//            createPds(TEST_JCL_PDS);
//            createPdsMember(getTestJclMemberPath(JOB_IEFBR14),
//                    new String(Files.readAllBytes(Paths.get("testFiles/jobIEFBR14"))));
//            createPdsMember(getTestJclMemberPath(JOB_WITH_STEPS),
//                    new String(Files.readAllBytes(Paths.get("testFiles/jobWithSteps"))));
        }
    }

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