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

import org.apache.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.zowe.data.sets.model.DataSetContent;
import org.zowe.data.sets.model.DataSetCreateRequest;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;

public class DataSetsPutContentIntegrationTest extends AbstractDataSetsIntegrationTest {

    private static final String TEMP_SDS = HLQ + ".TEMP.SDS";
    private static final String TEMP_PDS = HLQ + ".TEMP.PDS";
    private static String jcl;
    private static DataSetContent content;

    @BeforeClass
    public static void createTempDataSets() throws Exception {
        DataSetCreateRequest pdsRequest = createPdsRequest(TEMP_PDS);
        createDataSet(pdsRequest).then().statusCode(HttpStatus.SC_CREATED);

        DataSetCreateRequest sdsRequest = createSdsRequest(TEMP_SDS);
        createDataSet(sdsRequest).then().statusCode(HttpStatus.SC_CREATED);

        jcl = new String(Files.readAllBytes(Paths.get("testFiles/" + JOB_IEFBR14)));
        content = new DataSetContent(jcl);
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        deleteDataSet(TEMP_SDS);
        deleteDataSet(TEMP_PDS);
    }

    @Test
    // TODO NOW - check E-Tag works
    public void testPutMemberContent() throws Exception {
        putDataSetContent(getDataSetMemberPath(TEMP_PDS, JOB_IEFBR14), content).then()
            .statusCode(HttpStatus.SC_NO_CONTENT);
        // .header("ETag", notNullValue())
        getDataSetContent(getDataSetMemberPath(TEMP_PDS, JOB_IEFBR14)).then().statusCode(HttpStatus.SC_OK)
            .body("records", equalTo(jcl));
    }

    // TODO now test of incorrect etag

    @Test
    public void testPutSequentialDataSetContent() throws Exception {
        putDataSetContent(TEMP_SDS, content).then().statusCode(HttpStatus.SC_NO_CONTENT);
        // .header("ETag", notNullValue())
        getDataSetContent(TEMP_SDS).then().statusCode(HttpStatus.SC_OK).body("records", equalTo(jcl));
    }

    @Test
    // TODO - need to create the unauthorised dataset in setup script
    @Ignore("Task 19604")
    public void testPutUnauthoriszedDatasetContent() throws Exception {
        putDataSetContent(UNAUTHORIZED_DATASET, content).then().statusCode(HttpStatus.SC_FORBIDDEN);
    }
}