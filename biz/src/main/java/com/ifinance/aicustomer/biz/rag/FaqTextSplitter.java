package com.ifinance.aicustomer.biz.rag;

import dev.langchain4j.data.segment.TextSegment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * FAQ 文本分割器：将 Q/A 格式的 FAQ 文件按条目和长度切分为可向量化的文本块。
 */
@Component
public class FaqTextSplitter {

    private static final String QUESTION_PREFIX = "Q:";
    private static final String ANSWER_PREFIX = "A:";

    private final int maxChunkLength;

    /**
     * 构造分割器。
     *
     * @param maxChunkLength 单块最大字符数
     */
    public FaqTextSplitter(@Value("${pinecone.faq-chunk-length:500}") int maxChunkLength) {
        this.maxChunkLength = maxChunkLength;
    }

    /**
     * 将 FAQ 原始内容切分为文本块。
     *
     * @param content FAQ 文件内容
     * @return 文本块列表
     */
    public List<TextSegment> split(String content) {
        List<FaqPair> pairs = parseFaqPairs(content);
        List<TextSegment> segments = new ArrayList<>();
        for (FaqPair pair : pairs) {
            segments.addAll(splitPair(pair));
        }
        return segments;
    }

    /**
     * 解析 Q/A 条目。
     */
    private List<FaqPair> parseFaqPairs(String content) {
        List<FaqPair> pairs = new ArrayList<>();
        StringBuilder question = new StringBuilder();
        StringBuilder answer = new StringBuilder();
        String normalized = content.replace("\r\n", "\n");
        for (String line : normalized.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith(QUESTION_PREFIX)) {
                flush(pairs, question, answer);
                question.append(trimmed.substring(QUESTION_PREFIX.length()).trim());
            } else if (trimmed.startsWith(ANSWER_PREFIX)) {
                answer.append(trimmed.substring(ANSWER_PREFIX.length()).trim());
            } else if (trimmed.isEmpty()) {
                flush(pairs, question, answer);
            } else if (answer.length() > 0) {
                answer.append("\n").append(trimmed);
            } else if (question.length() > 0) {
                question.append(" ").append(trimmed);
            }
        }
        flush(pairs, question, answer);
        return pairs;
    }

    /**
     * 将单个 Q/A 条目按长度切块。
     */
    private List<TextSegment> splitPair(FaqPair pair) {
        String text = pair.toText();
        if (text.length() <= maxChunkLength) {
            return List.of(TextSegment.from(text));
        }
        return splitLongAnswer(pair);
    }

    /**
     * 超长答案按行切块，每块保留问题标题。
     */
    private List<TextSegment> splitLongAnswer(FaqPair pair) {
        List<TextSegment> segments = new ArrayList<>();
        String[] lines = pair.answer().split("\n");
        StringBuilder chunk = new StringBuilder("Q: " + pair.question() + "\nA: ");
        for (String line : lines) {
            if (chunk.length() + line.length() + 1 > maxChunkLength && chunk.length() > QUESTION_PREFIX.length()) {
                segments.add(TextSegment.from(chunk.toString().trim()));
                chunk = new StringBuilder("Q: " + pair.question() + "（续）\nA: ");
            }
            if (!chunk.toString().endsWith("\nA: ")) {
                chunk.append("\n");
            }
            chunk.append(line);
        }
        if (chunk.length() > 0) {
            segments.add(TextSegment.from(chunk.toString().trim()));
        }
        return segments;
    }

    /**
     * 将已解析的 Q/A 追加到结果集。
     */
    private void flush(List<FaqPair> pairs, StringBuilder question, StringBuilder answer) {
        if (question.length() > 0 || answer.length() > 0) {
            pairs.add(new FaqPair(question.toString(), answer.toString()));
            question.setLength(0);
            answer.setLength(0);
        }
    }

    /**
     * FAQ 单条问答。
     */
    private record FaqPair(String question, String answer) {

        /**
         * 转为向量化文本。
         */
        String toText() {
            return "Q: " + question + "\nA: " + answer;
        }
    }
}
