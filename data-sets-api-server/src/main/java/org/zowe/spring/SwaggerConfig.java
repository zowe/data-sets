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


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    @Primary
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("all")
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/api.*"))
                .build()
                .apiInfo(
                        new ApiInfo("Files API", "REST API for the Data sets and z/OS Unix Files Services", "1.0", null, null, null, null, Collections.emptyList())
                );
    }

    @Bean
    public Docket apiV1Datasets() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("apiV1Datasets")
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/api/v1/datasets.*"))
                .build()
                .apiInfo(
                        new ApiInfo("Datasets API", "REST API for the Data sets Service", "1.0", null, null, null, null, Collections.emptyList())
                );
    }

    @Bean
    public Docket apiV2Datasets() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("apiV2Datasets")
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/api/v2/datasets.*"))
                .build()
                .apiInfo(
                        new ApiInfo("Datasets API", "REST API for the Data sets Service", "2.0", null, null, null, null, Collections.emptyList())
                );
    }

    @Bean
    public Docket apiV1UnixFiles() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("apiV1UnixFiles")
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/api/v1/unixfiles.*"))
                .build()
                .apiInfo(
                        new ApiInfo("Unix Files API", "REST API for the z/OS Unix Files Service", "1.0", null, null, null, null, Collections.emptyList())
                );
    }

    @Bean
    public Docket apiV2UnixFiles() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("apiV2UnixFiles")
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/api/v2/unixfiles/.*"))
                .build()
                .apiInfo(
                        new ApiInfo("Unix Files API", "REST API for the z/OS Unix Files Service", "2.0", null, null, null, null, Collections.emptyList())
                );
    }
}