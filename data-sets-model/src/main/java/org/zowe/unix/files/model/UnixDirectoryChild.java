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

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor

@JsonInclude(Include.NON_NULL)
@Schema(title = "UnixDirectoryChild", description = "Child of a unix directory")
public class UnixDirectoryChild {

    @Schema(description = "Path", required = true)
    private String name;

    @Schema(description = "type", required = true)
    private UnixEntityType type;

    @Schema(description = "size", required = true)
    private Integer size;

    @Schema(description = "lastModified", required = true)
    private String lastModified;

    @Schema(description = "Link", required = true)
    private String link;
}
