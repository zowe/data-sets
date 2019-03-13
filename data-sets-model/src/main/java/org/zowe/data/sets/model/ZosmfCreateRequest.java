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

    private String volser;
    private String unit;
    private String dsorg;
    private String alcunit;

    private Integer primary;
    private Integer secondary;
    private Integer dirblk;
    private Integer avgblk;

    private String recfm;
    private Integer blksize;
    private Integer lrecl;

    public static ZosmfCreateRequest createFromDataSetCreateRequest(DataSetCreateRequest request) {
        ZosmfCreateRequestBuilder builder = builder().volser(request.getVolumeSerial()).unit(request.getDeviceType())
            .dsorg(request.getDataSetOrganization().getZosmfName()).primary(request.getPrimary())
            .secondary(request.getSecondary()).dirblk(request.getDirectoryBlocks()).avgblk(request.getAverageBlock())
            .recfm(request.getRecordFormat()).blksize(request.getBlockSize()).lrecl(request.getRecordLength());

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

}