package com.ifinance.aicustomer.biz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 外部 MCP 数据服务配置。
 */
@Component
@ConfigurationProperties(prefix = "mcp")
public class McpProperties {

    private boolean enabled = false;
    private String transport = "stdio";
    private List<String> serverCommand = new ArrayList<>(List.of("python", "mcp-server/market_data_server.py"));
    private String sseUrl;
    private Map<String, String> environment = new HashMap<>(Map.of(
            "PYTHONIOENCODING", "utf-8",
            "PYTHONUTF8", "1"));
    private long timeoutSeconds = 30L;
    private long initializationTimeoutSeconds = 30L;
    private long toolExecutionTimeoutSeconds = 60L;
    private boolean logEvents = false;

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
     * 获取传输方式：stdio 或 sse。
     */
    public String getTransport() {
        return transport;
    }

    /**
     * 设置传输方式：stdio 或 sse。
     */
    public void setTransport(String transport) {
        this.transport = transport;
    }

    /**
     * 获取 stdio 模式的启动命令。
     */
    public List<String> getServerCommand() {
        return serverCommand;
    }

    /**
     * 设置 stdio 模式的启动命令。
     */
    public void setServerCommand(List<String> serverCommand) {
        this.serverCommand = serverCommand;
    }

    /**
     * 获取 sse 模式的 MCP 服务地址。
     */
    public String getSseUrl() {
        return sseUrl;
    }

    /**
     * 设置 sse 模式的 MCP 服务地址。
     */
    public void setSseUrl(String sseUrl) {
        this.sseUrl = sseUrl;
    }

    /**
     * 获取子进程环境变量。
     */
    public Map<String, String> getEnvironment() {
        return environment;
    }

    /**
     * 设置子进程环境变量。
     */
    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    /**
     * 获取 sse 模式连接超时（秒）。
     */
    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    /**
     * 设置 sse 模式连接超时（秒）。
     */
    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * 获取初始化超时（秒）。
     */
    public long getInitializationTimeoutSeconds() {
        return initializationTimeoutSeconds;
    }

    /**
     * 设置初始化超时（秒）。
     */
    public void setInitializationTimeoutSeconds(long initializationTimeoutSeconds) {
        this.initializationTimeoutSeconds = initializationTimeoutSeconds;
    }

    /**
     * 获取工具执行超时（秒）。
     */
    public long getToolExecutionTimeoutSeconds() {
        return toolExecutionTimeoutSeconds;
    }

    /**
     * 设置工具执行超时（秒）。
     */
    public void setToolExecutionTimeoutSeconds(long toolExecutionTimeoutSeconds) {
        this.toolExecutionTimeoutSeconds = toolExecutionTimeoutSeconds;
    }

    /**
     * 是否打印 stdio 进程日志。
     */
    public boolean isLogEvents() {
        return logEvents;
    }

    /**
     * 设置是否打印 stdio 进程日志。
     */
    public void setLogEvents(boolean logEvents) {
        this.logEvents = logEvents;
    }
}
