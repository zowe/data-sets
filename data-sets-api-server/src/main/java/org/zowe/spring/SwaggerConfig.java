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

import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private static final String V1 = "1.0.0";
    private static final String V2 = "2.0.0";
    private static final String DATASETS_TITLE = "Datasets API";
    private static final String DATASETS_DESCRIPTION = "REST API for the Data sets Service";
    private static final String UNIXFILES_TITLE = "Unix Files API";
    private static final String UNIXFILES_DESCRIPTION = "REST API for the z/OS Unix Files Service";

    private static final GenericApiResponseCustomizer GENERIC_API_RESPONSE_CUSTOMIZER = new GenericApiResponseCustomizer();

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Files API")
                .description("REST API for the Data sets and z/OS Unix Files Services")
                .version(V2));
    }

    private static class GenericApiResponseCustomizer implements OpenApiCustomiser {
        private static final ApiResponse response401 = new ApiResponse().description("Unauthorized");
        private static final ApiResponse response403 = new ApiResponse().description("Forbidden");
        private static final ApiResponse response404 = new ApiResponse().description("Not Found");

        @Override
        public void customise(OpenAPI openApi) {
            openApi.getPaths().forEach((key, pathEntry) -> pathEntry.readOperations().forEach(op -> {
                        op.getResponses().addApiResponse("401", response401);
                        op.getResponses().addApiResponse("403", response403);
                        op.getResponses().addApiResponse("404", response404);
                    }
            ));
        }
    }

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("all")
                .pathsToMatch("/api/**")
                .addOpenApiCustomiser(GENERIC_API_RESPONSE_CUSTOMIZER)
                .build();
    }

    @Bean
    public GroupedOpenApi apiV1Datasets() {
        return GroupedOpenApi.builder()
                .group("datasetsV1")
                .pathsToMatch("/api/v1/datasets/**")
                .addOpenApiCustomiser(openApi -> openApi.setInfo(openApi.getInfo().title(DATASETS_TITLE)))
                .addOpenApiCustomiser(openApi -> openApi.setInfo(openApi.getInfo().description(DATASETS_DESCRIPTION)))
                .addOpenApiCustomiser(openApi -> openApi.setInfo(openApi.getInfo().version(V1)))
                .addOpenApiCustomiser(GENERIC_API_RESPONSE_CUSTOMIZER)
                .build();
    }

    @Bean
    public GroupedOpenApi apiV2Datasets() {
        return GroupedOpenApi.builder()
                .group("datasetsV2")
                .pathsToMatch("/api/v2/datasets/**")
                .addOpenApiCustomiser(openApi -> openApi.setInfo(openApi.getInfo().title(DATASETS_TITLE)))
                .addOpenApiCustomiser(openApi -> openApi.setInfo(openApi.getInfo().description(DATASETS_DESCRIPTION)))
                .addOpenApiCustomiser(GENERIC_API_RESPONSE_CUSTOMIZER)
                .build();
    }

    @Bean
    public GroupedOpenApi apiV1UnixFiles() {
        return GroupedOpenApi.builder()
                .group("unixfilesV1")
                .pathsToMatch("/api/v1/unixfiles/**")
                .addOpenApiCustomiser(openApi -> openApi.setInfo(openApi.getInfo().title(UNIXFILES_TITLE)))
                .addOpenApiCustomiser(openApi -> openApi.setInfo(openApi.getInfo().description(UNIXFILES_DESCRIPTION)))
                .addOpenApiCustomiser(openApi -> openApi.setInfo(openApi.getInfo().version(V1)))
                .addOpenApiCustomiser(GENERIC_API_RESPONSE_CUSTOMIZER)
                .build();
    }

    @Bean
    public GroupedOpenApi apiV2UnixFiles() {
        return GroupedOpenApi.builder()
                .group("unixfilesV2")
                .pathsToMatch("/api/v2/unixfiles/**")
                .addOpenApiCustomiser(openApi -> openApi.setInfo(openApi.getInfo().title(UNIXFILES_TITLE)))
                .addOpenApiCustomiser(openApi -> openApi.setInfo(openApi.getInfo().description(UNIXFILES_DESCRIPTION)))
                .addOpenApiCustomiser(GENERIC_API_RESPONSE_CUSTOMIZER)
                .build();
    }
}