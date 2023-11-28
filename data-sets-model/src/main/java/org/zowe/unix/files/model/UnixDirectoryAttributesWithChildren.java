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

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(title = "UnixDirectoryAttributesWithChildren", description = "Attributes of a Unix Directory with its children")
public class UnixDirectoryAttributesWithChildren {

    @Schema(description = "Type", required = true)
    private UnixEntityType type;

    @Schema(description = "Owner", required = true)
    private String owner;

    @Schema(description = "Group", required = true)
    private String group;

    @Schema(description = "Symbolic permissions", required = true)
    private String permissionsSymbolic;

    @Schema(description = "Size on disk", required = true)
    private Integer size;

    @Schema(description = "Last Modified", required = true)
    private String lastModified;

    @Schema(description = "Children", required = true)
    private List<UnixDirectoryChild> children;
}
