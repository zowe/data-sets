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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@JsonInclude(Include.NON_NULL)
public class ZosmfCreateRequest {
    private static final String PDSE_DSNTYPE = "LIBRARY";

    private String volser;
    private String unit;
    private String dsorg;
    private String dsntype;
    private String alcunit;

    private Integer primary;
    private Integer secondary;
    private Integer dirblk;
    private Integer avgblk;

    private String recfm;
    private Integer blksize;
    private Integer lrecl;

    public static ZosmfCreateRequest createFromDataSetCreateRequest(DataSetCreateRequest request) {
        ZosmfCreateRequestBuilder builder = builder()
                .volser(request.getVolumeSerial())
                .unit(request.getDeviceType())
                .dsorg(checkDataSetOrganization(request.getDataSetOrganization().getZosmfName()))
                .dsntype(retrieveDataSetNameType(request.getDataSetOrganization()))
                .primary(request.getPrimary())
                .secondary(request.getSecondary())
                .dirblk(request.getDirectoryBlocks())
                .avgblk(request.getAverageBlock())
                .recfm(request.getRecordFormat())
                .blksize(request.getBlockSize())
                .lrecl(request.getRecordLength());

        switch (request.getAllocationUnit()) {
            case TRACK:
                builder.alcunit("TRK");
                break;
            case CYLINDER:
                builder.alcunit("CYL");
                break;
            default:
                throw new IllegalArgumentException(
                        "Creating data sets with a z/OS MF connector only supports allocation unit type of track and cylinder");
        }

        return builder.build();
    }

    /**
     * Check if the data set organisation is acceptable on z/OS and replace it with valid one if it is needed.
     *
     * @param dsorg - data set organisation to check
     * @return dsorg or valid value
     */
    private static String checkDataSetOrganization(String dsorg) {
        return DataSetOrganisationType.PO_E.getZosmfName().equals(dsorg) ?
                DataSetOrganisationType.PO.getZosmfName() :
                dsorg;
    }

    /**
     * Retrieve data set name type based on data set organization if required or null.
     * PDSE data sets (with dsorg=PO_E) requires dsntype=LIBRARY to be allocated.
     *
     * @param dsorg - data set organization to recognise if data set name type is required
     * @return dsntype based on dsorg or null if not required
     */
    private static String retrieveDataSetNameType(DataSetOrganisationType dsorg) {
        return DataSetOrganisationType.PO_E == dsorg ? PDSE_DSNTYPE : null;
    }
}