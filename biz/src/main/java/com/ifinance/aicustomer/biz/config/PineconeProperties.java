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

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isInitOnStartup() {
        return initOnStartup;
    }

    public void setInitOnStartup(boolean initOnStartup) {
        this.initOnStartup = initOnStartup;
    }
}
