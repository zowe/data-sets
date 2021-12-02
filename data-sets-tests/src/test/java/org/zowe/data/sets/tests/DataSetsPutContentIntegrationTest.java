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

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.hamcrest.text.MatchesPattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.zowe.data.sets.model.DataSetContent;
import org.zowe.data.sets.model.DataSetCreateRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.*;

@Slf4j
public class DataSetsPutContentIntegrationTest extends AbstractDataSetsIntegrationTest {

    protected static final String JOB_IEFBR14 = "IEFBR14";
    private static final String TEMP_SDS = HLQ + ".TEMP.SDS";
    private static final String TEMP_PDS = HLQ + ".TEMP.PDS";
    private static String jcl;
    private static DataSetContent content;

    @BeforeClass
    public static void createTempDataSets() throws IOException {
        DataSetCreateRequest pdsRequest = createPdsRequest(TEMP_PDS);
        createDataSet(pdsRequest).then().statusCode(HttpStatus.SC_CREATED);
        // Create dataset member, so we can get the etag back
        putDataSetContent(getDataSetMemberPath(TEMP_PDS, JOB_IEFBR14), new DataSetContent("test")).then()
            .statusCode(HttpStatus.SC_NO_CONTENT);

        DataSetCreateRequest sdsRequest = createSdsRequest(TEMP_SDS);
        createDataSet(sdsRequest).then().statusCode(HttpStatus.SC_CREATED);

        jcl = new String(Files.readAllBytes(Paths.get("testFiles/" + JOB_IEFBR14)));
        content = new DataSetContent(jcl);
    }

    @AfterClass
    public static void cleanUp() {
        deleteDataSet(TEMP_SDS);
        deleteDataSet(TEMP_PDS);
    }

    @Test
    public void testPutMemberContentWithoutIfMatchWorks() {
        putDataSetContent(getDataSetMemberPath(TEMP_PDS, JOB_IEFBR14), new DataSetContent("junk\n")).then()
            .statusCode(HttpStatus.SC_NO_CONTENT).header("ETag", is(nullValue()));
        getDataSetContent(getDataSetMemberPath(TEMP_PDS, JOB_IEFBR14)).then().statusCode(HttpStatus.SC_OK)
            .body("records", equalTo("junk\n"));
    }

    @Test
    public void testPutMemberContentWithEtag() {
        String eTag = getDataSetContentWithEtag(getDataSetMemberPath(TEMP_PDS, JOB_IEFBR14)).then().extract().header("ETag");

        Response r = putDataSetContentReturnEtag(getDataSetMemberPath(TEMP_PDS, JOB_IEFBR14), content, eTag);
        log.info("testPutMemberContentWithEtag test response: {}: {}", r.getStatusCode(), r.getBody().prettyPrint());
        r.then().statusCode(HttpStatus.SC_NO_CONTENT).header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX));
        getDataSetContent(getDataSetMemberPath(TEMP_PDS, JOB_IEFBR14)).then().statusCode(HttpStatus.SC_OK)
            .body("records", equalTo(jcl));
    }

    @Test
    public void testPutMemberContentWithNoEtag() {
        String eTag = getDataSetContentWithEtag(getDataSetMemberPath(TEMP_PDS, JOB_IEFBR14)).then().extract().header("ETag");

        putDataSetContent(getDataSetMemberPath(TEMP_PDS, JOB_IEFBR14), content, eTag).then()
                .statusCode(HttpStatus.SC_NO_CONTENT)
                .header("ETag", is(nullValue()));
        getDataSetContent(getDataSetMemberPath(TEMP_PDS, JOB_IEFBR14)).then().statusCode(HttpStatus.SC_OK)
                .body("records", equalTo(jcl));
    }

    @Test
    public void testPutSequentialDataSetContent() {
        String eTag = getDataSetContentWithEtag(TEMP_SDS).then().extract().header("ETag");
        putDataSetContentReturnEtag(TEMP_SDS, content, eTag).then().statusCode(HttpStatus.SC_NO_CONTENT)
                .header("ETag", MatchesPattern.matchesPattern(HEX_IN_QUOTES_REGEX));
        getDataSetContent(TEMP_SDS).then().statusCode(HttpStatus.SC_OK).body("records", equalTo(jcl));
    }

    @Test
    public void testPutSequentialDataSetContentWithIncorrectETag() {
        String wrongEtag = "00000aaaa";
        putDataSetContent(TEMP_SDS, content, wrongEtag).then().statusCode(HttpStatus.SC_PRECONDITION_FAILED);
    }

    @Test
    // TODO - need to create the unauthorised dataset in setup script
    @Ignore("Task 19604")
    public void testPutUnauthorisedDatasetContent() {
        putDataSetContent(UNAUTHORIZED_DATASET, content).then().statusCode(HttpStatus.SC_FORBIDDEN);
    }
}