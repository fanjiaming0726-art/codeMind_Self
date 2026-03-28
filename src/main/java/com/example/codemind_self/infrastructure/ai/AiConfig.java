package com.example.codemind_self.infrastructure.ai;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class AiConfig {

    private final ChatAiProperties chatAiProperties;
    public final EmbeddingAiProperties embeddingAiProperties;

    @Bean
    public OpenAiChatModel chatModel(){
        return OpenAiChatModel.builder()
                .apiKey(chatAiProperties.getApiKey())
                .baseUrl(chatAiProperties.getBaseUrl())
                .modelName(chatAiProperties.getModel())
                .temperature(0.7)
                .timeout(Duration.ofSeconds(30))
                .build();
    }

    @Bean
    public OpenAiStreamingChatModel streamingChatModel(){
        return OpenAiStreamingChatModel.builder()
                .apiKey(chatAiProperties.getApiKey())
                .baseUrl(chatAiProperties.getBaseUrl())
                .modelName(chatAiProperties.getModel())
                .temperature(0.7)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel(){
        return OpenAiEmbeddingModel.builder()
                .apiKey(embeddingAiProperties.getApiKey())
                .baseUrl(embeddingAiProperties.getBaseUrl())
                .modelName(embeddingAiProperties.getModel())
                .timeout(Duration.ofSeconds(30))
                .build();

    }

    @Bean
    public MilvusEmbeddingStore milvusEmbeddingStore(){
        return MilvusEmbeddingStore.builder()
                .host("localhost")
                .port(19530)
                .collectionName("code_chunks")
                .dimension(1024)
                .build();

    }




}
