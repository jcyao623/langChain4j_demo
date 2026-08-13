# 外部 MCP 市场数据服务

本项目提供一个 stdio 模式的 MCP 数据服务，向智能客服暴露金融行情类工具：

- `get_deposit_rates`：存款利率
- `get_loan_rates`：贷款利率
- `get_fund_nav`：基金净值
- `get_stock_quote`：A 股行情
- `get_exchange_rate`：人民币汇率中间价

当前返回静态演示数据，便于本地联调；接入真实行情源时替换 `market_data_server.py` 中的查询逻辑即可。

## 安装依赖

```powershell
pip install -r mcp-server/requirements.txt
```

## 手动验证

```powershell
python mcp-server/market_data_server.py
```

应用通过 `mcp.transport=stdio` 自动拉起该进程，无需手动启动。
