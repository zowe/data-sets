/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */
package org.zowe.data.sets.services;

import org.zowe.api.common.model.ItemsWrapper;
import org.zowe.data.sets.model.DataSet;
import org.zowe.data.sets.model.DataSetAttributes;
import org.zowe.data.sets.model.DataSetContentWithEtag;
import org.zowe.data.sets.model.DataSetCreateRequest;
import org.zowe.data.sets.model.DataSetRenameRequest;

public interface DataSetService {

    ItemsWrapper<DataSetAttributes> listDataSetAttributes(String filter, String authToken);

    ItemsWrapper<DataSet> listDataSets(String filter, String authToken);

    ItemsWrapper<String> listDataSetMembers(String dataSetName, String authToken);

    DataSetContentWithEtag getContent(String dataSetName, String authToken);

    String putContent(String dataSetName, DataSetContentWithEtag content, String authToken);

    String createDataSet(DataSetCreateRequest input, String authToken);

    void deleteDataSet(String dataSetName, String authToken);

    String renameDataSet(String oldDataSetName, DataSetRenameRequest input, String authToken);
}
