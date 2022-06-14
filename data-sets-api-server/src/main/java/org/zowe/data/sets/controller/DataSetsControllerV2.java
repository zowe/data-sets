/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2016, 2020
 */
package org.zowe.data.sets.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zowe.data.sets.services.DataSetService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v2/datasets")
@Tag(name = "Data Sets V2 APIs")
public class DataSetsControllerV2 extends AbstractDataSetsController {

    @Autowired
    @Qualifier("ZosmfDataSetServiceV2")
    private DataSetService dataSetService;

    @Autowired
    private HttpServletRequest request;

    @Override
    DataSetService getDataSetService() {
        if (request != null) {
            dataSetService.setRequest(request);
        }
        return dataSetService;
    }

}
