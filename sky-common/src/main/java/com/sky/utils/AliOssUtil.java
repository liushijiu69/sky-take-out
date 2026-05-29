package com.sky.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.io.ByteArrayInputStream;

/**
 * 阿里云 OSS 工具类
 * <p>
 * accessKeyId 和 accessKeySecret 通过配置类从 application.yml 获取，
 * application.yml 中通过 ${OSS_ACCESS_KEY_ID} / ${OSS_ACCESS_KEY_SECRET} 占位符读取环境变量。
 * </p>
 */
@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes      文件字节数组
     * @param objectName 对象完整路径（不包含 Bucket 名称）
     * @return 文件访问 URL，上传失败返回 null
     */
    public String upload(byte[] bytes, String objectName) {
        OSS ossClient = null;
        try {
            // 创建 OSSClient 实例
            ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

            // 上传文件
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));

            // 文件访问路径规则 https://BucketName.Endpoint/ObjectName
            String url = new StringBuilder("https://")
                    .append(bucketName)
                    .append(".")
                    .append(endpoint)
                    .append("/")
                    .append(objectName)
                    .toString();

            log.info("文件上传到:{}", url);
            return url;

        } catch (OSSException oe) {
            log.error("OSS 上传失败，请求被服务端拒绝: ErrorMessage={} ErrorCode={} RequestId={} HostId={}",
                    oe.getErrorMessage(), oe.getErrorCode(), oe.getRequestId(), oe.getHostId());
            return null;
        } catch (ClientException ce) {
            log.error("OSS 客户端异常，无法访问网络或凭证错误: {}", ce.getMessage(), ce);
            return null;
        } catch (Exception e) {
            log.error("OSS 上传发生未知异常: {}", e.getMessage(), e);
            return null;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
