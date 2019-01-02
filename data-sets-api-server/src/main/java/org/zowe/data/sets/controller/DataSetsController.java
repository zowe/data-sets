/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2016, 2018
 */
package org.zowe.data.sets.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zowe.api.common.utils.ZosUtils;
import org.zowe.data.sets.services.DataSetService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/datasets")
@Api(value = "Data Sets APIs")
public class DataSetsController {

    private static final Logger log = LoggerFactory.getLogger(DataSetsController.class);

    @Autowired
    private DataSetService dataSetService;

    @GetMapping(value = "/username", produces = { "application/json" })
    @ApiOperation(value = "Get current userid", nickname = "getCurrentUserName", notes = "This API returns the caller's current TSO userid.", response = String.class, tags = {
            "System APIs", })
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Ok", response = String.class) })
    public String getCurrentUserName() {
        return ZosUtils.getUsername();
    }

    @GetMapping(value = "{dataSetName}/members", produces = { "application/json" })
    @ApiOperation(value = "Get a list of members for a partitioned data set", nickname = "getMembers", notes = "This API returns a list of members for a given partitioned data set.", tags = "Data Sets APIs")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List") })

    public List<String> getMembers(
            @ApiParam(value = "Partitioned data set name", required = true) @PathVariable String dataSetName) {
        return dataSetService.listDataSetMembers(dataSetName);
    }
}
