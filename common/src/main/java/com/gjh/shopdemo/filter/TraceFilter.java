package com.gjh.shopdemo.filter;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class TraceFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String traceId = req.getHeader("X-Trace-Id");
        MDC.put("traceId", traceId);
        try {
            // 放行，执行后面的过滤器、Controller
            chain.doFilter(request, response);
        } finally {
            // ========== 请求结束后清理 ==========
            MDC.remove("traceId");
        }

    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
