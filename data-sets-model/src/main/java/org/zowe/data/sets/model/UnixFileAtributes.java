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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor

@JsonInclude(Include.NON_NULL)
@ApiModel(value = "UnixFileAttribues", description = "Attributes of a Unix File or Directory")
public class UnixFileAtributes {
    
    @ApiModelProperty(value = "Path", required = true)
    private String name;
    
    @ApiModelProperty(value = "Access mode", required = true)
    private String accessMode;
    
    @ApiModelProperty(value = "Size on disk", required = true)
    private Integer size;
    
    @ApiModelProperty(value = "User ID value", required = true)
    private String userId;
    
    @ApiModelProperty(value = "Owner", required = true)
    private String user;
    
    @ApiModelProperty(value = "Group ID value", required = true)
    private String groupId;
    
    @ApiModelProperty(value = "Group", required = true)
    private String group;
    
    @ApiModelProperty(value = "Last Modified", required = true)
    private String lastModified;
}
