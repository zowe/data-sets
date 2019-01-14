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
    @ApiModelProperty(value = "Volume serial")
    private String volumeSerial;
    @ApiModelProperty(value = "Device type")
    private String deviceType;
    // we can support PO-E in 2.3 and read VS. How to reconcil this?
    @ApiModelProperty(value = "Data set organization", dataType = "string")
    private DataSetOrganisationType dataSetOrganization;

    @ApiModelProperty(value = "Unit of space allocation, alcunit, spaceu", dataType = "string")
    private AllocationUnitType allocationUnit;

    @ApiModelProperty(value = "Primary space allocation")
    private Integer primary;
    @ApiModelProperty(value = "Secondary space allocation")
    private Integer secondary;
    @ApiModelProperty(value = "Number of directory blocks, dirblk")
    private Integer directoryBlocks;
    @ApiModelProperty(value = "Average block")
    private Integer averageBlock;

    // TODO convert to enum once we know which formats z/OS MF works with?
    @ApiModelProperty(value = "Record format, recfm", required = true)
    private String recordFormat;
    @ApiModelProperty(value = "Block size, blksize")
    private Integer blockSize;
    @ApiModelProperty(value = "Record length, lrecl", required = true)
    private Integer recordLength;
}