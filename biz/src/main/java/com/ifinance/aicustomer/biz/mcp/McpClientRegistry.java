package com.ifinance.aicustomer.biz.mcp;

import dev.langchain4j.mcp.client.McpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * MCP 客户端注册表，统一负责客户端生命周期管理。
 */
@Component
public class McpClientRegistry implements DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(McpClientRegistry.class);

    private final List<McpClient> clients = new CopyOnWriteArrayList<>();

    /**
     * 注册一个 MCP 客户端，应用关闭时统一释放。
     */
    public void register(McpClient client) {
        clients.add(client);
    }

    @Override
    public void destroy() {
        for (McpClient client : clients) {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("关闭 MCP 客户端失败, client={}", client.key(), e);
            }
        }
        clients.clear();
    }
}
