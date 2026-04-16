package com.gjh.shopdemo.filter;

import com.gjh.shopdemo.enums.ZuulFilterTypeEnum;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PreFilter extends ZuulFilter {

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
        return null;
    }
}
