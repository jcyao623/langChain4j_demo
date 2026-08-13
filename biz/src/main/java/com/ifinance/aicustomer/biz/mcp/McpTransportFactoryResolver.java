package com.ifinance.aicustomer.biz.mcp;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 传输工厂注册表，按传输方式选择对应实现。
 */
@Component
public class McpTransportFactoryResolver {

    private final Map<McpTransportType, McpTransportFactory> factories;

    public McpTransportFactoryResolver(List<McpTransportFactory> factories) {
        this.factories = factories.stream()
                .collect(Collectors.toUnmodifiableMap(McpTransportFactory::type, Function.identity()));
    }

    /**
     * 获取指定传输方式的工厂。
     */
    public McpTransportFactory resolve(McpTransportType type) {
        McpTransportFactory factory = factories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("不支持的 MCP transport: " + type);
        }
        return factory;
    }
}
