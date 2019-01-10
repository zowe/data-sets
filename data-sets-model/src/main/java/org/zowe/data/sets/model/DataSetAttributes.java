/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */
package org.zowe.data.sets.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//TODO - give attributes better names? Same for Create

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor

@JsonInclude(Include.NON_NULL)
@ApiModel(value = "DataSetAttributes", description = "Attributes of a data set")
public class DataSetAttributes {

    // TODO - match this with Create request
    @ApiModelProperty(value = "Data set name", required = true)
    private String name;
    @ApiModelProperty(value = "Volume")
    private String volser;
    @ApiModelProperty(value = "Device type")
    private String unit;
    // we can support PO-E in 2.3 and read VS. How to reconcil this?
    @ApiModelProperty(value = "Data set organization", dataType = "string")
    private DataSetOrganisationType dsorg;

    // TODO - zosmf comes back with spaceunits which is the same thing, but allows
    // block, track, or cyclinders
    @ApiModelProperty(value = "Unit of space allocation", dataType = "string")
    private AllocationUnitType alcunit;

    @ApiModelProperty(value = "Primary space allocation")
    private Integer primary;
    @ApiModelProperty(value = "Secondary space allocation")
    private Integer secondary;
    @ApiModelProperty(value = "Number of directory blocks - only valid with a partioned data set")
    private Integer dirblk;
    @ApiModelProperty(value = "Average block")
    private Integer avgblk;

    // TODO convert to enum once we know which formats z/OS MF works with?
    @ApiModelProperty(value = "Record format", required = true)
    private String recfm;
    @ApiModelProperty(value = "Block size")
    private Integer blksize;
    @ApiModelProperty(value = "Record length", required = true)
    private Integer lrecl;

    // not valid in create - seperate into super model?
    @ApiModelProperty(value = "Allocate size")
    private Integer sizex;
    @ApiModelProperty(value = "Current allocate space units")
    private AllocationUnitType spacu;
    @ApiModelProperty(value = "Percentage of allocation used")
    private Integer used;
    @ApiModelProperty(value = "Whether the data set is migrated")
    private Boolean migrated;
    @ApiModelProperty(value = "Catalog name")
    private String catnm;
    @ApiModelProperty(value = "Creation date") // TODO should we make this a date object?
    private String cdate;
    @ApiModelProperty(value = "Device e.g. 3390")
    private String dev;
    @ApiModelProperty(value = "Expiration date") // TODO should we make this a date object?
    private String edate;
}