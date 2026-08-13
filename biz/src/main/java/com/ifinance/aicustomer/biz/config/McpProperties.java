package com.ifinance.aicustomer.biz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 外部 MCP 数据服务配置，支持按服务器列表扩展多个数据源。
 */
@Component
@ConfigurationProperties(prefix = "mcp")
public class McpProperties {

    private boolean enabled = false;
    private List<McpServerProperties> servers = new ArrayList<>();

    /**
     * 是否启用 MCP 外部数据。
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用 MCP 外部数据。
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取外部 MCP 服务器列表。
     */
    public List<McpServerProperties> getServers() {
        return servers;
    }

    /**
     * 设置外部 MCP 服务器列表。
     */
    public void setServers(List<McpServerProperties> servers) {
        this.servers = servers;
    }
}
