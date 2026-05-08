package com.gjh.shopdemo.service;

import com.gjh.shopdemo.pojo.dto.PermissionCacheDTO;

import java.util.List;

public interface PermissionCacheService {

    void loadAndCache(Long userId);

    List<PermissionCacheDTO> getCachedPermissions(Long userId);

    void invalidateUser(Long userId);

    void invalidateUsersByRoleId(Long roleId);
}