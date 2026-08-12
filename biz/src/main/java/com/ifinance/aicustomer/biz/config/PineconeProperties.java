package com.ifinance.aicustomer.biz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Pinecone 向量数据库配置。
 */
@Component
@ConfigurationProperties(prefix = "pinecone")
public class PineconeProperties {

    private String apiKey;
    private String index = "ai";
    private String namespace = "__default__";
    private boolean enabled = true;
    private boolean initOnStartup = false;

    /**
     * 获取 Pinecone API Key。
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * 设置 Pinecone API Key。
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 获取索引名称。
     */
    public String getIndex() {
        return index;
    }

    /**
     * 设置索引名称。
     */
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * 获取命名空间。
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * 设置命名空间。
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * 是否启用 Pinecone。
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用 Pinecone。
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 是否在启动时初始化知识库。
     */
    public boolean isInitOnStartup() {
        return initOnStartup;
    }

    /**
     * 设置是否在启动时初始化知识库。
     */
    public void setInitOnStartup(boolean initOnStartup) {
        this.initOnStartup = initOnStartup;
    }
}
