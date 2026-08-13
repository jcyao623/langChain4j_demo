package com.ifinance.aicustomer.biz.mcp;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolExecutionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetryableMcpClientTest {

    @Mock
    private McpClient delegate;

    @Test
    void shouldRetryListToolsOnConnectionError() {
        ToolSpecification tool = ToolSpecification.builder().name("get_exchange_rate").build();
        when(delegate.listTools())
                .thenThrow(new RuntimeException(new IOException("Connection reset")))
                .thenReturn(List.of(tool));

        RetryableMcpClient client = new RetryableMcpClient(delegate);

        assertEquals(List.of(tool), client.listTools());
        verify(delegate, times(2)).listTools();
    }

    @Test
    void shouldRetryExecuteToolOnConnectionError() {
        ToolExecutionResult result = ToolExecutionResult.builder().resultText("ok").build();
        ToolExecutionRequest request = ToolExecutionRequest.builder()
                .id("1")
                .name("brief")
                .arguments("{}")
                .build();
        when(delegate.executeTool(request))
                .thenThrow(new RuntimeException(new IOException("Connection reset")))
                .thenReturn(result);

        RetryableMcpClient client = new RetryableMcpClient(delegate);

        assertEquals(result, client.executeTool(request));
        verify(delegate, times(2)).executeTool(request);
    }

    @Test
    void shouldNotRetryNonConnectionError() {
        when(delegate.listTools()).thenThrow(new RuntimeException("AI error"));

        RetryableMcpClient client = new RetryableMcpClient(delegate);

        assertThrows(RuntimeException.class, client::listTools);
        verify(delegate, times(1)).listTools();
    }
}
