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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 远程 A 股 MCP 服务端到端测试，默认跳过，执行时需添加 -Dmcp.remote.e2e=true。
 */
@EnabledIfSystemProperty(named = "mcp.remote.e2e", matches = "true")
class McpRemoteIntegrationTest {

    @Test
    void shouldListAndCallRemoteAStockTools() {
        McpServerProperties server = new McpServerProperties();
        server.setName("cn-a-stock");
        server.setTransport(McpTransportType.HTTP);
        server.setUrl("http://82.156.17.205/cnstock/mcp");
        server.setRetryOnConnectionError(true);
        server.setTimeoutSeconds(10);

        McpClientFactory factory = new McpClientFactory(new McpTransportFactoryResolver(List.of(
                new StdioMcpTransportFactory(),
                new SseMcpTransportFactory(),
                new StreamableHttpMcpTransportFactory())));
        McpClientRegistry registry = new McpClientRegistry();
        McpClient client = factory.create(server);
        registry.register(client);
        try {
            List<ToolSpecification> tools = client.listTools();
            assertTrue(tools.stream().anyMatch(tool -> "brief".equals(tool.name())));
            assertTrue(tools.stream().anyMatch(tool -> "medium".equals(tool.name())));
            assertTrue(tools.stream().anyMatch(tool -> "full".equals(tool.name())));

            ToolExecutionResult result = client.executeTool(ToolExecutionRequest.builder()
                    .id("remote-1")
                    .name("brief")
                    .arguments("{\"symbol\":\"SH600000\"}")
                    .build());
            assertFalse(result.isError());
            assertTrue(result.resultText().contains("SH600000"));
            assertTrue(result.resultText().contains("浦发银行"));
        } finally {
            registry.destroy();
        }
    }
}
