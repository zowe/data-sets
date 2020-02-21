/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2020
 */
package org.zowe.data.sets.tests;

import org.apache.http.HttpStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zowe.data.sets.model.DataSetCreateRequest;
import org.zowe.data.sets.model.DataSetRenameRequest;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class DataSetsPutRenameIntegrationTest extends AbstractDataSetsIntegrationTest {


    private static final String TEMP_OLD_SEQ = HLQ + ".RENAME.OLD";
    private static final String TEMP_NEW_SEQ = HLQ + ".RENAME.NEW";
   
    private static final String TEMP_OLD_PDS = HLQ + ".RENAME";
    private static final String TEMP_OLD_MEMBER = "OLD";
    private static final String TEMP_NEW_MEMBER = "NEW";
    
    private static final String TEMP_EXIST_MEMBER1 = "EXIST1";
    private static final String TEMP_EXIST_MEMBER2 = "EXIST2";

    @BeforeClass
    public static void createTempDataSets() throws Exception {
        DataSetCreateRequest sdsRequest = createSdsRequest(TEMP_OLD_SEQ);
        createDataSet(sdsRequest).then().statusCode(HttpStatus.SC_CREATED);
        createPdsWithMembers(TEMP_OLD_PDS, TEMP_OLD_MEMBER, TEMP_EXIST_MEMBER1, TEMP_EXIST_MEMBER2);
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        deleteDataSet(TEMP_OLD_SEQ); //This may still exist if test to rename failed
        deleteDataSet(TEMP_NEW_SEQ).then().statusCode(HttpStatus.SC_NO_CONTENT);
        deleteDataSet(TEMP_OLD_PDS).then().statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void testRenameSequentialDataSet() throws Exception {
        putDataSetRename(TEMP_OLD_SEQ, DataSetRenameRequest.builder().newName(TEMP_NEW_SEQ).build()).then().log().all()
            .statusCode(HttpStatus.SC_NO_CONTENT);
        getDataSetContent(TEMP_NEW_SEQ).then().statusCode(HttpStatus.SC_OK);
        getDataSetContent(TEMP_OLD_SEQ).then().statusCode(HttpStatus.SC_NOT_FOUND);
    }
    
    @Test
    public void testRenameMemberDataSet() throws Exception {
        String oldName = TEMP_OLD_PDS+"("+TEMP_OLD_MEMBER+")";
        String newName = TEMP_OLD_PDS+"("+TEMP_NEW_MEMBER+")";
        
        putDataSetRename(oldName, DataSetRenameRequest.builder().newName(newName).build()).then().log().all()
            .statusCode(HttpStatus.SC_NO_CONTENT);
        getDataSetContent(newName).then().statusCode(HttpStatus.SC_OK);
        getDataSetContent(oldName).then().statusCode(HttpStatus.SC_NOT_FOUND);
    }
    
    @Test
    public void testRenameMemberNonExistentMember() throws Exception {
        String oldName = TEMP_OLD_PDS+"("+"ABC"+")";
        String newName = TEMP_OLD_PDS+"("+"DEF"+")";
        
        //non existent member name is PDS throw NOT FOUND
        putDataSetRename(oldName, DataSetRenameRequest.builder().newName(newName).build()).then().log().all()
            .statusCode(HttpStatus.SC_NOT_FOUND);
    }
    
    @Test
    public void testRenameMemberNonExistentDataSet() throws Exception {
        String oldName = "NoExist.ABC";
        String newName = "NewExist.ABC";
        
        //non existent dataset name is PDS throw INTERNAL ERROR, zosmf throw very general 500 exception
        putDataSetRename(oldName, DataSetRenameRequest.builder().newName(newName).build()).then().log().all()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
    
    @Test
    public void testRenameMemberInvalidMember() throws Exception {
        String oldName = TEMP_OLD_PDS+"("+TEMP_OLD_MEMBER+")";
        //invalid new name where member name length greater than length 8
        String newName = TEMP_OLD_PDS+"("+TEMP_NEW_MEMBER+"ABCDEFGH)";
        
        putDataSetRename(oldName, DataSetRenameRequest.builder().newName(newName).build()).then().log().all()
            .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
    
    
    @Test
    public void testRenameMemberExisting() throws Exception {
        String oldName = TEMP_OLD_PDS+"("+TEMP_EXIST_MEMBER1+")";
        //invalid new name where member name length greater than length 8
        String newName = TEMP_OLD_PDS+"("+TEMP_EXIST_MEMBER2+")";
        
        //non existent dataset name is PDS throw INTERNAL ERROR, zosmf throw very general 500 exception
        putDataSetRename(oldName, DataSetRenameRequest.builder().newName(newName).build()).then().log().all()
            .statusCode(HttpStatus.SC_BAD_REQUEST).content("message", org.hamcrest.Matchers.containsString("exists")); 
    }
    
    private Response putDataSetRename(String oldDataSetName, DataSetRenameRequest body) {
        RequestSpecification requestSpecification = new RequestSpecBuilder().setUrlEncodingEnabled(false).build();
        return RestAssured.given().spec(requestSpecification).contentType("application/json").body(body).when().put(oldDataSetName + "/rename");
    }
}