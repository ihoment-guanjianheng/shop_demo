package com.gjh.shopdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjh.shopdemo.pojo.model.RolePermission;

public interface RolePermissionService extends IService<RolePermission> {

    void assignPermission(Long roleId, Long permissionId);

    void removeRolePermission(Long id);
}