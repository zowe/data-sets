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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zowe.data.sets.services.DataSetService;

import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/v1/datasets")
@Api(value = "Data Sets V1 APIs", tags = "Data Sets V1 APIs")
public class DataSetsControllerV1 extends AbstractDataSetsController {

    @Autowired
    @Qualifier("ZosmfDataSetService1")
    private DataSetService dataSetService;

    @Override
    DataSetService getDataSetService() {
        return dataSetService;
    }

}
