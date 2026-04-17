package com.gjh.shopdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjh.shopdemo.constant.RedisConstant;
import com.gjh.shopdemo.mapper.UserMapper;
import com.gjh.shopdemo.pojo.dto.UserLoginDTO;
import com.gjh.shopdemo.pojo.dto.UserRegisterDTO;
import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.model.User;
import com.gjh.shopdemo.pojo.vo.UserInfoVO;
import com.gjh.shopdemo.pojo.vo.UserVO;
import com.gjh.shopdemo.service.UserService;
import com.gjh.shopdemo.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";

    @Override
    public void register(UserRegisterDTO dto) {
        long count = lambdaQuery().eq(User::getUsername, dto.getUsername()).count();
        if (count > 0) {
            throw new BaseException("用户名已存在");
        }
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        save(user);
    }

    @Override
    public UserVO login(UserLoginDTO dto) {
        User user = lambdaQuery().eq(User::getUsername, dto.getUsername()).one();
        if (user == null) {
            throw new BaseException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BaseException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BaseException("账号已被禁用");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setToken(JwtUtils.generateToken(user.getId()));
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        String key = RedisConstant.TOKEN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(key, userInfoVO, RedisConstant.TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
        return vo;
    }

    @Override
    public void logout(String token) {
        try {
            Claims claims = JwtUtils.parseToken(token);
            Date expiration = claims.getExpiration();
            long ttlSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            if (ttlSeconds > 0) {
                stringRedisTemplate.opsForValue().set(TOKEN_BLACKLIST_PREFIX + token, "1", ttlSeconds, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            // 如果 token 本身已无法解析，无需加入黑名单，直接视为无效即可
            throw new BaseException("无效的认证令牌");
        }
    }

}