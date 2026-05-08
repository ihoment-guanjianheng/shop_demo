package com.gjh.shopdemo.controller;

import com.gjh.shopdemo.pojo.dto.UserRoleAssignDTO;
import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/userRole")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    @PostMapping("/assign")
    public ShopResult<Void> assignRole(@RequestBody @Validated UserRoleAssignDTO dto) {
        userRoleService.assignRole(dto.getUserId(), dto.getRoleId());
        return ShopResult.success();
    }

    @DeleteMapping("/{id}")
    public ShopResult<Void> removeUserRole(@PathVariable Long id) {
        userRoleService.removeUserRole(id);
        return ShopResult.success();
    }
}