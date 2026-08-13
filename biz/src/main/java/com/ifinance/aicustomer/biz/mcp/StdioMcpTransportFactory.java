package com.ifinance.aicustomer.biz.mcp;

import com.ifinance.aicustomer.biz.config.McpServerProperties;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * stdio 子进程传输工厂。
 */
@Component
public class StdioMcpTransportFactory implements McpTransportFactory {

    private static final Logger log = LoggerFactory.getLogger(StdioMcpTransportFactory.class);

    @Override
    public McpTransportType type() {
        return McpTransportType.STDIO;
    }

    @Override
    public McpTransport create(McpServerProperties server) {
        if (server.getServerCommand() == null || server.getServerCommand().isEmpty()) {
            throw new IllegalArgumentException("MCP stdio 服务需要配置 server-command: " + server.getName());
        }
        log.info("初始化 MCP stdio 外部数据服务, server={}, command={}", server.getName(), server.getServerCommand());
        return StdioMcpTransport.builder()
                .command(server.getServerCommand())
                .environment(server.getEnvironment())
                .logEvents(server.isLogEvents())
                .build();
    }
}
