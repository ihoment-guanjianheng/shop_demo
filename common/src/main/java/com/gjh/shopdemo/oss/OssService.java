package com.gjh.shopdemo.oss;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.gjh.shopdemo.util.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

@Service
@ConditionalOnClass(name = "com.aliyun.oss.OSS")
@ConditionalOnProperty(prefix = "oss", name = "endpoint")
public class OssService {

    @Autowired
    private OSS ossClient;

    @Value("${oss.bucket}")
    private String bucket;

    @Value("${oss.endpoint}")
    private String endpoint;

    /**
     * 生成预签名 PUT URL，供前端直接上传文件到 OSS，有效期 expiresSeconds 秒
     * contentType 不为空时会纳入签名，客户端 PUT 时必须携带相同的 Content-Type
     */
    public String generatePresignedPutUrl(String objectKey, String contentType, long expiresSeconds) {
        Date expiration = new Date(System.currentTimeMillis() + expiresSeconds * 1000L);
        GeneratePresignedUrlRequest request =
                new GeneratePresignedUrlRequest(bucket, objectKey, HttpMethod.PUT);
        request.setExpiration(expiration);
        if (contentType != null && !contentType.isEmpty()) {
            request.setContentType(contentType);
        }
        URL url = ossClient.generatePresignedUrl(request);
        return url.toString();
    }

    /**
     * 服务端上传文件流到 OSS
     */
    public void upload(String objectKey, InputStream inputStream) {
        ossClient.putObject(bucket, objectKey, inputStream);
    }

    /**
     * 返回对象的公网访问 URL（适用于公开读 bucket）
     */
    public String getPublicUrl(String objectKey) {
        return "https://" + bucket + "." + endpoint + "/" + objectKey;
    }

    /**
     * 生成预签名 GET URL，有效期 expiresSeconds 秒，适用于私有 bucket 临时授权下载
     */
    public String generatePresignedGetUrl(String objectKey, long expiresSeconds) {
        Date expiration = new Date(System.currentTimeMillis() + expiresSeconds * 1000L);
        GeneratePresignedUrlRequest request =
                new GeneratePresignedUrlRequest(bucket, objectKey, HttpMethod.GET);
        request.setExpiration(expiration);
        return ossClient.generatePresignedUrl(request).toString();
    }

    /**
     * 生成预签名 VO：根据原始文件名和目录前缀，一次性返回 PUT 上传 URL、公网访问 URL 及 Content-Type
     */
    public PresignVO presign(String directory, String filename, long expiresSeconds) {
        String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.')).toLowerCase() : "";
        String objectKey = directory + "/" + UUIDUtils.getUUID() + ext;
        String contentType = resolveContentType(ext);
        return new PresignVO(
                generatePresignedPutUrl(objectKey, contentType, expiresSeconds),
                getPublicUrl(objectKey),
                contentType
        );
    }

    private String resolveContentType(String ext) {
        switch (ext) {
            case ".jpg":
            case ".jpeg": return "image/jpeg";
            case ".png":  return "image/png";
            case ".gif":  return "image/gif";
            case ".webp": return "image/webp";
            default:      return "application/octet-stream";
        }
    }
}
