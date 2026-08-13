package com.ifinance.aicustomer.biz.config;

import com.ifinance.aicustomer.biz.mcp.McpClientFactory;
import com.ifinance.aicustomer.biz.mcp.McpClientRegistry;
import com.ifinance.aicustomer.biz.mcp.McpTransportFactoryResolver;
import com.ifinance.aicustomer.biz.mcp.SseMcpTransportFactory;
import com.ifinance.aicustomer.biz.mcp.StdioMcpTransportFactory;
import com.ifinance.aicustomer.biz.mcp.StreamableHttpMcpTransportFactory;
import dev.langchain4j.service.tool.ToolProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class McpConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(
                    McpConfig.class,
                    PropertiesConfig.class,
                    McpClientFactory.class,
                    McpClientRegistry.class,
                    McpTransportFactoryResolver.class,
                    StdioMcpTransportFactory.class,
                    SseMcpTransportFactory.class,
                    StreamableHttpMcpTransportFactory.class);

    @Test
    void shouldProvideEmptyToolProviderWhenDisabled() {
        contextRunner
                .withPropertyValues("mcp.enabled=false")
                .run(context -> {
                    ToolProvider provider = context.getBean("mcpToolProvider", ToolProvider.class);
                    assertNotNull(provider);
                });
    }

    @Test
    void shouldRejectHttpServerWithoutUrl() {
        contextRunner
                .withPropertyValues(
                        "mcp.enabled=true",
                        "mcp.servers[0].name=cn-a-stock",
                        "mcp.servers[0].transport=http")
                .run(context -> assertThat(context.getStartupFailure())
                        .hasStackTraceContaining("MCP HTTP 服务需要配置 url"));
    }

    @EnableConfigurationProperties(McpProperties.class)
    static class PropertiesConfig {
    }
}
