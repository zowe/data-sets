/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019,2020
 */
package org.zowe.data.sets.services.zosmf;

import com.google.gson.JsonObject;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.RequestBuilder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.zowe.api.common.exceptions.ZoweApiRestException;
import org.zowe.api.common.test.services.zosmf.AbstractZosmfRequestRunnerTest;
import org.zowe.api.common.utils.JsonUtils;
import org.zowe.data.sets.exceptions.DataSetAlreadyExists;
import org.zowe.data.sets.exceptions.InvalidDirectoryBlockException;
import org.zowe.data.sets.model.AllocationUnitType;
import org.zowe.data.sets.model.DataSetCreateRequest;
import org.zowe.data.sets.model.DataSetOrganisationType;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CreateDataSetZosmfRequestRunner.class })
public class CreateDataSetZosmfRequestRunnerTest extends AbstractZosmfRequestRunnerTest {

    // TODO - Add tests for dsnType = library for zosmf 2.3 - what else fails on 2.2? https://github.com/zowe/data-sets/issues/47

    @Test
    public void create_example_sds_dataset_should_transform_request_to_zosmf() throws Exception {
        String json = "{\"volser\":\"zmf046\",\"unit\":\"3390\",\"dsorg\":\"PS\",\"alcunit\":\"TRK\",\"primary\":10,\"secondary\":5,"
                + "\"avgblk\":500,\"recfm\":\"FB\",\"blksize\":400,\"lrecl\":80}";
        JsonObject zosmfRequest = JsonUtils.readAsJsonElement(json).getAsJsonObject();

        create_dataset_and_verify("STEVENH.TEST.SDS",
                createBaseRequest().dataSetOrganization(DataSetOrganisationType.PS), zosmfRequest);
    }

    @Test
    public void create_example_pds_dataset_should_transform_request_to_zosmf() throws Exception {
        String json = "{\"volser\":\"zmf046\",\"unit\":\"3390\",\"dsorg\":\"PO\",\"alcunit\":\"TRK\",\"primary\":10,\"secondary\":5,\""
                + "dirblk\":10,\"avgblk\":500,\"recfm\":\"FB\",\"blksize\":400,\"lrecl\":80}";
        JsonObject zosmfRequest = JsonUtils.readAsJsonElement(json).getAsJsonObject();

        create_dataset_and_verify("STEVENH.TEST.PDS",
                createBaseRequest().dataSetOrganization(DataSetOrganisationType.PO).directoryBlocks(10), zosmfRequest);
    }

    @Test
    public void create_example_pdse_dataset_should_transform_request_to_zosmf_and_add_dsntype() throws Exception {
        String json = "{\"volser\":\"zmf046\",\"unit\":\"3390\",\"dsorg\":\"PO\",\"dsntype\":\"LIBRARY\",\"alcunit\":\"TRK\",\"primary\":10,\"secondary\":5,\""
                + "dirblk\":10,\"avgblk\":500,\"recfm\":\"FB\",\"blksize\":400,\"lrecl\":80}";
        JsonObject zosmfRequest = JsonUtils.readAsJsonElement(json).getAsJsonObject();

        create_dataset_and_verify("STEVENH.TEST.PDSE",
                createBaseRequest().dataSetOrganization(DataSetOrganisationType.PO_E).directoryBlocks(10), zosmfRequest);
    }

    @Test
    public void create_example_pds_dataset_with_cylinders_should_transform_request_to_zosmf() throws Exception {
        int primary = 300;
        int secondary = 150;
        int dirblk = 20;
        int lrecl = 133;
        int blksize = 0;
        String recfm = "VB";

        String json = "{\"volser\":\"zmf046\",\"unit\":\"3390\",\"dsorg\":\"PO\",\"alcunit\":\"CYL\",\"primary\":"
                + primary + ",\"secondary\":" + secondary + ",\"" + "dirblk\":" + dirblk
                + ",\"avgblk\":500,\"recfm\":\"" + recfm + "\",\"blksize\":" + blksize + ",\"lrecl\":" + lrecl
                + ",\"alcunit\":\"CYL\"}";
        JsonObject zosmfRequest = JsonUtils.readAsJsonElement(json).getAsJsonObject();

        create_dataset_and_verify("STEVENH.TEST.PDS",
                createBaseRequest().dataSetOrganization(DataSetOrganisationType.PO).recordFormat(recfm).primary(primary)
                    .secondary(secondary).directoryBlocks(dirblk).blockSize(blksize).recordLength(lrecl)
                    .allocationUnit(AllocationUnitType.CYLINDER),
                zosmfRequest);
    }

