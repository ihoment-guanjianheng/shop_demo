package com.gjh.shopdemo.filter;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.gjh.shopdemo.enums.ZuulFilterTypeEnum;
import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.util.JwtUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class AuthFilter extends ZuulFilter {

    @Value("${zuul.filter.token}")
    private Boolean enable;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String X_USER_ID_HEADER = "X-User-Id";

    @Override
    public String filterType() {
        return ZuulFilterTypeEnum.PRE.toString();
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return enable;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        // 白名单路径可直接放行（如登录、注册）
        String uri = request.getRequestURI();
        if (isWhiteList(uri)) {
            return null;
        }

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            rejectRequest(ctx, "缺少有效的认证令牌");
            return null;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // 校验黑名单（由 user-auth 服务在登出时写入）
        if (stringRedisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token)) {
            rejectRequest(ctx, "令牌已失效，请重新登录");
            return null;
        }

        // 解析并校验 JWT
        try {
            Long userId = JwtUtils.getUserId(token);
            // 将 userId 透传给下游服务
            ctx.addZuulRequestHeader(X_USER_ID_HEADER, String.valueOf(userId));
        } catch (ExpiredJwtException e) {
            log.warn("JWT 已过期: {}", token);
            rejectRequest(ctx, "令牌已过期，请重新登录");
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            rejectRequest(ctx, "无效的认证令牌");
        }

        return null;
    }

    private boolean isWhiteList(String uri) {
        // 可根据配置化扩展，目前先硬编码登录/注册接口
        return uri.startsWith("/api/user/login") || uri.startsWith("/api/user/register");
    }

    private void rejectRequest(RequestContext ctx, String message) {
        ctx.setSendZuulResponse(false);
        ctx.setResponseStatusCode(HttpServletResponse.SC_UNAUTHORIZED);
        ctx.getResponse().setContentType("application/json;charset=UTF-8");
        ctx.setResponseBody(JacksonUtils.toJson(ShopResult.fail(message)));
    }
}