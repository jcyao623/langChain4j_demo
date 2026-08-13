package com.ifinance.aicustomer.biz.config;

import dev.langchain4j.service.tool.ToolProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class McpConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(McpConfig.class, PropertiesConfig.class);

    @Test
    void shouldProvideEmptyToolProviderWhenDisabled() {
        contextRunner
                .withPropertyValues("mcp.enabled=false")
                .run(context -> {
                    ToolProvider provider = context.getBean("marketDataMcpToolProvider", ToolProvider.class);
                    assertNotNull(provider);
                });
    }

    @Test
    void shouldRejectUnsupportedTransport() {
        contextRunner
                .withPropertyValues("mcp.enabled=true", "mcp.transport=invalid")
                .run(context -> assertThat(context.getStartupFailure())
                        .hasStackTraceContaining("不支持的 MCP transport"));
    }

    @EnableConfigurationProperties(McpProperties.class)
    static class PropertiesConfig {
    }
}
