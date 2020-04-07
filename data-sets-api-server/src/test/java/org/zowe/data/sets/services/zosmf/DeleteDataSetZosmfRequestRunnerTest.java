/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.data.sets.services.zosmf;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.RequestBuilder;
import org.junit.Test;
import org.zowe.api.common.connectors.zosmf.exceptions.DataSetNotFoundException;
import org.zowe.api.common.test.services.zosmf.AbstractZosmfRequestRunnerTest;

import static org.mockito.Mockito.when;

public class DeleteDataSetZosmfRequestRunnerTest extends AbstractZosmfRequestRunnerTest {

    @Test
    public void delete_data_set_should_call_zosmf_correctly() throws Exception {
        String dataSetName = "STEVENH.TEST.JCL";
        mockResponseCache(HttpStatus.SC_NO_CONTENT);
        RequestBuilder builder = mockDeleteBuilder(String.format("restfiles/ds/%s", dataSetName));

        when(zosmfConnector.executeRequest(builder)).thenReturn(response);

        new DeleteDataSetZosmfRequestRunner(dataSetName).run(zosmfConnector);

        verifyInteractions(builder);
    }

    @Test
    public void delete_data_set_for_non_existing_data_set_should_throw_exception() throws Exception {
        String dataSetName = "STEVENH.TEST";

        Exception expectedException = new DataSetNotFoundException(dataSetName);

        mockJsonResponse(HttpStatus.SC_NOT_FOUND, loadTestFile("deleteDataSet_doesntExist.json"));

        RequestBuilder requestBuilder = mockDeleteBuilder(String.format("restfiles/ds/%s", dataSetName));

        when(zosmfConnector.executeRequest(requestBuilder)).thenReturn(response);

        shouldThrow(expectedException, () -> new DeleteDataSetZosmfRequestRunner(dataSetName).run(zosmfConnector));
        verifyInteractions(requestBuilder);
    }
}
