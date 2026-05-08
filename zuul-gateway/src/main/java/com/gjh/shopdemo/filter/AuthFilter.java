package com.gjh.shopdemo.filter;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.gjh.shopdemo.constant.RedisConstant;
import com.gjh.shopdemo.enums.ZuulFilterTypeEnum;
import com.gjh.shopdemo.pojo.dto.PermissionCacheDTO;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Component
@Slf4j
public class AuthFilter extends ZuulFilter {

    @Value("${zuul.filter.token}")
    private Boolean enable;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String X_USER_ID_HEADER = "X-User-Id";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

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
            rejectRequest(ctx, HttpServletResponse.SC_UNAUTHORIZED, "缺少有效的认证令牌");
            return null;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // 校验黑名单（由 user-auth 服务在登出时写入）
        if (stringRedisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token)) {
            rejectRequest(ctx, HttpServletResponse.SC_UNAUTHORIZED, "令牌已失效，请重新登录");
            return null;
        }

        // 解析并校验 JWT
        Long userId;
        try {
            userId = JwtUtils.getUserId(token);
        } catch (ExpiredJwtException e) {
            log.warn("JWT 已过期: {}", token);
            rejectRequest(ctx, HttpServletResponse.SC_UNAUTHORIZED, "令牌已过期，请重新登录");
            return null;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            rejectRequest(ctx, HttpServletResponse.SC_UNAUTHORIZED, "无效的认证令牌");
            return null;
        }

        // 权限判定：从 Redis 读取用户权限列表
        if (!checkPermission(userId, uri)) {
            return null;
        }

        // 将 userId 透传给下游服务
        ctx.addZuulRequestHeader(X_USER_ID_HEADER, String.valueOf(userId));

        return null;
    }

    private boolean checkPermission(Long userId, String uri) {
        RequestContext ctx = RequestContext.getCurrentContext();
        String key = RedisConstant.PERMISSION_USER_PREFIX + userId;
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            rejectRequest(ctx, HttpServletResponse.SC_UNAUTHORIZED, "权限缓存已过期，请重新登录");
            return false;
        }

        @SuppressWarnings("unchecked")
        List<PermissionCacheDTO> permissions = (List<PermissionCacheDTO>) value;
        if (permissions == null || permissions.isEmpty()) {
            rejectRequest(ctx, HttpServletResponse.SC_FORBIDDEN, "无权访问该资源");
            return false;
        }

        boolean matched = permissions.stream()
                .anyMatch(p -> p.getRequestPath() != null && pathMatcher.match(p.getRequestPath(), uri));

        if (!matched) {
            rejectRequest(ctx, HttpServletResponse.SC_FORBIDDEN, "无权访问该资源");
            return false;
        }

        return true;
    }

    private boolean isWhiteList(String uri) {
        // 可根据配置化扩展，目前先硬编码登录/注册/登出接口
        return uri.startsWith("/api/user/login") || uri.startsWith("/api/user/register") || uri.startsWith("/api/user/logout");
    }

    private void rejectRequest(RequestContext ctx, int statusCode, String message) {
        ctx.setSendZuulResponse(false);
        ctx.setResponseStatusCode(statusCode);
        ctx.getResponse().setContentType("application/json;charset=UTF-8");
        ctx.setResponseBody(JacksonUtils.toJson(ShopResult.fail(message)));
    }
}