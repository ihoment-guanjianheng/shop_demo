package com.gjh.shopdemo.controller;

import com.gjh.shopdemo.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rolePermission")
public class RolePermissionController {

    @Autowired
    private RolePermissionService rolePermissionService;
}