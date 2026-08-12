package com.ifinance.aicustomer.biz.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j 模型配置。
 */
@Configuration
public class LangChain4jConfig {

    @Bean
    public OpenAiChatModel openAiChatModel(AliyunOpenAiProperties properties) {
        return OpenAiChatModel.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .modelName(properties.getModel())
                .temperature(properties.getTemperature())
                .maxTokens(properties.getMaxTokens())
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();
    }
}
