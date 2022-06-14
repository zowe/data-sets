/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018
 */
package org.zowe.spring;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;

import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private static final ApiResponse response401 = new ApiResponse().description("Unauthorized");
    private static final ApiResponse response403 = new ApiResponse().description("Forbidden");
    private static final ApiResponse response404 = new ApiResponse().description("Not Found");

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Files API")
                .description("REST API for the Data sets and z/OS Unix Files Services")
                .version("1.0.0")
        );
    }

    @Bean
    public OpenApiCustomiser genericApiResponsesCustomizer() {
        return openApi -> openApi.getPaths().forEach((key, pathEntry) -> pathEntry.readOperations().forEach(op -> {
                    op.getResponses().addApiResponse("401", response401);
                    op.getResponses().addApiResponse("403", response403);
                    op.getResponses().addApiResponse("404", response404);
                }
        ));
    }
}