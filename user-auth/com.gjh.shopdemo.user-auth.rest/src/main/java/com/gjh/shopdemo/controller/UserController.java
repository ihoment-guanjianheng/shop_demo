package com.gjh.shopdemo.controller;

import com.gjh.shopdemo.pojo.dto.UserLoginDTO;
import com.gjh.shopdemo.pojo.dto.UserRegisterDTO;
import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.pojo.vo.UserVO;
import com.gjh.shopdemo.service.UserService;
import com.gjh.shopdemo.userauth.client.remote.UserFeignRemote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping
public class UserController implements UserFeignRemote {

    @Autowired
    private UserService userService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @PostMapping("/register")
    public ShopResult<Void> register(@Valid @RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return ShopResult.success();
    }

    @PostMapping("/login")
    public ShopResult<UserVO> login(@Valid @RequestBody UserLoginDTO dto) {
        UserVO vo = userService.login(dto);
        return ShopResult.success(vo);
    }

    @PostMapping("/logout")
    public ShopResult<Void> logout(@RequestHeader(value = AUTHORIZATION_HEADER, required = false) String authHeader) {
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
            return ShopResult.fail("缺少有效的认证令牌");
        }
        String token = authHeader.substring(BEARER_PREFIX.length());
        userService.logout(token);
        return ShopResult.success();
    }

}