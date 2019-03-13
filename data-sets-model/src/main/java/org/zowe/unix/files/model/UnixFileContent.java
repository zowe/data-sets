/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */
package org.zowe.unix.files.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

@ApiModel(value = "UnixFileContent", description = "Unix file content")
public class UnixFileContent {

    @ApiModelProperty(value = "The content of the unix file", dataType = "string", required = true, example = "Hello World")
    private String content;
}