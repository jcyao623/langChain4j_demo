package com.ifinance.aicustomer.biz.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfig.class);

    @Test
    void shouldUseDefaultsWhenNotConfigured() {
        contextRunner.run(context -> {
            McpProperties properties = context.getBean(McpProperties.class);
            assertFalse(properties.isEnabled());
            assertEquals("stdio", properties.getTransport());
            assertEquals(List.of("python", "mcp-server/market_data_server.py"), properties.getServerCommand());
            assertEquals(30L, properties.getInitializationTimeoutSeconds());
        });
    }

    @Test
    void shouldBindConfiguredValues() {
        contextRunner
                .withPropertyValues(
                        "mcp.enabled=true",
                        "mcp.transport=stdio",
                        "mcp.server-command[0]=python",
                        "mcp.server-command[1]=../mcp-server/market_data_server.py",
                        "mcp.environment.PYTHONUTF8=1",
                        "mcp.timeout-seconds=15",
                        "mcp.initialization-timeout-seconds=20",
                        "mcp.tool-execution-timeout-seconds=90",
                        "mcp.log-events=true")
                .run(context -> {
                    McpProperties properties = context.getBean(McpProperties.class);
                    assertTrue(properties.isEnabled());
                    assertEquals(List.of("python", "../mcp-server/market_data_server.py"), properties.getServerCommand());
                    assertEquals("1", properties.getEnvironment().get("PYTHONUTF8"));
                    assertEquals(15L, properties.getTimeoutSeconds());
                    assertEquals(20L, properties.getInitializationTimeoutSeconds());
                    assertEquals(90L, properties.getToolExecutionTimeoutSeconds());
                    assertTrue(properties.isLogEvents());
                });
    }

    @EnableConfigurationProperties(McpProperties.class)
    static class PropertiesConfig {
    }
}
