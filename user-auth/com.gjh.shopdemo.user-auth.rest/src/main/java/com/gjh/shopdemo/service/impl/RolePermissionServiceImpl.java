package com.gjh.shopdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjh.shopdemo.mapper.RolePermissionMapper;
import com.gjh.shopdemo.pojo.model.RolePermission;
import com.gjh.shopdemo.service.PermissionCacheService;
import com.gjh.shopdemo.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {

    @Autowired
    private PermissionCacheService permissionCacheService;

    @Override
    @Transactional
    public void assignPermission(Long roleId, Long permissionId) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionId);
        save(rolePermission);

        registerCacheInvalidation(() -> permissionCacheService.invalidateUsersByRoleId(roleId));
    }

    @Override
    @Transactional
    public void removeRolePermission(Long id) {
        RolePermission rolePermission = getById(id);
        if (rolePermission == null) {
            return;
        }
        removeById(id);

        Long roleId = rolePermission.getRoleId();
        registerCacheInvalidation(() -> permissionCacheService.invalidateUsersByRoleId(roleId));
    }

    private void registerCacheInvalidation(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}