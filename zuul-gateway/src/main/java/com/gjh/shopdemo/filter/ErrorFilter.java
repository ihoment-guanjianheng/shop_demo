package com.gjh.shopdemo.filter;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.gjh.shopdemo.enums.ZuulFilterTypeEnum;
import com.gjh.shopdemo.pojo.enums.ResultEnum;
import com.gjh.shopdemo.pojo.result.ShopResult;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class ErrorFilter extends ZuulFilter {

    @Value("${zuul.filter.error}")
    private Boolean enabled;

    @Override
    public String filterType() {
        return ZuulFilterTypeEnum.ERROR.toString();
    }

    @Override
    public int filterOrder() {
        return -1;
    }

    @Override
    public boolean shouldFilter() {
        return enabled;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        Throwable throwable = ctx.getThrowable();

        // 如果前面有异常，可以打印日志
        if (throwable != null) {
             log.error("Zuul routing error", throwable);
        }

        // 关键：手动构造响应，避免继续执行后续可能出错的逻辑
        ctx.setSendZuulResponse(false);
        ctx.setResponseStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        ctx.getResponse().setContentType("application/json;charset=UTF-8");
        ctx.setResponseBody(JacksonUtils.toJson(ShopResult.fail(ResultEnum.SERVICE_NOT_ACCESS)));
        // 清除异常标记，防止某些版本下 POST filter 再次处理时报错
        ctx.remove("throwable");

        return null;
    }
}
