package com.ifinance.aicustomer.biz.mcp;

import com.ifinance.aicustomer.biz.config.McpServerProperties;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Duration;

/**
 * MCP 客户端工厂，将服务器配置转换为可被模型调用的客户端。
 */
@Component
public class McpClientFactory {

    private final McpTransportFactoryResolver transportFactoryResolver;

    public McpClientFactory(McpTransportFactoryResolver transportFactoryResolver) {
        this.transportFactoryResolver = transportFactoryResolver;
    }

    /**
     * 根据服务器配置创建 MCP 客户端。
     */
    public McpClient create(McpServerProperties server) {
        Assert.hasText(server.getName(), "MCP 服务名称不能为空");
        McpTransport transport = transportFactoryResolver.resolve(server.getTransport()).create(server);
        return DefaultMcpClient.builder()
                .transport(transport)
                .clientName("langchain4j-demo-" + server.getName())
                .initializationTimeout(Duration.ofSeconds(server.getInitializationTimeoutSeconds()))
                .toolExecutionTimeout(Duration.ofSeconds(server.getToolExecutionTimeoutSeconds()))
                .build();
    }
}
