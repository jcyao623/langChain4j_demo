package com.ifinance.aicustomer.common.util;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.TokenStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class StreamHelper {

    /**
     * 将 TokenStream 转换为同步的 String（阻塞等待）
     *
     * @param tokenStream 流式对象
     * @param timeoutSeconds 超时时间（秒），防止模型卡死
     * @return 完整的回复文本
     */
    public static String collectToString(TokenStream tokenStream, int timeoutSeconds) {
        CompletableFuture<String> future = new CompletableFuture<>();
        StringBuilder fullContent = new StringBuilder();

        tokenStream
            .onPartialResponse(token -> {
                // 接收流式数据
                fullContent.append(token);
            })
            .onCompleteResponse(chatResponse -> {
                // 流程结束
                AiMessage aiMessage = chatResponse.aiMessage();
                if (aiMessage != null) {
                    future.complete(aiMessage.text());
                } else {
                    future.complete(fullContent.toString());
                }
            })
            // 【关键修改 1】：显式调用 onError 处理异常
            .onError(error -> {
                future.completeExceptionally(error);
            })
            // 【关键修改 2】：或者直接调用 .ignoreErrors() (二选一即可，推荐 onError 更安全)
            // .ignoreErrors()
            .start();

        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("AI生成超时或失败", e);
        }
    }
}


