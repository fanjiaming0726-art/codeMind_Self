package com.example.codemind_self.infrastructure.minio;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties minioProperties;

    @Bean // 被@Bean标注的方法不能使用private方法，因为这个配置类在运行时会被Spring创建一个子类代理，由子类代理重写Bean方法，如果被private标注就重写不了了，重写不了Bean就没法注入
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(minioProperties.getEndPoint())
                .credentials(minioProperties.getAccessKey(),minioProperties.getSecretKey())
                .build();
    }

}
