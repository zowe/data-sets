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
@ApiModel(value = "UnixDirectoryChild", description = "Child of a unix directory")
public class UnixDirectoryChild {
    
    @ApiModelProperty(value = "Path", required = true)
    private String name;
    
    @ApiModelProperty(value = "type", required = true)
    private UnixEntityType type; 
    
    @ApiModelProperty(value = "size", required = true)
    private Integer size; 
    
    @ApiModelProperty(value = "lastModified", required = true)
    private String lastModified; 
    
    @ApiModelProperty(value = "Link", required = true)
    private String link;
}
