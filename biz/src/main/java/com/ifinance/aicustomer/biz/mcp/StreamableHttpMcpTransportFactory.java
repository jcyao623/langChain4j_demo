package com.ifinance.aicustomer.biz.mcp;

import com.ifinance.aicustomer.biz.config.McpServerProperties;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * streamable-http 远程服务传输工厂。
 */
@Component
public class StreamableHttpMcpTransportFactory implements McpTransportFactory {

    private static final Logger log = LoggerFactory.getLogger(StreamableHttpMcpTransportFactory.class);

    @Override
    public McpTransportType type() {
        return McpTransportType.HTTP;
    }

    @Override
    public McpTransport create(McpServerProperties server) {
        if (!StringUtils.hasText(server.getUrl())) {
            throw new IllegalArgumentException("MCP HTTP 服务需要配置 url: " + server.getName());
        }
        log.info("初始化 MCP HTTP 外部数据服务, server={}, url={}", server.getName(), server.getUrl());
        return StreamableHttpMcpTransport.builder()
                .url(server.getUrl())
                .customHeaders(server.getCustomHeaders())
                .timeout(Duration.ofSeconds(server.getTimeoutSeconds()))
                .build();
    }
}
