package com.gjh.shopdemo.config;

import com.gjh.shopdemo.filter.TokenFilter;
import com.gjh.shopdemo.filter.TraceFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<TraceFilter> traceFilterRegistration(TraceFilter traceFilter) {
        FilterRegistrationBean<TraceFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(traceFilter);
        // 拦截路径
        registration.addUrlPatterns("/*");
        // 顺序
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<TokenFilter> tokenFilterRegistration(TokenFilter tokenFilter) {
        FilterRegistrationBean<TokenFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(tokenFilter);
        // 拦截路径
        registration.addUrlPatterns("/*");
        registration.addInitParameter("exclusions", "/login");
        registration.addInitParameter("exclusions", "/register");
        // 顺序
        registration.setOrder(2);
        return registration;
    }
}
