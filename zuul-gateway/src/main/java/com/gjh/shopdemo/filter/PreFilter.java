package com.gjh.shopdemo.filter;

import com.gjh.shopdemo.enums.ZuulFilterTypeEnum;
import com.gjh.shopdemo.util.UUIDUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PreFilter extends ZuulFilter {

    public static final String TRACE_ID = "traceId";
    public static final String X_TRACE_ID = "X-Trace-Id";

    @Value("${zuul.filter.pre}")
    private boolean enabled;

    @Override
    public String filterType() {
        return ZuulFilterTypeEnum.PRE.toString();
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return enabled;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        String uuid = UUIDUtils.getUUID();
        ctx.addZuulRequestHeader(X_TRACE_ID, uuid);
        MDC.put(TRACE_ID, uuid);
        String pathInfo = ctx.getRequest().getRequestURI();
        log.info("调用接口路径：{}", pathInfo);
        return null;
    }
}
