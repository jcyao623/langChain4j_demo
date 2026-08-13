package com.ifinance.aicustomer.biz;

import com.ifinance.aicustomer.biz.config.McpConfig;
import com.ifinance.aicustomer.biz.config.McpProperties;
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

        McpProperties properties = new McpProperties();
        properties.setTransport("stdio");
        properties.setServerCommand(List.of("python", script.toString()));
        properties.setEnvironment(Map.of("PYTHONIOENCODING", "utf-8", "PYTHONUTF8", "1"));
        properties.setLogEvents(true);

        McpClient client = new McpConfig().marketDataMcpClient(properties);
        try (McpClient ignored = client) {
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
        }
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
