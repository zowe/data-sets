/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.uss.files.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zowe.data.sets.model.UnixDirectoryAttributesWithChildren;
import org.zowe.data.sets.services.DataSetService;

@RestController
@RequestMapping("/api/v1/unixfiles")
@Api(value = "Unix Files APIs")
public class UnixFilesController {
    
    @Autowired
    private DataSetService dataSetService;
    
    @GetMapping(value = "", produces = { "application/json" })
    @ApiOperation(value = "Get a list of a directories contents", nickname = "getDirectoryListing", notes = "This API gets a list of files and directories for a given path", tags = "Unix Files APIs")
    @ApiResponses({ @ApiResponse(code = 200, message = "Ok", response = String.class, responseContainer = "List")})
    public UnixDirectoryAttributesWithChildren getDirectoryListing(
            @ApiParam(value = "Path of Directory to be listed", required = true) @RequestParam String path) {
        return dataSetService.listUnixDirectory(path);
    }
}
