package com.ifinance.aicustomer.biz.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * FAQ 知识库初始化：读取 txt 并写入 Pinecone。
 */
@Component
@ConditionalOnExpression("${pinecone.init-on-startup:false} and ${pinecone.enabled:true}")
public class KnowledgeBaseLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseLoader.class);

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final FaqTextSplitter faqTextSplitter;

    @Value("${pinecone.faq-file:faq/finance-faq.txt}")
    private String faqFile;

    /**
     * 构造知识库加载器。
     */
    public KnowledgeBaseLoader(EmbeddingStore<TextSegment> embeddingStore,
                               EmbeddingModel embeddingModel,
                               FaqTextSplitter faqTextSplitter) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.faqTextSplitter = faqTextSplitter;
    }

    @Override
    /**
     * 应用启动时执行 FAQ 向量化与写入。
     */
    public void run(ApplicationArguments args) {
        List<TextSegment> segments = loadFaqSegments();
        log.info("开始写入知识库, size={}, faqFile={}", segments.size(), faqFile);
        Response<List<Embedding>> response = embeddingModel.embedAll(segments);
        embeddingStore.addAll(response.content(), segments);
        log.info("知识库写入完成, size={}", segments.size());
    }

    /**
     * 读取 FAQ 文件并调用分割器切分。
     */
    private List<TextSegment> loadFaqSegments() {
        ClassPathResource resource = new ClassPathResource(faqFile);
        try (InputStream inputStream = resource.getInputStream()) {
            String content = new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            return faqTextSplitter.split(content);
        } catch (IOException e) {
            log.error("读取 FAQ 文件失败, file={}", faqFile, e);
            throw new IllegalStateException("读取 FAQ 文件失败: " + faqFile, e);
        }
    }
}
