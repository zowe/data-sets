/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.unix.files.services.zosmf;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zowe.api.common.test.services.zosmf.AbstractZosmfRequestRunnerTest;
import org.zowe.api.common.utils.JsonUtils;
import org.zowe.unix.files.exceptions.AlreadyExistsException;
import org.zowe.unix.files.exceptions.InvalidPermissionsException;
import org.zowe.unix.files.model.UnixCreateAssetRequest;
import org.zowe.unix.files.model.UnixEntityType;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CreateUnixAssetZosmfRunner.class })
public class CreateUnixAssetZosmfRunnerTest extends AbstractZosmfRequestRunnerTest {

    @Test
    public void create_unix_asset_file_should_call_zosmf_and_parse_response_correctly() throws Exception {
        UnixCreateAssetRequest createAssetRequest = new UnixCreateAssetRequest(UnixEntityType.FILE, null);

        creatUnixAssetSuccessTest("/u/newFile", createAssetRequest, "{\"type\":\"file\"}");
    }

    @Test
    public void create_unix_asset_directory_should_call_zosmf_and_parse_response_correctly() throws Exception {
        UnixCreateAssetRequest createAssetRequest = new UnixCreateAssetRequest(UnixEntityType.DIRECTORY, null);

        creatUnixAssetSuccessTest("/u/newDir", createAssetRequest, "{\"type\":\"directory\"}");
    }

    private void creatUnixAssetSuccessTest(String path, UnixCreateAssetRequest createAssetRequest,
            String zosmfJsonString) throws Exception {
        mockResponseCache(HttpStatus.SC_CREATED);

        RequestBuilder requestBuilder = mockPostBuilder("restfiles/fs" + path,
                JsonUtils.readAsJsonElement(zosmfJsonString).getAsJsonObject());

        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        assertEquals(path, new CreateUnixAssetZosmfRunner(path, createAssetRequest).run(zosmfConnector));
        verifyInteractions(requestBuilder);
    }

    @Test
    public void create_unix_asset_with_conflicting_assets_already_existing_and_throw_already_exists_error()
            throws Exception {
        String path = "/u/newFile";
        UnixCreateAssetRequest createAssetRequest = new UnixCreateAssetRequest(UnixEntityType.FILE, null);
        String json = "{\"type\":\"file\"}";
        Exception exception = new AlreadyExistsException(path);

        mockJsonResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, loadTestFile("createUnixFileAlreadyExists.json"));

        RequestBuilder requestBuilder = mockPostBuilder("restfiles/fs" + path,
                JsonUtils.readAsJsonElement(json).getAsJsonObject());

        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        shouldThrow(exception, () -> new CreateUnixAssetZosmfRunner(path, createAssetRequest).run(zosmfConnector));
        verifyInteractions(requestBuilder);
    }

    @Test
    public void create_unix_asset_with_invalid_permission_characters_throws_bad_permissions_error() throws Exception {
        createUnixAssetBadPermissionsTestWithException("/u/newFile", "123rwxrwxr");
    }

    @Test
    public void create_unix_asset_with_too_many_permissions_chars_throws_bad_permissions_error() throws Exception {
        createUnixAssetBadPermissionsTestWithException("/u/newFile", "rwxrwxrwxr");
    }

    private void createUnixAssetBadPermissionsTestWithException(String path, String permissions) throws Exception {
        UnixCreateAssetRequest createAssetRequest = new UnixCreateAssetRequest(UnixEntityType.FILE, permissions);
        Exception exception = new InvalidPermissionsException(permissions);

        shouldThrow(exception, () -> new CreateUnixAssetZosmfRunner(path, createAssetRequest).run(zosmfConnector));
    }
}
