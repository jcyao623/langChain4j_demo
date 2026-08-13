package com.ifinance.aicustomer.biz.mcp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class McpTransportFactoryResolverTest {

    @Mock
    private McpTransportFactory stdioFactory;

    @Test
    void shouldResolveFactoryByTransportType() {
        when(stdioFactory.type()).thenReturn(McpTransportType.STDIO);

        McpTransportFactoryResolver resolver = new McpTransportFactoryResolver(List.of(stdioFactory));

        assertSame(stdioFactory, resolver.resolve(McpTransportType.STDIO));
    }

    @Test
    void shouldRejectUnsupportedTransport() {
        when(stdioFactory.type()).thenReturn(McpTransportType.STDIO);

        McpTransportFactoryResolver resolver = new McpTransportFactoryResolver(List.of(stdioFactory));

        assertThrows(IllegalArgumentException.class, () -> resolver.resolve(McpTransportType.SSE));
    }
}
