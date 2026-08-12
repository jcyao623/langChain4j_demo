package com.ifinance.aicustomer.biz.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * RAG 向量检索配置。
 */
@Configuration
public class RagConfig {

    private static final Logger log = LoggerFactory.getLogger(RagConfig.class);

    /**
     * 初始化阿里云 OpenAI 兼容 Embedding 模型。
     */
    @Bean
    public OpenAiEmbeddingModel embeddingModel(AliyunOpenAiProperties properties) {
        log.info("初始化 Embedding 模型, model={}", properties.getEmbeddingModel());
        return OpenAiEmbeddingModel.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .modelName(properties.getEmbeddingModel())
                .dimensions(properties.getEmbeddingDimensions())
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .build();
    }

    /**
     * 初始化 Pinecone 向量存储。
     */
    @Bean
    @ConditionalOnProperty(name = "pinecone.enabled", havingValue = "true", matchIfMissing = true)
    public EmbeddingStore<TextSegment> embeddingStore(PineconeProperties properties) {
        log.info("初始化 Pinecone, index={}, namespace={}", properties.getIndex(), properties.getNamespace());
        return PineconeEmbeddingStore.builder()
                .apiKey(properties.getApiKey())
                .index(properties.getIndex())
                .nameSpace(properties.getNamespace())
                .build();
    }

    /**
     * 构建基于 Pinecone 的 RAG 检索增强器。
     */
    @Bean("retrievalAugmentor")
    @ConditionalOnProperty(name = "pinecone.enabled", havingValue = "true", matchIfMissing = true)
    public RetrievalAugmentor pineconeRetrievalAugmentor(EmbeddingStore<TextSegment> embeddingStore,
                                                         EmbeddingModel embeddingModel) {
        EmbeddingStoreContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.5)
                .build();
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(retriever)
                .build();
    }

    /**
     * Pinecone 关闭时提供空检索增强器，保证 AI Service 正常装配。
     */
    @Bean("retrievalAugmentor")
    @ConditionalOnProperty(name = "pinecone.enabled", havingValue = "false")
    public RetrievalAugmentor emptyRetrievalAugmentor() {
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(query -> java.util.List.of())
                .build();
    }
}
