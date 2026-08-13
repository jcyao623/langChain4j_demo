package com.ifinance.aicustomer.biz.mcp;

import com.ifinance.aicustomer.biz.config.McpServerProperties;
import dev.langchain4j.mcp.client.transport.McpTransport;

/**
 * MCP 传输层工厂，每种传输方式提供独立实现，便于后续扩展。
 */
public interface McpTransportFactory {

    /**
     * 获取该工厂支持的传输方式。
     */
    McpTransportType type();

    /**
     * 根据服务器配置创建传输层。
     */
    McpTransport create(McpServerProperties server);
}
