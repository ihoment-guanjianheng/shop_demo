package com.gjh.shopdemo.filter;

import com.gjh.shopdemo.constant.RedisConstant;
import com.gjh.shopdemo.context.AuthContext;
import com.gjh.shopdemo.pojo.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class TokenFilter implements Filter {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String X_USER_ID_HEADER = "X-User-Id";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String userId = req.getHeader(X_USER_ID_HEADER);
        if (userId != null && !userId.isEmpty()) {
            String key = RedisConstant.TOKEN_PREFIX + userId;
            UserInfoVO userInfo = (UserInfoVO) redisTemplate.opsForValue().get(key);
            AuthContext.setCurrentUser(userInfo);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            AuthContext.removeCurrentUser();
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
