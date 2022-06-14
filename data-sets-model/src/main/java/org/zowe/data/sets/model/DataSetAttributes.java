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

import io.swagger.v3.oas.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor

@JsonInclude(Include.NON_NULL)
@Schema(title = "DataSetAttributes", description = "Attributes of a data set")
public class DataSetAttributes {

    // TODO - match this with Create request
    @Schema(description = "Data set name", required = true)
    private String name;
    @Schema(description = "Volume serial")
    private String volumeSerial;
    @Schema(description = "Device type")
    private String deviceType;
    // we can support PO-E in 2.3 and read VS. How to reconcil this?
    @Schema(description = "Data set organization")
    private DataSetOrganisationType dataSetOrganization;

    @Schema(description = "Unit of space allocation, alcunit, spaceu")
    private AllocationUnitType allocationUnit;

    @Schema(description = "Primary space allocation")
    private Integer primary;
    @Schema(description = "Secondary space allocation")
    private Integer secondary;
    @Schema(description = "Number of directory blocks, dirblk")
    private Integer directoryBlocks;
    @Schema(description = "Average block")
    private Integer averageBlock;

    // TODO convert to enum once we know which formats z/OS MF works with?
    @Schema(description = "Record format, recfm", required = true)
    private String recordFormat;
    @Schema(description = "Block size, blksize")
    private Integer blockSize;
    @Schema(description = "Record length, lrecl", required = true)
    private Integer recordLength;

    // TODO - dsnType - dataSetNameType https://github.com/zowe/data-sets/issues/30
    // TODO - extx, rdate vol, mvol, ovf
    // https://www.ibm.com/support/knowledgecenter/en/SSLTBW_2.3.0/com.ibm.zos.v2r3.izua700/IZUHPINFO_API_RESTFILES_JSON_Documents.htm?

    // not valid in create - seperate into super model?
    @Schema(description = "Allocate size, sizex")
    private Integer allocatedSize;
    @Schema(description = "Percentage of allocation used")
    private Integer used;
    @Schema(description = "Whether the data set is migrated")
    private Boolean migrated;
    @Schema(description = "Catalog name, catnm")
    private String catalogName;
    @Schema(description = "Creation date, cdate") // TODO should we make this a date object?
    private String creationDate;
    @Schema(description = "Expiration date, edate") // TODO should we make this a date object?
    private String expirationDate;
}