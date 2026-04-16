package com.gjh.shopdemo.config;

import com.gjh.shopdemo.filter.TraceFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<TraceFilter> traceFilterRegistration() {
        FilterRegistrationBean<TraceFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceFilter());
        // 拦截路径
        registration.addUrlPatterns("/*");
        // 顺序
        registration.setOrder(1);
        return registration;
    }
}
