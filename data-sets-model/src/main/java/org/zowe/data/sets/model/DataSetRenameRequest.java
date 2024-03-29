/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2020
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
@Builder
@NoArgsConstructor
@AllArgsConstructor

@JsonInclude(Include.NON_NULL)
@Schema(title = "DataSetRenameRequest", description = "Reaname data set request payload with attributes from-dataset to-dataset")
public class DataSetRenameRequest {

    @Schema(description = "new dataset name", requiredMode = Schema.RequiredMode.REQUIRED, example = "HLQ.ZOWE or HLQ.ZOWE(mem1)")
    private String newName;
}