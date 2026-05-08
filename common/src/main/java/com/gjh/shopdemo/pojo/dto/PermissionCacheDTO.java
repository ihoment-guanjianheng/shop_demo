package com.gjh.shopdemo.pojo.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PermissionCacheDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String permissionName;
    private String permissionCode;
    private String serviceName;
    private String requestPath;
    private String resourceType;
    private Long parentId;
    private Integer sortOrder;
    private Integer status;
}