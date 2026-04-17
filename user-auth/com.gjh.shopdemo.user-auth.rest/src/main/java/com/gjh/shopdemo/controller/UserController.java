package com.gjh.shopdemo.controller;

import com.gjh.shopdemo.pojo.model.User;
import com.gjh.shopdemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class UserController {

    @Autowired
    private UserService userService;
}