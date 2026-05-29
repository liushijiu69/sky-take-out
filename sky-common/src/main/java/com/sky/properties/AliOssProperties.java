package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置属性类，从 application.yml 的 sky.alioss 前缀加载
 * <p>
 * accessKeyId 和 accessKeySecret 在 application.yml 中通过
 * ${OSS_ACCESS_KEY_ID} / ${OSS_ACCESS_KEY_SECRET} 占位符读取环境变量。
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "sky.alioss")
@Data
public class AliOssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

}
