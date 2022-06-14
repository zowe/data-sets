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

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private static final String V1 = "1.0.0";
    private static final String V2 = "2.0.0";
    private static final String DATASETS_TITLE = "Datasets API";
    private static final String DATASETS_DESCRIPTION = "REST API for the Data sets Service";
    private static final String UNIXFILES_TITLE = "Unix FIles API";
    private static final String UNIXFILES_DESCRIPTION = "REST API for the z/OS Unix Files Service";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("Files API")
                .description("REST API for the Data sets and z/OS Unix Files Services")
                .version(V2));
    }

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("all")
                .pathsToMatch("/api/**")
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
                .build();
    }

    @Bean
    public GroupedOpenApi apiV2Datasets() {
        return GroupedOpenApi.builder()
                .group("datasetsV2")
                .addOpenApiCustomiser(openApi -> openApi.setInfo(openApi.getInfo().title(DATASETS_TITLE)))
                .addOpenApiCustomiser(openApi -> openApi.setInfo(openApi.getInfo().description(DATASETS_DESCRIPTION)))
                .pathsToMatch("/api/v2/datasets/**")
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
                .build();
    }

    @Bean
    public GroupedOpenApi apiV2UnixFiles() {
        return GroupedOpenApi.builder()
                .group("unixfilesV2")
                .addOpenApiCustomiser(openApi -> openApi.setInfo(openApi.getInfo().title(UNIXFILES_TITLE)))
                .addOpenApiCustomiser(openApi -> openApi.setInfo(openApi.getInfo().description(UNIXFILES_DESCRIPTION)))
                .pathsToMatch("/api/v2/unixfiles/**")
                .build();
    }
}