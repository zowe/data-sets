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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "DataSetContent", description = "Data Set file content")
public class DataSetContent {

    @ApiModelProperty(value = "The content of the data set, with \\\\n for new lines", dataType = "string", required = true, example = "//TESTJOBX JOB (),MSGCLASS=H\\n// EXEC PGM=IEFBR14")
    private String records;
}