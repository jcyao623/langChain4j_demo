package com.ifinance.aicustomer.biz.mcp;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.McpGetPromptResult;
import dev.langchain4j.mcp.client.McpPrompt;
import dev.langchain4j.mcp.client.McpReadResourceResult;
import dev.langchain4j.mcp.client.McpResource;
import dev.langchain4j.mcp.client.McpResourceTemplate;
import dev.langchain4j.mcp.client.McpRoot;
import dev.langchain4j.service.tool.ToolExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * MCP 客户端装饰器，连接异常时对工具列表与工具调用自动重试一次。
 * 远程服务可能重置空闲连接，重试可让 Java HttpClient 使用新连接继续请求。
 */
public class RetryableMcpClient implements McpClient {

    private static final Logger log = LoggerFactory.getLogger(RetryableMcpClient.class);

    private final McpClient delegate;

    public RetryableMcpClient(McpClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public String key() {
        return delegate.key();
    }

    @Override
    public String instructions() {
        return delegate.instructions();
    }

    @Override
    public List<ToolSpecification> listTools() {
        return retryOnConnectionError(delegate::listTools);
    }

    @Override
    public List<ToolSpecification> listTools(InvocationContext invocationContext) {
        return retryOnConnectionError(() -> delegate.listTools(invocationContext));
    }

    @Override
    public ToolExecutionResult executeTool(ToolExecutionRequest request) {
        return retryOnConnectionError(() -> delegate.executeTool(request));
    }

    @Override
    public ToolExecutionResult executeTool(ToolExecutionRequest request, InvocationContext invocationContext) {
        return retryOnConnectionError(() -> delegate.executeTool(request, invocationContext));
    }

    @Override
    public List<McpResource> listResources() {
        return delegate.listResources();
    }

    @Override
    public List<McpResource> listResources(InvocationContext invocationContext) {
        return delegate.listResources(invocationContext);
    }

    @Override
    public List<McpResourceTemplate> listResourceTemplates() {
        return delegate.listResourceTemplates();
    }

    @Override
    public List<McpResourceTemplate> listResourceTemplates(InvocationContext invocationContext) {
        return delegate.listResourceTemplates(invocationContext);
    }

    @Override
    public McpReadResourceResult readResource(String uri) {
        return delegate.readResource(uri);
    }

    @Override
    public McpReadResourceResult readResource(String uri, InvocationContext invocationContext) {
        return delegate.readResource(uri, invocationContext);
    }

    @Override
    public void subscribeToResource(String uri) {
        delegate.subscribeToResource(uri);
    }

    @Override
    public void unsubscribeFromResource(String uri) {
        delegate.unsubscribeFromResource(uri);
    }

    @Override
    public List<McpPrompt> listPrompts() {
        return delegate.listPrompts();
    }

    @Override
    public McpGetPromptResult getPrompt(String name, Map<String, Object> arguments) {
        return delegate.getPrompt(name, arguments);
    }

    @Override
    public void checkHealth() {
        delegate.checkHealth();
    }

    @Override
    public void setRoots(List<McpRoot> roots) {
        delegate.setRoots(roots);
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }

    private <T> T retryOnConnectionError(Supplier<T> action) {
        try {
            return action.get();
        } catch (RuntimeException e) {
            if (!isConnectionError(e)) {
                throw e;
            }
            log.warn("MCP 连接异常，准备重试一次, client={}", delegate.key(), e);
            return action.get();
        }
    }

    private boolean isConnectionError(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof IOException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
