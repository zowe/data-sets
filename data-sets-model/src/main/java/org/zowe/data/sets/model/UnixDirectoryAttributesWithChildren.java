/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
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

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor

@JsonInclude(Include.NON_NULL)
@ApiModel(value = "UnixDirectoryAttributesWithChildren", description = "Attributes of a Unix Directory with its children")
public class UnixDirectoryAttributesWithChildren {
    
    @ApiModelProperty(value = "type", required = true)
    private UnixEntityType type;
    
    @ApiModelProperty(value = "Owner", required = true)
    private String owner;
    
    @ApiModelProperty(value = "group", required = true)
    private String group;
    
    @ApiModelProperty(value = "Symbolic permissions", required = true)
    private String permissionsSymbolic;
    
    @ApiModelProperty(value = "Size on disk", required = true)
    private Integer size;
    
    @ApiModelProperty(value = "Last Modified", required = true)
    private String lastModified;
    
    @ApiModelProperty(value = "children", required = true)
    private List<UnixDirectoryChild> children;
}
