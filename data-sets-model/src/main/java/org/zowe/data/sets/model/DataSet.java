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
@ApiModel(value = "DataSet", description = "List of data set")
public class DataSet {
    // TODO - match this with Create request
    @ApiModelProperty(value = "Data set name", required = true)
    private String name;
    @ApiModelProperty(value = "Whether the data set is migrated")
    private Boolean migrated;
}