/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */

package org.zowe.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.zowe.filter.GZipServletFilter;

import java.util.List;

@Configuration
public class AppConfig {
        
    @Value("${server.customCompression.mime-types}")
    private List<String> mimeTypes;

    
    @Bean
    @ConditionalOnProperty(prefix = "server", name = "customCompression.enabled")
    public FilterRegistrationBean<GZipServletFilter> filterRegistrationBean() {
        GZipServletFilter gzipFilter = new GZipServletFilter(mimeTypes);
        FilterRegistrationBean<GZipServletFilter> registration = new FilterRegistrationBean<>(gzipFilter);
        registration.addUrlPatterns("/api/v1/datasets/*");
        registration.addUrlPatterns("/api/v1/unixfiles/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
