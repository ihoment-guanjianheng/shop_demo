package com.gjh.shopdemo.service.impl;

import com.gjh.shopdemo.constant.RedisConstant;
import com.gjh.shopdemo.mapper.PermissionMapper;
import com.gjh.shopdemo.mapper.UserRoleMapper;
import com.gjh.shopdemo.pojo.dto.PermissionCacheDTO;
import com.gjh.shopdemo.pojo.model.Permission;
import com.gjh.shopdemo.service.PermissionCacheService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PermissionCacheServiceImpl implements PermissionCacheService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void loadAndCache(Long userId) {
        List<Permission> permissions = permissionMapper.selectPermissionsByUserId(userId);
        List<PermissionCacheDTO> dtoList = permissions.stream().map(p -> {
            PermissionCacheDTO dto = new PermissionCacheDTO();
            BeanUtils.copyProperties(p, dto);
            return dto;
        }).collect(Collectors.toList());

        String key = RedisConstant.PERMISSION_USER_PREFIX + userId;
        redisTemplate.opsForValue().set(key, dtoList, RedisConstant.PERMISSION_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PermissionCacheDTO> getCachedPermissions(Long userId) {
        String key = RedisConstant.PERMISSION_USER_PREFIX + userId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof List) {
            return (List<PermissionCacheDTO>) value;
        }
        return null;
    }

    @Override
    public void invalidateUser(Long userId) {
        String key = RedisConstant.PERMISSION_USER_PREFIX + userId;
        redisTemplate.delete(key);
    }

    @Override
    public void invalidateUsersByRoleId(Long roleId) {
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(roleId);
        for (Long userId : userIds) {
            invalidateUser(userId);
        }
    }
}