package com.ifinance.aicustomer.biz.mcp;

import com.ifinance.aicustomer.biz.config.McpServerProperties;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * SSE 传输工厂。
 */
@Component
public class SseMcpTransportFactory implements McpTransportFactory {

    private static final Logger log = LoggerFactory.getLogger(SseMcpTransportFactory.class);

    @Override
    public McpTransportType type() {
        return McpTransportType.SSE;
    }

    @Override
    public McpTransport create(McpServerProperties server) {
        if (!StringUtils.hasText(server.getUrl())) {
            throw new IllegalArgumentException("MCP SSE 服务需要配置 url: " + server.getName());
        }
        log.info("初始化 MCP SSE 外部数据服务, server={}, url={}", server.getName(), server.getUrl());
        return HttpMcpTransport.builder()
                .sseUrl(server.getUrl())
                .timeout(Duration.ofSeconds(server.getTimeoutSeconds()))
                .build();
    }
}
