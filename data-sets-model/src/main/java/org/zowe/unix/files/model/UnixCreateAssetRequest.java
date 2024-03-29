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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor

@Schema(title = "UnixCreateAssetRequest", description = "Unix File or Directory attributes for creation")
public class UnixCreateAssetRequest {

    @Schema(description = "Unix Entity type, File or Directory", requiredMode = Schema.RequiredMode.REQUIRED, example = "FILE")
    private UnixEntityType type;
    @Schema(description = "Access Mode for new asset", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "rwxrw-r--")
    private String permissions;
}
