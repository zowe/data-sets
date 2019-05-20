/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.data.sets.tests;

import io.restassured.RestAssured;

import org.apache.http.HttpStatus;
import org.junit.Test;

public class DataSetsLogoutIntegrationTest extends AbstractDataSetsIntegrationTest {
    public static final String path = INVALID_DATASET_NAME + "/list";

    @Test
    public void testGetInvalidDatasetMembersWithoutAuth() throws Exception {
        RestAssured.given().when().auth().none().get(path)
            .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void testLogoutWithGetInvalidDatasetMembers() throws Exception {
        testLogout(path);
    }
}