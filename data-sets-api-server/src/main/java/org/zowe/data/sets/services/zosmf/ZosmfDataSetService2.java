/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */
package org.zowe.data.sets.services.zosmf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zowe.api.common.connectors.zosmf.ZosmfConnectorJWTAuth;
import org.zowe.api.common.model.ItemsWrapper;
import org.zowe.data.sets.model.DataSet;
import org.zowe.data.sets.model.DataSetAttributes;
import org.zowe.data.sets.model.DataSetContentWithEtag;
import org.zowe.data.sets.model.DataSetCreateRequest;
import org.zowe.data.sets.model.DataSetRenameRequest;
import org.zowe.data.sets.services.DataSetService;

@Service("ZosmfDataSetService2")
public class ZosmfDataSetService2 implements DataSetService {

    @Autowired
    ZosmfConnectorJWTAuth zosmfConnector;

    // TODO - review error handling, serviceability, https://github.com/zowe/data-sets/issues/16
    // use the zomsf error categories to work out errors
    // https://www.ibm.com/support/knowledgecenter/SSLTBW_2.3.0/com.ibm.zos.v2r3.izua700/IZUHPINFO_API_RESTFILES_Error_Categories.htm

    @Override
    public ItemsWrapper<String> listDataSetMembers(String dataSetName) {
        ListDataSetMembersZosmfRequestRunner runner = new ListDataSetMembersZosmfRequestRunner(dataSetName);
        return runner.run(zosmfConnector);
    }


    @Override
    public ItemsWrapper<DataSetAttributes> listDataSetAttributes(String filter) {
        ListDataSetsAttributesZosmfRequestRunner runner = new ListDataSetsAttributesZosmfRequestRunner(filter);
        return runner.run(zosmfConnector);
    }

    @Override
    public ItemsWrapper<DataSet> listDataSets(String filter) {
        ListDataSetsZosmfRequestRunner runner = new ListDataSetsZosmfRequestRunner(filter);
        return runner.run(zosmfConnector);
    }

    @Override
    public DataSetContentWithEtag getContent(String dataSetName) {
        GetDataSetContentZosmfRequestRunner runner = new GetDataSetContentZosmfRequestRunner(dataSetName);
        return runner.run(zosmfConnector);
    }

    @Override
    public String putContent(String dataSetName, DataSetContentWithEtag contentWithEtag) {
        PutDataSetContentZosmfRequestRunner runner = new PutDataSetContentZosmfRequestRunner(dataSetName,
                contentWithEtag);
        return runner.run(zosmfConnector);
    }
    
    @Override
    public String renameDataSet(String oldDataSetName, DataSetRenameRequest input) {
        PutDataSetRenameZosmfRequestRunner runner = new PutDataSetRenameZosmfRequestRunner(oldDataSetName, input);
        return runner.run(zosmfConnector);
    }

    @Override
    public String createDataSet(DataSetCreateRequest request) {
        CreateDataSetZosmfRequestRunner runner = new CreateDataSetZosmfRequestRunner(request);
        return runner.run(zosmfConnector);
    }

    @Override
    public void deleteDataSet(String dataSetName) {
        DeleteDataSetZosmfRequestRunner runner = new DeleteDataSetZosmfRequestRunner(dataSetName);
        runner.run(zosmfConnector);
    }
}