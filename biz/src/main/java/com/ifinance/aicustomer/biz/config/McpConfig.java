package com.ifinance.aicustomer.biz.config;

import com.ifinance.aicustomer.biz.mcp.McpClientFactory;
import com.ifinance.aicustomer.biz.mcp.McpClientRegistry;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 外部 MCP 数据客户端装配，按 {@code mcp.enabled} 控制注册。
 */
@Configuration
public class McpConfig {

    private static final Logger log = LoggerFactory.getLogger(McpConfig.class);

    /**
     * 启用时聚合所有已启用的 MCP 服务器，向模型暴露外部工具。
     */
    @Bean("mcpToolProvider")
    @ConditionalOnProperty(name = "mcp.enabled", havingValue = "true")
    public McpToolProvider mcpToolProvider(McpProperties properties,
                                           McpClientFactory clientFactory,
                                           McpClientRegistry registry) {
        List<McpClient> clients = properties.getServers().stream()
                .filter(McpServerProperties::isEnabled)
                .map(server -> registerClient(clientFactory, registry, server))
                .toList();
        log.info("初始化 MCP 外部数据工具, servers={}", clients.stream().map(McpClient::key).toList());
        return McpToolProvider.builder()
                .mcpClients(clients)
                .build();
    }

    /**
     * 关闭时提供空 ToolProvider，保证 AI Service 正常装配。
     */
    @Bean("mcpToolProvider")
    @ConditionalOnProperty(name = "mcp.enabled", havingValue = "false", matchIfMissing = true)
    public ToolProvider emptyMcpToolProvider() {
        return request -> ToolProviderResult.builder().build();
    }

    private McpClient registerClient(McpClientFactory clientFactory,
                                     McpClientRegistry registry,
                                     McpServerProperties server) {
        McpClient client = clientFactory.create(server);
        registry.register(client);
        return client;
    }
}
