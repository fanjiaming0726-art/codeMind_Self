package com.example.codemind_self.infrastructure.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.deepseek")
public class ChatAiProperties {

    private String baseUrl;
    private String apiKey;
    private String model;

}
