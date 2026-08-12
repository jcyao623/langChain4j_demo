package com.ifinance.aicustomer.biz.rag;

import com.ifinance.aicustomer.common.exception.BusinessException;
import com.ifinance.aicustomer.common.exception.ErrorCode;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    @Value("${pinecone.faq-file:faq/互联网金融客服FAQ.txt}")
    private String faqFile;

    public KnowledgeBaseLoader(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<TextSegment> segments = loadFaqSegments();
        log.info("开始写入知识库, size={}, faqFile={}", segments.size(), faqFile);
        Response<List<Embedding>> response = embeddingModel.embedAll(segments);
        embeddingStore.addAll(response.content(), segments);
        log.info("知识库写入完成, size={}", segments.size());
    }

    private List<TextSegment> loadFaqSegments() {
        ClassPathResource resource = new ClassPathResource(faqFile);
        List<TextSegment> segments = new ArrayList<>();
        StringBuilder question = new StringBuilder();
        StringBuilder answer = new StringBuilder();

        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    flush(segments, question, answer);
                    continue;
                }
                if (trimmed.startsWith("Q:")) {
                    flush(segments, question, answer);
                    question.append(trimmed.substring(2).trim());
                } else if (trimmed.startsWith("A:")) {
                    answer.append(trimmed.substring(2).trim());
                } else if (answer.length() > 0) {
                    answer.append("\n").append(trimmed);
                } else {
                    question.append(" ").append(trimmed);
                }
            }
            flush(segments, question, answer);
            return segments;
        } catch (IOException e) {
            log.error("读取 FAQ 文件失败, file={}", faqFile, e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "读取 FAQ 文件失败", e);
        }
    }

    private void flush(List<TextSegment> segments, StringBuilder question, StringBuilder answer) {
        if (question.length() > 0 || answer.length() > 0) {
            String text = "Q: " + question + "\nA: " + answer;
            segments.add(TextSegment.from(text));
            question.setLength(0);
            answer.setLength(0);
        }
    }
}
