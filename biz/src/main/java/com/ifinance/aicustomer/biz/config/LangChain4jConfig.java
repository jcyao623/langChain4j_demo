package com.ifinance.aicustomer.biz.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j 模型配置。
 */
@Configuration
public class LangChain4jConfig {

    private static final Logger log = LoggerFactory.getLogger(LangChain4jConfig.class);

    @Bean
    /**
     * 构建阿里云百炼 OpenAI 兼容模式的聊天模型。
     */
    public OpenAiChatModel openAiChatModel(AliyunOpenAiProperties properties) {
        log.info("初始化阿里云 OpenAI 兼容模型, model={}, baseUrl={}", properties.getModel(), properties.getBaseUrl());
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
