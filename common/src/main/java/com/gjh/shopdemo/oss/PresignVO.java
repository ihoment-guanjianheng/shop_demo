package com.gjh.shopdemo.oss;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PresignVO {

    /** 前端 PUT 上传用的预签名 URL */
    private String uploadUrl;

    /** 上传完成后存入 mainImage 的公网访问 URL */
    private String accessUrl;

    /** PUT 请求必须携带的 Content-Type，与签名一致 */
    private String contentType;
}