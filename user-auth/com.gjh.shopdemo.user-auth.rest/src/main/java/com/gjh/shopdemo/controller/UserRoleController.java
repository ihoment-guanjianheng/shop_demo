package com.gjh.shopdemo.controller;

import com.gjh.shopdemo.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/userRole")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;
}