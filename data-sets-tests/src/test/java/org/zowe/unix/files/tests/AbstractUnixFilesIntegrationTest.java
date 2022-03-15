/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019, 2020
 */
package org.zowe.unix.files.tests;

import io.restassured.RestAssured;

import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.zowe.tests.AbstractFilesIntegrationTest;
import org.zowe.unix.files.model.UnixDirectoryAttributesWithChildren;
import org.zowe.unix.files.model.UnixDirectoryChild;
import org.zowe.unix.files.model.UnixEntityType;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AbstractUnixFilesIntegrationTest extends AbstractFilesIntegrationTest {

    static final String UNIXFILES_SERVICE_ID = "unixfiles";
    static final String UNIXFILES_BASE_URL = FILES_API_BASE_URL + UNIXFILES_SERVICE_ID;
    static final String BASE_URL = getBaseUrl(UNIXFILES_SERVICE_ID);
    static final String TEST_DIRECTORY = System.getProperty("server.test.directory");

    @BeforeClass
    public static void setUpEndpoint() throws Exception {
        RestAssured.baseURI = BASE_URL;
    }

    public static void testGetDirectory(String directoryPath, UnixDirectoryChild[] expectedChildren) throws Exception {
        UnixDirectoryAttributesWithChildren response = RestAssured.given().header(AUTH_HEADER)
                .when().get("?path=" + directoryPath).then()
                .statusCode(HttpStatus.SC_OK).header("Content-Encoding", "gzip").extract().body().as(UnixDirectoryAttributesWithChildren.class);

        validateDirectory(response, expectedChildren);
    }

    private static void validateDirectory(UnixDirectoryAttributesWithChildren directory,
            UnixDirectoryChild[] expectedChildren) {
        assertFalse(directory.getOwner().isEmpty());
        assertFalse(directory.getGroup().isEmpty());
        assertFalse(directory.getPermissionsSymbolic().isEmpty());
        assertTrue(directory.getPermissionsSymbolic().startsWith("d"));
        assertTrue(directory.getSize() == (expectedChildren.length > 0 ? 8192 : 0));
        assertTrue(directory.getLastModified().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"));
        assertEquals(directory.getType(), UnixEntityType.DIRECTORY);

        validateDirectoryChildren(directory.getChildren(), expectedChildren);
    }

    private static void validateDirectoryChildren(List<UnixDirectoryChild> actualChildren,
            UnixDirectoryChild[] expectedChildren) {
        for (UnixDirectoryChild actualChild : actualChildren) {
            assertTrue(actualChild.getLastModified().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"));
            actualChild.setLastModified(null);
        }
        for (UnixDirectoryChild expectedChild : actualChildren) {
            expectedChild.setLastModified(null);
        }
        assertEquals(actualChildren.size(), expectedChildren.length);
        assertThat(actualChildren, containsInAnyOrder(expectedChildren));
    }
}
