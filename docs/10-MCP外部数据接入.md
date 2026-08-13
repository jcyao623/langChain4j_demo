# MCP 外部数据接入

## 1. 概述

智能客服通过 LangChain4j MCP 客户端连接多个外部数据服务，用户询问行情、汇率、利率、基金净值或 A 股个股数据时，模型可调用外部工具获取数据后作答。

配置采用多服务器结构：每个 MCP 数据源对应 `mcp.servers` 中的一个条目，传输方式由独立工厂实现，后续新增数据源只需增加配置，必要时补充新的传输工厂即可，主装配逻辑无需改动。

当前内置两个数据源：

- `market-data`：本地 stdio 子进程服务，提供利率、基金净值、汇率等演示数据，不依赖外部网络。
- `cn-a-stock`：远程 streamable-http A 股行情服务，提供个股基本数据、行情、财务数据与技术指标。

## 2. 数据工具

### market-data

| 工具 | 说明 |
| --- | --- |
| `get_deposit_rates` | 查询存款利率 |
| `get_loan_rates` | 查询贷款利率 |
| `get_fund_nav` | 按基金代码查询基金净值 |
| `get_stock_quote` | 按股票代码查询本地演示行情 |
| `get_exchange_rate` | 按币种查询人民币汇率中间价 |

### cn-a-stock

| 工具 | 说明 |
| --- | --- |
| `brief` | 股票基本信息与行情数据 |
| `medium` | 基本信息与部分财务数据 |
| `full` | 全部数据与技术指标 |

## 3. 安装本地依赖

```powershell
pip install -r mcp-server/requirements.txt
```

仅使用远程 `cn-a-stock` 时无需安装 Python 依赖，可将其余服务器条目 `enabled` 设为 `false`。

## 4. Nacos 配置

在 `nacos-config/langchain4j-demo.yaml` 中启用：

```yaml
mcp:
  enabled: true
  servers:
    - name: market-data
      enabled: true
      transport: stdio
      server-command:
        - python
        - ${MCP_SERVER_SCRIPT:mcp-server/market_data_server.py}
      environment:
        PYTHONIOENCODING: utf-8
        PYTHONUTF8: "1"
      initialization-timeout-seconds: 30
      tool-execution-timeout-seconds: 60
      log-events: false
    - name: cn-a-stock
      enabled: true
      transport: http
      url: http://82.156.17.205/cnstock/mcp
      retry-on-connection-error: true
      initialization-timeout-seconds: 10
      tool-execution-timeout-seconds: 20
      timeout-seconds: 10
```

应用从项目根目录启动时使用默认相对路径；若从 `biz` 模块目录启动，请设置环境变量 `MCP_SERVER_SCRIPT=../mcp-server/market_data_server.py`，或改为绝对路径。

## 5. 运行验证

启动应用后调用对话接口：

```powershell
Invoke-RestMethod -Method Post -Uri http://127.0.0.1:8080/api/v1/chat -ContentType 'application/json' -Body '{"message":"今天美元兑人民币汇率是多少","sessionId":"demo-mcp-001"}'
```

查询 A 股个股时：

```powershell
Invoke-RestMethod -Method Post -Uri http://127.0.0.1:8080/api/v1/chat -ContentType 'application/json' -Body '{"message":"分析一下浦发银行最近的走势","sessionId":"demo-mcp-002"}'
```

端到端测试（需本机安装 Python 与 `mcp` 依赖）：

```powershell
mvn -pl biz -am test -Dmcp.e2e=true
```

远程 A 股服务测试（依赖公网可达）：

```powershell
mvn -pl biz -am test -Dmcp.remote.e2e=true
```

## 6. 传输方式

| transport | 实现 | 适用场景 |
| --- | --- | --- |
| `stdio` | `StdioMcpTransportFactory` | 本地子进程服务 |
| `sse` | `SseMcpTransportFactory` | 传统 SSE 远程服务 |
| `http` | `StreamableHttpMcpTransportFactory` | streamable-http 远程服务 |

新增传输方式时，实现 `McpTransportFactory` 并注册为 Spring 组件即可，无需改动装配逻辑。

远程 A 股服务会重置空闲连接，客户端已对连接异常自动重试一次（`retry-on-connection-error`），同时配置较短超时，减少不可用时的等待时间与告警影响。

## 7. 接入新的 MCP 数据源

1. 在 `mcp.servers` 中新增一个条目，配置唯一 `name`、`transport` 与对应地址。
2. 若使用 `stdio`，确保启动命令可用；若使用远程服务，配置 `url`。
3. 重启应用后，`McpConfig` 会自动创建客户端并合并到 `mcpToolProvider`，模型即可调用新工具。

本地 `market_data_server.py` 中的数据与查询函数一一对应，接入真实行情、汇率或基金净值 API 时，保持工具名与入参结构不变，替换对应分支的查询逻辑即可。
