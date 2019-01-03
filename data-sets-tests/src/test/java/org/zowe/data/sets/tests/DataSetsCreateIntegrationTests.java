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
import io.restassured.response.Response;

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.zowe.api.common.errors.ApiError;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.data.sets.exceptions.DataSetAlreadyExists;
import org.zowe.data.sets.model.DataSetCreateRequest;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;

public class DataSetsCreateIntegrationTests extends AbstractDataSetsIntegrationTest {

    private static final String VALID_DATASET_NAME = HLQ + ".TEST.DELETE";
    private static final String PUT_DATASET_NAME = HLQ + ".TEST.PUT";

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
        DataSetCreateRequest pdsRequest = createPdsRequest(VALID_DATASET_NAME);
        cleanUp = VALID_DATASET_NAME;
        Response response = createDataSet(pdsRequest);
        response.then().statusCode(HttpStatus.SC_CREATED)
                .header("Location", endsWith(DATASETS_ROOT_ENDPOINT + "/" + VALID_DATASET_NAME)).body(equalTo(""));
        // TODO - once get Attributes done
        //
        // JSONArray expected = getExpectedAttributes(VALID_DATASET_NAME, pdsRequest);
        //
        // JSONArray actual =
        // getAttributes(VALID_DATASET_NAME).shouldHaveStatusOk().getEntityAsJsonArray();
        // assertEquals(expected, actual);
    }

    @Test
    public void testTryingToCreateExistingPdsThrowsError() throws Exception {
        DataSetCreateRequest pdsRequest = createPdsRequest(VALID_DATASET_NAME);
        cleanUp = VALID_DATASET_NAME;
        createDataSet(pdsRequest);

        ZoweApiRestException expected = new DataSetAlreadyExists(VALID_DATASET_NAME);
        ApiError expectedError = expected.getApiError();

        // TODO - refactor with errors in get members
        createDataSet(pdsRequest).then().statusCode(expectedError.getStatus().value()).contentType(ContentType.JSON)
                .body("status", equalTo(expectedError.getStatus().name()))
                .body("message", equalTo(expectedError.getMessage()));
    }

    @Test
    public void testCreateSds() throws Exception {
        DataSetCreateRequest sdsRequest = createSdsRequest(VALID_DATASET_NAME);
        cleanUp = VALID_DATASET_NAME;
        Response response = createDataSet(sdsRequest);
        response.then().statusCode(HttpStatus.SC_CREATED)
                .header("Location", endsWith(DATASETS_ROOT_ENDPOINT + "/" + VALID_DATASET_NAME)).body(equalTo(""));
        // TODO - once get Attributes done test
    }

    // TODO - test create sds

    //
    // @Test
    // public void testPostDatasetWithInvalidRequestFails() throws Exception {
    // CreateDataSetRequest sdsRequestWithDirBlk = createSdsRequest();
    // sdsRequestWithDirBlk.setDirblk(10);
    // createDataset(VALID_DATASET_NAME,
    // sdsRequestWithDirBlk).shouldHaveStatus(HttpStatus.SC_BAD_REQUEST);
    // }
    //
    // @Test
    // public void testPostDatasetAlreadyExists() throws Exception {
    // createPds(TEST_JCL_PDS).shouldHaveStatus(HttpStatus.SC_CONFLICT);
    // }
    //
    // @Test
    // public void testUpdateWithChecksumWorks() throws Exception {
    // createSds(PUT_DATASET_NAME);
    // cleanUp = PUT_DATASET_NAME;
    // String checksum = getContent(PUT_DATASET_NAME,
    // "checksum=true").getEntityAs(DataSetContentResponse.class)
    // .getChecksum();
    // updateDatasetContent(PUT_DATASET_NAME, "Some test file",
    // checksum).shouldHaveStatusOk();
    // }
    //
    // @Test
    // public void testUpdateWithIncorrectChecksum() throws Exception {
    // createSds(PUT_DATASET_NAME);
    // cleanUp = PUT_DATASET_NAME;
    //
    // updateDatasetContent(PUT_DATASET_NAME, "Some test file", "junk")
    // .shouldHaveStatus(HttpStatus.SC_PRECONDITION_FAILED);
    // // TODO - create proper error message?
    // }
    //
    // @Test
    // @Ignore("z/os mf error")
    // public void testUpdateDatasetWhichDoesntExist() throws Exception {
    // String notExistantFile = HLQ + ".DUMMY";
    // updateDatasetContent(notExistantFile, "test Content",
    // null).shouldHaveStatus(HttpStatus.SC_NOT_FOUND);
    // // TODO - create proper error message?
    // }
    //
    // // TODO LATER actually test the basedns
    //
    // // Includes create, attributes and content testing
    // @Test
    // public void testPostDatasetCreateWithRecords() throws Exception {
    //
    // String expectedContent = "This is my test report\n";
    // CreateDataSetRequest request = createSdsRequest();
    // request.setRecfm("VB");
    // request.setRecords(expectedContent);
    //
    // createDataset(VALID_DATASET_NAME, request);
    // cleanUp = VALID_DATASET_NAME;
    //
    // JSONArray expected = getExpectedAttributes(VALID_DATASET_NAME, request);
    //
    // // Check attributes
    // JSONArray actual =
    // getAttributes(VALID_DATASET_NAME).shouldHaveStatusOk().getEntityAsJsonArray();
    // assertEquals(expected, actual);
    //
    // // Check content
    // DataSetContentResponse actualContent = getContent(VALID_DATASET_NAME,
    // "convert=true").shouldHaveStatusOk()
    // .getEntityAs(DataSetContentResponse.class);
    // assertEquals(expectedContent, actualContent.getRecords());
    // }
    //
    // private JSONArray getExpectedAttributes(String dataSetName,
    // CreateDataSetRequest request) {
    // // TODO LATER - switch response to bring back ints, not strings
    // JSONObject expectedAttributes = new JSONObject();
    // expectedAttributes.put("name", dataSetName);
    // expectedAttributes.put("blksize", request.getBlksize().toString());
    // expectedAttributes.put("lrecl", request.getLrecl().toString());
    // expectedAttributes.put("recfm", request.getRecfm());
    // expectedAttributes.put("dsorg", request.getDsorg().name());
    // JSONArray expected = new JSONArray();
    // expected.add(0, expectedAttributes);
    // return expected;
    // }
}
