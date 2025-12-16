package com.example.student.config;

import io.minio.MinioClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minio 对象存储配置
 */
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@Slf4j
public class MinioConfig {
    
    @Bean
    @ConditionalOnProperty(name = "minio.enabled", havingValue = "true")
    public MinioClient minioClient(MinioProperties minioProperties) {
        try {
            MinioClient.Builder builder = MinioClient.builder()
                    .endpoint(minioProperties.getEndpoint())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey());
            
            MinioClient minioClient = builder.build();
            
            log.info("Minio 客户端已初始化，端点: {}", minioProperties.getEndpoint());
            return minioClient;
        } catch (Exception e) {
            log.error("初始化 Minio 客户端失败", e);
            throw new RuntimeException("Failed to initialize Minio client", e);
        }
    }
}

/**
 * Minio 配置属性
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
class MinioProperties {
    
    private boolean enabled = false;
    private String endpoint = "http://localhost:9000";
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
    private String bucket = "student-management";
    private boolean usePathStyle = true;
    private boolean useHttps = false;
}
