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

@Data
@Builder // TODO - required? (toBuilder = true, builderMethodName = "createBuilder")
@NoArgsConstructor
@AllArgsConstructor

@JsonInclude(Include.NON_NULL)
@ApiModel(value = "DataSetCreateRequest", description = "Attributes of a data set to be created")
public class DataSetCreateRequest {

    @ApiModelProperty(value = "Data set name", required = true)
    private String name;
    @ApiModelProperty(value = "Volume")
    private String volser;
    @ApiModelProperty(value = "Device type")
    private String unit;
    // we can support PO-E in 2.3 - need some validation
    @ApiModelProperty(value = "Data set organization", dataType = "string", required = true, allowableValues = "PO, PS, (PO_E in z/OS 2.3 only)")
    private DataSetOrganisationType dsorg;
    @ApiModelProperty(value = "Unit of space allocation", dataType = "string", required = true, allowableValues = "TRACK, CYLINDER, BLOCK")
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

    // TODO - work out PDSE support for zosmf 2.3+
    // @ApiModelProperty(value = "Data set", required = true)
    // private Integer dsntype;

    // not valid in create - seperate into super model?
    // @ApiModelProperty(value = "Allocate size in tracks")
    // private String sizex;
    // @ApiModelProperty(value = "Current allocate space units")
    // private String spacu;
    // @ApiModelProperty(value = "Percentage of allocation used")
    // private String used;
    // @ApiModelProperty(value = "Whether the data set is migrated")
    // private Boolean migrated;
    // @ApiModelProperty(value = "Catalog name")
    // private String catnm;
    // @ApiModelProperty(value = "Creation date")
    // private String cdate;
    // @ApiModelProperty(value = "Device e.g. 3390")
    // private String dev;
    // @ApiModelProperty(value = "Expiration date")
    // private String edate;
}