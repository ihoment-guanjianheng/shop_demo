package com.gjh.shopdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjh.shopdemo.mapper.UserRoleMapper;
import com.gjh.shopdemo.pojo.model.UserRole;
import com.gjh.shopdemo.service.PermissionCacheService;
import com.gjh.shopdemo.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    @Autowired
    private PermissionCacheService permissionCacheService;

    @Override
    @Transactional
    public void assignRole(Long userId, Long roleId) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        save(userRole);

        registerCacheInvalidation(() -> permissionCacheService.invalidateUser(userId));
    }

    @Override
    @Transactional
    public void removeUserRole(Long id) {
        UserRole userRole = getById(id);
        if (userRole == null) {
            return;
        }
        removeById(id);

        Long userId = userRole.getUserId();
        registerCacheInvalidation(() -> permissionCacheService.invalidateUser(userId));
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