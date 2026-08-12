package com.ifinance.aicustomer.biz.rag;

import dev.langchain4j.data.segment.TextSegment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FAQ 文本分割器测试。
 */
class FaqTextSplitterTest {

    /**
     * 验证按 Q/A 条目切分。
     */
    @Test
    void shouldSplitFaqContentIntoPairs() {
        String content = """
                Q: 借款利率怎么算？
                A: 按合同年化利率计算。

                Q: 可以提前还款吗？
                A: 支持提前还款。
                """;
        FaqTextSplitter splitter = new FaqTextSplitter(500);

        List<TextSegment> segments = splitter.split(content);

        assertEquals(2, segments.size());
        assertTrue(segments.get(0).text().contains("借款利率怎么算"));
        assertTrue(segments.get(1).text().contains("提前还款"));
    }

    /**
     * 验证超长答案会切分为多个文本块。
     */
    @Test
    void shouldSplitLongAnswerIntoMultipleChunks() {
        StringBuilder answer = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            answer.append("这是一段用于测试长答案分割的金融客服知识文本。");
        }
        String content = "Q: 长答案问题？\nA: " + answer;
        FaqTextSplitter splitter = new FaqTextSplitter(200);

        List<TextSegment> segments = splitter.split(content);

        assertTrue(segments.size() > 1);
        assertTrue(segments.get(0).text().contains("长答案问题"));
    }

    /**
     * 验证空内容返回空列表。
     */
    @Test
    void shouldReturnEmptyForEmptyContent() {
        FaqTextSplitter splitter = new FaqTextSplitter(500);

        List<TextSegment> segments = splitter.split("");

        assertEquals(0, segments.size());
    }
}
