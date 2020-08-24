/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2020
 */
package org.zowe.data.sets.services;

import lombok.Setter;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.zowe.api.common.model.ItemsWrapper;
import org.zowe.data.sets.model.DataSet;
import org.zowe.data.sets.model.DataSetAttributes;
import org.zowe.data.sets.model.DataSetContentWithEtag;
import org.zowe.data.sets.model.DataSetCreateRequest;
import org.zowe.data.sets.model.DataSetRenameRequest;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Setter
public abstract class DataSetService {
    
    private HttpServletRequest request;
    
    public List<Header> getIbmHeadersFromRequest() {
        ArrayList<Header> ibmHeaders = new ArrayList<Header>();
        if (request != null ) {
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement().toUpperCase();
                if (headerName.contains("X-IBM")) {
                    Header newHeader = new BasicHeader(headerName, request.getHeader(headerName));
                    ibmHeaders.add(newHeader);
                }
            }
        }
        return ibmHeaders;
    }

    public abstract ItemsWrapper<DataSetAttributes> listDataSetAttributes(String filter);

    public abstract ItemsWrapper<DataSet> listDataSets(String filter);

    public abstract ItemsWrapper<String> listDataSetMembers(String dataSetName);

    public abstract DataSetContentWithEtag getContent(String dataSetName);

    public abstract String putContent(String dataSetName, DataSetContentWithEtag content);

    public abstract String createDataSet(DataSetCreateRequest input);

    public abstract void deleteDataSet(String dataSetName);

    public abstract String renameDataSet(String oldDataSetName, DataSetRenameRequest input);
}
