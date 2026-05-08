package com.gjh.shopdemo.controller;

import com.gjh.shopdemo.pojo.dto.RolePermissionAssignDTO;
import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rolePermission")
public class RolePermissionController {

    @Autowired
    private RolePermissionService rolePermissionService;

    @PostMapping("/assign")
    public ShopResult<Void> assignPermission(@RequestBody @Validated RolePermissionAssignDTO dto) {
        rolePermissionService.assignPermission(dto.getRoleId(), dto.getPermissionId());
        return ShopResult.success();
    }

    @DeleteMapping("/{id}")
    public ShopResult<Void> removeRolePermission(@PathVariable Long id) {
        rolePermissionService.removeRolePermission(id);
        return ShopResult.success();
    }
}