package com.ifinance.aicustomer.biz;

import com.ifinance.aicustomer.biz.config.McpServerProperties;
import com.ifinance.aicustomer.biz.mcp.McpClientFactory;
import com.ifinance.aicustomer.biz.mcp.McpClientRegistry;
import com.ifinance.aicustomer.biz.mcp.McpTransportFactoryResolver;
import com.ifinance.aicustomer.biz.mcp.McpTransportType;
import com.ifinance.aicustomer.biz.mcp.SseMcpTransportFactory;
import com.ifinance.aicustomer.biz.mcp.StdioMcpTransportFactory;
import com.ifinance.aicustomer.biz.mcp.StreamableHttpMcpTransportFactory;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolExecutionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * stdio 外部 MCP 服务端到端测试，默认跳过，执行时需添加 -Dmcp.e2e=true。
 */
@EnabledIfSystemProperty(named = "mcp.e2e", matches = "true")
class McpIntegrationTest {

    @Test
    void shouldListAndCallExternalToolsViaStdio() throws Exception {
        Path script = findScript();
        assertTrue(Files.exists(script), "找不到 mcp-server/market_data_server.py");

        McpServerProperties server = new McpServerProperties();
        server.setName("market-data");
        server.setTransport(McpTransportType.STDIO);
        server.setServerCommand(List.of("python", script.toString()));
        server.setEnvironment(Map.of("PYTHONIOENCODING", "utf-8", "PYTHONUTF8", "1"));
        server.setLogEvents(true);

        McpClientFactory factory = new McpClientFactory(transportFactoryResolver());
        McpClientRegistry registry = new McpClientRegistry();
        McpClient client = factory.create(server);
        registry.register(client);
        try {
            List<ToolSpecification> tools = client.listTools();
            assertTrue(tools.stream().anyMatch(tool -> "get_exchange_rate".equals(tool.name())));

            ToolExecutionResult result = client.executeTool(ToolExecutionRequest.builder()
                    .id("test-1")
                    .name("get_exchange_rate")
                    .arguments("{\"currency\":\"USD\"}")
                    .build());
            assertFalse(result.isError());
            assertTrue(result.resultText().contains("USD"));
            assertTrue(result.resultText().contains("7.2500"));
        } finally {
            registry.destroy();
        }
    }

    private McpTransportFactoryResolver transportFactoryResolver() {
        return new McpTransportFactoryResolver(List.of(
                new StdioMcpTransportFactory(),
                new SseMcpTransportFactory(),
                new StreamableHttpMcpTransportFactory()));
    }

    private Path findScript() {
        Path current = Path.of("").toAbsolutePath();
        for (int i = 0; i < 4; i++) {
            Path candidate = current.resolve("mcp-server/market_data_server.py");
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("未找到 mcp-server/market_data_server.py");
    }
}