    @Test
    public void create_example_pds_dataset_blocks_fails() throws Exception {
        String json = "{\"volser\":\"zmf046\",\"unit\":\"3390\",\"dsorg\":\"PO\",\"alcunit\":\"TRK\",\"primary\":10,\"secondary\":5,\""
                + "dirblk\":10,\"avgblk\":500,\"recfm\":\"FB\",\"blksize\":400,\"lrecl\":80}";
        JsonObject zosmfRequest = JsonUtils.readAsJsonElement(json).getAsJsonObject();

        Exception expected = new IllegalArgumentException(
                "Creating data sets with a z/OS MF connector only supports allocation unit type of track and cylinder");
        shouldThrow(expected,
                () -> create_dataset_and_verify("STEVENH.TEST.PDS", createBaseRequest()
                    .dataSetOrganization(DataSetOrganisationType.PO).allocationUnit(AllocationUnitType.BLOCK),
                        zosmfRequest));
    }

    private void create_dataset_and_verify(String dataSetName,
            DataSetCreateRequest.DataSetCreateRequestBuilder requestBuilder, JsonObject zosmfRequest)
            throws IOException, ClientProtocolException, Exception {
        DataSetCreateRequest request = requestBuilder.name(dataSetName).build();

        mockResponseCache(HttpStatus.SC_CREATED);

        RequestBuilder builder = mockPostBuilder(String.format("restfiles/ds/%s", dataSetName), zosmfRequest);

        when(zosmfConnector.executeRequest(builder)).thenReturn(response);

        assertEquals(dataSetName, new CreateDataSetZosmfRequestRunner(request, new ArrayList<>()).run(zosmfConnector));

        verifyInteractions(builder);
    }

    @Test
    public void create_sds_with_dirBlk_fails() throws Exception {
        String name = "DUMM";
        DataSetCreateRequest request = DataSetCreateRequest.builder().name(name)
            .dataSetOrganization(DataSetOrganisationType.PS).directoryBlocks(12).build();

        Exception expected = new InvalidDirectoryBlockException(name);
        shouldThrow(expected, () -> new CreateDataSetZosmfRequestRunner(request, new ArrayList<>()).run(zosmfConnector));
        verifyNoMoreInteractions(zosmfConnector);
    }

    @Test
    @Ignore("TODO - work out how to decipher the dynamic allocation error codes")
    public void create_data_set_which_already_exists_throws_correct_error() throws Exception {
        String dataSetName = "STEVENH.EXISTS";

        Exception expectedException = new DataSetAlreadyExists(dataSetName);
        checkCreateDataSetExceptionAndVerify(dataSetName, expectedException, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "createDataSet_exists.json");
    }

    @Test
    public void create_data_set_with_unknown_error_throws_correct_error() throws Exception {
        String dataSetName = "STEVENH.JUNK";
        Exception expectedException = new ZoweApiRestException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, loadTestFile("createDataSet_unknown.json"));
        checkCreateDataSetExceptionAndVerify(dataSetName, expectedException, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                "createDataSet_unknown.json");
    }

    private void checkCreateDataSetExceptionAndVerify(String dataSetName, Exception expectedException, int statusCode,
            String file) throws IOException, Exception {

        String json = "{\"volser\":\"zmf046\",\"unit\":\"3390\",\"dsorg\":\"PS\",\"alcunit\":\"TRK\",\"primary\":10,\"secondary\":5,\""
                + "avgblk\":500,\"recfm\":\"FB\",\"blksize\":400,\"lrecl\":80}";
        JsonObject zosmfRequest = JsonUtils.readAsJsonElement(json).getAsJsonObject();
        DataSetCreateRequest request = createBaseRequest().dataSetOrganization(DataSetOrganisationType.PS)
            .name(dataSetName).build();

        mockJsonResponse(statusCode, loadTestFile(file));
        RequestBuilder builder = mockPostBuilder(String.format("restfiles/ds/%s", dataSetName), zosmfRequest);

        when(zosmfConnector.executeRequest(builder)).thenReturn(response);

        shouldThrow(expectedException, () -> new CreateDataSetZosmfRequestRunner(request, new ArrayList<>()).run(zosmfConnector));
        verifyInteractions(builder);
    }

    private DataSetCreateRequest.DataSetCreateRequestBuilder createBaseRequest() {
        return DataSetCreateRequest.builder().volumeSerial("zmf046").deviceType("3390")
            .allocationUnit(AllocationUnitType.TRACK).primary(10).secondary(5).recordFormat("FB").blockSize(400)
            .recordLength(80).averageBlock(500);
    }
}
