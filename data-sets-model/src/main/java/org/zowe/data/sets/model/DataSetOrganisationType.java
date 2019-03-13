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

@ApiModel
public enum DataSetOrganisationType {
    PO("PO"), // Partitioned
    POU("POU"), // Partitioned unmovable
    PO_E("PO-E"), // Partitioned extended (PDSE)
    PS("PS"), // Sequential
    PS_E("PS-E"), // Sequential Extended Format
    PS_L("PS-L"), // Large Format Sequential
    PSU("PSU"), // Sequential unmovable
    VSAM("VS"), // VSAM
    VSAM_E("VS-E"), // VSAM Extended Format
    HFS("HFS"), // MVS Hierarchical File System
    ZFS("ZFS"), DA("DA"), // Direct
    DAU("DAU"); // Direct unmovable

    String zosmfName;

    private DataSetOrganisationType(String zosmfName) {
        this.zosmfName = zosmfName;
    }

    public static DataSetOrganisationType getByZosmfName(String zosmfName) {
        for (DataSetOrganisationType state : DataSetOrganisationType.values()) {
            if (state.zosmfName.equals(zosmfName)) {
                return state;
            }
        }
        throw new IllegalArgumentException(
                "Character: " + zosmfName + " was not a recognised data set organisation type");
    }

    public String getZosmfName() {
        return zosmfName;
    }
}