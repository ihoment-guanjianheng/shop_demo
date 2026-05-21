package com.gjh.shopdemo.config;

import com.gjh.shopdemo.context.AuthContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    public static final String X_TRACE_ID = "X-Trace-Id";

    public static final String X_USER_ID = "X-User-Id";

    @Bean
    public RequestInterceptor feignTraceInterceptor() {
        return (RequestTemplate template) -> {
            String traceId = MDC.get("traceId");
            if (traceId != null) {
                template.header(X_TRACE_ID, traceId);
            }
            String userId = AuthContext.getCurrentUser().getId().toString();
            if (userId != null) {
                template.header(X_USER_ID, userId);
            }
        };
    }
}
