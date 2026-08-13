package com.ifinance.aicustomer.biz.config;

import com.ifinance.aicustomer.biz.mcp.McpTransportType;
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
            assertTrue(properties.getServers().isEmpty());
        });
    }

    @Test
    void shouldBindMultipleServers() {
        contextRunner
                .withPropertyValues(
                        "mcp.enabled=true",
                        "mcp.servers[0].name=market-data",
                        "mcp.servers[0].enabled=true",
                        "mcp.servers[0].transport=stdio",
                        "mcp.servers[0].server-command[0]=python",
                        "mcp.servers[0].server-command[1]=mcp-server/market_data_server.py",
                        "mcp.servers[0].initialization-timeout-seconds=20",
                        "mcp.servers[0].tool-execution-timeout-seconds=90",
                        "mcp.servers[1].name=cn-a-stock",
                        "mcp.servers[1].transport=http",
                        "mcp.servers[1].url=http://82.156.17.205/cnstock/mcp",
                        "mcp.servers[1].timeout-seconds=15",
                        "mcp.servers[1].custom-headers.X-Client=test",
                        "mcp.servers[1].retry-on-connection-error=false")
                .run(context -> {
                    McpProperties properties = context.getBean(McpProperties.class);
                    assertTrue(properties.isEnabled());

                    List<McpServerProperties> servers = properties.getServers();
                    assertEquals(2, servers.size());

                    McpServerProperties local = servers.get(0);
                    assertEquals("market-data", local.getName());
                    assertEquals(McpTransportType.STDIO, local.getTransport());
                    assertEquals(List.of("python", "mcp-server/market_data_server.py"), local.getServerCommand());
                    assertEquals(20L, local.getInitializationTimeoutSeconds());
                    assertEquals(90L, local.getToolExecutionTimeoutSeconds());

                    McpServerProperties remote = servers.get(1);
                    assertEquals("cn-a-stock", remote.getName());
                    assertEquals(McpTransportType.HTTP, remote.getTransport());
                    assertEquals("http://82.156.17.205/cnstock/mcp", remote.getUrl());
                    assertEquals(15L, remote.getTimeoutSeconds());
                    assertEquals("test", remote.getCustomHeaders().get("X-Client"));
                    assertFalse(remote.isRetryOnConnectionError());
                });
    }

    @EnableConfigurationProperties(McpProperties.class)
    static class PropertiesConfig {
    }
}
