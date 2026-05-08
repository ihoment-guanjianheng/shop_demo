package com.gjh.shopdemo.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RolePermissionAssignDTO {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    @NotNull(message = "权限ID不能为空")
    private Long permissionId;
}