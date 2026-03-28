package com.example.codemind_self.infrastructure.ai;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.embedding")
public class EmbeddingAiProperties {
    private String baseUrl;
    private String apiKey;
    private String model;
}
