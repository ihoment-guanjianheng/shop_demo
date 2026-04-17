package com.gjh.shopdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjh.shopdemo.pojo.dto.UserLoginDTO;
import com.gjh.shopdemo.pojo.dto.UserRegisterDTO;
import com.gjh.shopdemo.pojo.model.User;
import com.gjh.shopdemo.pojo.vo.UserVO;

public interface UserService extends IService<User> {

    void register(UserRegisterDTO dto);

    UserVO login(UserLoginDTO dto);

    void logout(String token);

}