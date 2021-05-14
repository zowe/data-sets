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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.zowe.api.common.model.ItemsWrapper;
import org.zowe.data.sets.model.*;
import org.zowe.data.sets.services.DataSetService;

import java.net.URI;

public abstract class AbstractDataSetsController {

    abstract DataSetService getDataSetService();

    @GetMapping(value = "{dataSetName}/members", produces = {"application/json"})
    @ApiOperation(value = "Get a list of members for a partitioned data set", nickname = "getMembers", notes = "This API returns a list of members for a given partitioned data set.")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Ok")})
    public ItemsWrapper<String> getMembers(
            @ApiParam(value = "Partitioned data set name", required = true) @PathVariable String dataSetName) {
        return getDataSetService().listDataSetMembers(dataSetName);
    }

    @GetMapping(value = "{filter:.+}", produces = {"application/json"})
    @ApiOperation(value = "Get a list of data sets matching the filter", nickname = "getDataSetAttributes", notes = "This API returns the attributes of data sets matching the filter")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Ok")})
    public ItemsWrapper<DataSetAttributes> getDataSetAttributes(
            @ApiParam(value = "Dataset filter string, e.g. HLQ.\\*\\*, \\*\\*.SUF, etc.", required = true) @PathVariable String filter) {
        return getDataSetService().listDataSetAttributes(filter);
    }

    @GetMapping(value = "{filter:.+}/list", produces = {"application/json"})
    @ApiOperation(value = "Get a list of data sets without attributes matching the filter", nickname = "getDataSets", notes = "This API returns the list of data sets matching the filter")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Ok")})
    public ItemsWrapper<DataSet> getDataSets(
            @ApiParam(value = "Dataset filter string, e.g. HLQ.\\*\\*, \\*\\*.SUF, etc.", required = true) @PathVariable String filter) {
        return getDataSetService().listDataSets(filter);
    }

    @GetMapping(value = "{dataSetName}/content", produces = {"application/json"})
    @ApiOperation(value = "Get the content of a sequential data set, or PDS member", nickname = "getContent", notes = "This API reads content from a sequential data set or member of a partitioned data set.")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Ok", response = DataSetContent.class)})
    public ResponseEntity<DataSetContent> getContent(
            @ApiParam(value = "Data set name, e.g. HLQ.PS or HLQ.PO(MEMBER)", required = true) @PathVariable String dataSetName,
            @RequestHeader(value = "X-Return-Etag", required = false) String etagHeader) {
        DataSetContentWithEtag content = getDataSetService().getContent(dataSetName);

        HttpHeaders headers = new HttpHeaders();
        if ("true".equalsIgnoreCase(etagHeader)) {
            headers.add("Access-Control-Expose-Headers", "ETag");
            headers.add("ETag", content.getEtag());
        }
        return new ResponseEntity<>(content.getContent(), headers, HttpStatus.OK);
    }

    @PostMapping(consumes = "application/json")
    @ApiOperation(value = "Create a data set", notes = "This creates a data set based on the attributes passed in")
    @ApiResponses({@ApiResponse(code = 201, message = "Data set successfully created")})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createDataSet(@RequestBody DataSetCreateRequest input) {

        String dataSetName = getDataSetService().createDataSet(input);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{dataSetName}")
                .buildAndExpand(dataSetName).toUri();

        return ResponseEntity.created(location).build();
    }

    @PutMapping(value = "{dataSetName}/content", produces = {"application/json"})
    @ApiOperation(value = "Sets the content of a sequential data set, or PDS member", nickname = "putContent", notes = "This API writes content to a sequential data set or partitioned data set member.")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Ok")})
    public ResponseEntity<?> putContent(
            @ApiParam(value = "Data set name, e.g. HLQ.PS or HLQ.PO(MEMBER)", required = true) @PathVariable String dataSetName,
            @RequestBody DataSetContent input, @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @RequestHeader(value = "X-Return-Etag", required = false) String etagHeader) {
        DataSetContentWithEtag request = new DataSetContentWithEtag(input, ifMatch);
        String putEtag = getDataSetService().putContent(dataSetName, request);

        HttpHeaders headers = new HttpHeaders();
        if ("true".equalsIgnoreCase(etagHeader)) {
            headers.add("Access-Control-Expose-Headers", "ETag");
            headers.add("ETag", "\"" + putEtag + "\"");
        }
        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @PutMapping(value = "{oldDataSetName}/rename", produces = {"application/json"})
    @ApiOperation(value = "Rename of a sequential data set, or PDS member", nickname = "renameContent", notes = "This API renames data set or partitioned data set member.")
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Ok")})
    public ResponseEntity<Void> putRename(
            @ApiParam(value = "Data set name, e.g. HLQ.PS or HLQ.PO(MEMBER)", required = true) @PathVariable String oldDataSetName,
            @RequestBody DataSetRenameRequest input) {
        getDataSetService().renameDataSet(oldDataSetName, input);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "{dataSetName:.+}")
    @ApiOperation(value = "Delete a data set or member", notes = "This API deletes a data set or data set member.")
    @ApiResponses({@ApiResponse(code = 204, message = "Data set or member successfully deleted")})
    public ResponseEntity<?> deleteDatasetMember(
            @ApiParam(value = "Data set name", required = true) @PathVariable String dataSetName) {

        getDataSetService().deleteDataSet(dataSetName);
        return ResponseEntity.noContent().build();
    }
}
