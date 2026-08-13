# MCP 外部数据接入

## 1. 概述

智能客服通过 LangChain4j MCP 客户端连接外部市场数据服务，用户询问行情、汇率、利率或基金净值时，模型可调用外部工具获取数据后作答。

当前实现采用 `stdio` 传输：应用启动时自动拉起 `mcp-server/market_data_server.py` 子进程，子进程通过标准输入输出与主应用完成 MCP 协议通信。该脚本返回静态演示数据，不依赖外部网络，便于本地联调；后续替换数据源时只需修改脚本内的数据或查询逻辑。

## 2. 数据工具

| 工具 | 说明 |
| --- | --- |
| `get_deposit_rates` | 查询存款利率 |
| `get_loan_rates` | 查询贷款利率 |
| `get_fund_nav` | 按基金代码查询基金净值 |
| `get_stock_quote` | 按股票代码查询 A 股行情 |
| `get_exchange_rate` | 按币种查询人民币汇率中间价 |

## 3. 安装依赖

```powershell
pip install -r mcp-server/requirements.txt
```

## 4. Nacos 配置

在 `nacos-config/langchain4j-demo.yaml` 中启用：

```yaml
mcp:
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
```

应用从项目根目录启动时使用默认相对路径；若从 `biz` 模块目录启动，请设置环境变量 `MCP_SERVER_SCRIPT=../mcp-server/market_data_server.py`，或改为绝对路径。

## 5. 运行验证

启动应用后调用对话接口：

```powershell
Invoke-RestMethod -Method Post -Uri http://127.0.0.1:8080/api/v1/chat -ContentType 'application/json' -Body '{"message":"今天美元兑人民币汇率是多少","sessionId":"demo-mcp-001"}'
```

模型会调用 `get_exchange_rate` 工具并返回演示汇率数据。执行 MCP 端到端测试（需本机安装 Python 与 `mcp` 依赖）：

```powershell
mvn -pl biz -am test -Dmcp.e2e=true
```

## 6. 切换为 SSE 外部服务

如果外部 MCP 服务通过 SSE 提供，修改配置即可，无需改动代码：

```yaml
mcp:
  enabled: true
  transport: sse
  sse-url: https://example.com/mcp/sse
```

## 7. 接入真实数据源

`market_data_server.py` 中的数据与查询函数一一对应。接入真实行情、汇率或基金净值 API 时，保持工具名与入参结构不变，在对应分支中替换为 HTTP 查询逻辑即可，主应用无需改动。
