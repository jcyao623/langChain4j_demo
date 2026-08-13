package com.ifinance.aicustomer.biz.config;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Locale;

/**
 * 外部 MCP 数据客户端配置，按 {@code mcp.enabled} 控制装配。
 */
@Configuration
public class McpConfig {

    private static final Logger log = LoggerFactory.getLogger(McpConfig.class);

    /**
     * 构建 MCP 客户端，服务通过 stdio 子进程或 sse 地址访问。
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "mcp.enabled", havingValue = "true")
    public McpClient marketDataMcpClient(McpProperties properties) {
        McpTransport transport = buildTransport(properties);
        return DefaultMcpClient.builder()
                .transport(transport)
                .clientName("langchain4j-demo-market-data-client")
                .initializationTimeout(Duration.ofSeconds(properties.getInitializationTimeoutSeconds()))
                .toolExecutionTimeout(Duration.ofSeconds(properties.getToolExecutionTimeoutSeconds()))
                .build();
    }

    /**
     * 启用时提供包含外部市场数据工具的 ToolProvider。
     */
    @Bean("marketDataMcpToolProvider")
    @ConditionalOnProperty(name = "mcp.enabled", havingValue = "true")
    public McpToolProvider marketDataMcpToolProvider(McpClient marketDataMcpClient) {
        log.info("初始化 MCP 外部数据工具, client={}", marketDataMcpClient.key());
        return McpToolProvider.builder()
                .mcpClients(marketDataMcpClient)
                .build();
    }

    /**
     * 关闭时提供空 ToolProvider，保证 AI Service 正常装配。
     */
    @Bean("marketDataMcpToolProvider")
    @ConditionalOnProperty(name = "mcp.enabled", havingValue = "false", matchIfMissing = true)
    public ToolProvider emptyMarketDataMcpToolProvider() {
        return request -> ToolProviderResult.builder().build();
    }

    private McpTransport buildTransport(McpProperties properties) {
        String transport = StringUtils.hasText(properties.getTransport())
                ? properties.getTransport().trim().toLowerCase(Locale.ROOT)
                : "stdio";
        return switch (transport) {
            case "stdio" -> {
                if (properties.getServerCommand() == null || properties.getServerCommand().isEmpty()) {
                    throw new IllegalArgumentException("mcp.server-command 不能为空（stdio 模式）");
                }
                log.info("初始化 MCP stdio 外部数据服务, command={}", properties.getServerCommand());
                yield StdioMcpTransport.builder()
                        .command(properties.getServerCommand())
                        .environment(properties.getEnvironment())
                        .logEvents(properties.isLogEvents())
                        .build();
            }
            case "sse" -> {
                if (!StringUtils.hasText(properties.getSseUrl())) {
                    throw new IllegalArgumentException("mcp.sse-url 不能为空（sse 模式）");
                }
                log.info("初始化 MCP SSE 外部数据服务, url={}", properties.getSseUrl());
                yield HttpMcpTransport.builder()
                        .sseUrl(properties.getSseUrl())
                        .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                        .build();
            }
            default -> throw new IllegalArgumentException("不支持的 MCP transport: " + transport);
        };
    }
}
