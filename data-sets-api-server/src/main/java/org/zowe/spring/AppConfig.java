package org.zowe.spring;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.zowe.filter.GZipServletFilter;

@Configuration
public class AppConfig {
    
    @Value("${server.customCompression.enabled}")
    private boolean isCompressionEnabled;
    
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
