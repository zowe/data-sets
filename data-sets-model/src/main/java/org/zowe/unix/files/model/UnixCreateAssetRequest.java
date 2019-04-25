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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor

@ApiModel(value = "UnixCreateAssetRequest", description = "Unix File or Directory attributes for creation")
public class UnixCreateAssetRequest {

    @ApiModelProperty(value = "Unix Entity type, File or Directory", required = true, example = "FILE")
    private UnixEntityType type;
    @ApiModelProperty(value = "Access Mode for new asset", required = false, example = "rwxrw-r--")
    private String permissions;
}
