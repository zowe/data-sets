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

import org.zowe.data.sets.model.DataSetAttributes;
import org.zowe.data.sets.model.DataSetContent;
import org.zowe.data.sets.model.DataSetCreateRequest;

import java.util.List;

public interface DataSetService {

    List<DataSetAttributes> listDataSets(String filter);

    List<String> listDataSetMembers(String dataSetName);

    DataSetContent getContent(String dataSetName);

    void putContent(String dataSetName, DataSetContent content);

    String createDataSet(DataSetCreateRequest input);

    void deleteDataSet(String dataSetName);
}
