package com.gjh.shopdemo.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserRoleAssignDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}