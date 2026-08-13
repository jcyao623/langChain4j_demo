"""金融行情类外部 MCP 数据服务（stdio 模式）。

该服务通过 MCP 协议向主应用提供演示用市场数据，包含存款利率、
贷款利率、基金净值、股票行情与汇率查询工具，供智能客服调用。
数据为静态演示数据，接入真实数据源时只需替换下方数据或查询逻辑。
"""

import json
from typing import Any

import anyio

from mcp import types
from mcp.server.lowlevel import Server
from mcp.server.stdio import stdio_server


DEPOSIT_RATES = {
    "demand": {"name": "活期存款", "rate": "0.20%"},
    "3m": {"name": "三个月定期", "rate": "1.15%"},
    "6m": {"name": "六个月定期", "rate": "1.35%"},
    "1y": {"name": "一年定期", "rate": "1.45%"},
    "2y": {"name": "两年定期", "rate": "1.65%"},
    "3y": {"name": "三年定期", "rate": "1.95%"},
}

LOAN_RATES = {
    "consumer": {"name": "消费贷", "rate": "3.45%", "note": "年化利率，以实际审批为准"},
    "business": {"name": "经营贷", "rate": "3.60%", "note": "年化利率，以实际审批为准"},
    "mortgage": {"name": "房贷（5年以上 LPR）", "rate": "3.45%", "note": "LPR 基准，加点另行计算"},
    "housing_fund": {"name": "公积金贷款（5年以上）", "rate": "2.85%", "note": "首套住房执行利率"},
}

FUND_NAV = {
    "000001": {"name": "华夏成长混合", "nav": "1.2860", "date": "2026-08-12", "change": "+0.45%"},
    "110022": {"name": "易方达消费行业股票", "nav": "3.8520", "date": "2026-08-12", "change": "-0.28%"},
    "161725": {"name": "招商中证白酒指数", "nav": "1.1240", "date": "2026-08-12", "change": "+1.12%"},
    "519674": {"name": "银河创新成长混合", "nav": "5.6320", "date": "2026-08-12", "change": "+0.86%"},
}

STOCK_QUOTES = {
    "600519": {"name": "贵州茅台", "price": "1688.00", "change": "+1.25%", "currency": "CNY"},
    "000001": {"name": "平安银行", "price": "11.86", "change": "+0.68%", "currency": "CNY"},
    "601318": {"name": "中国平安", "price": "52.40", "change": "-0.36%", "currency": "CNY"},
    "300750": {"name": "宁德时代", "price": "186.72", "change": "+2.10%", "currency": "CNY"},
    "000858": {"name": "五粮液", "price": "142.55", "change": "+0.92%", "currency": "CNY"},
}

EXCHANGE_RATES = {
    "USD": {"currency": "美元", "rate": "7.2500", "unit": "1 USD"},
    "EUR": {"currency": "欧元", "rate": "7.8500", "unit": "1 EUR"},
    "JPY": {"currency": "日元", "rate": "0.0480", "unit": "100 JPY"},
    "HKD": {"currency": "港币", "rate": "0.9300", "unit": "1 HKD"},
    "GBP": {"currency": "英镑", "rate": "9.1000", "unit": "1 GBP"},
}


def _json_text(payload: dict[str, Any]) -> str:
    return json.dumps(payload, ensure_ascii=False, indent=2)


def _tool(name: str, description: str, properties: dict[str, Any]) -> types.Tool:
    return types.Tool(
        name=name,
        description=description,
        inputSchema={"type": "object", "properties": properties},
    )


TOOLS = [
    _tool(
        "get_deposit_rates",
        "查询当前存款产品利率，返回活期与各期限定期存款年化利率。",
        {},
    ),
    _tool(
        "get_loan_rates",
        "查询当前贷款产品参考利率，返回消费贷、经营贷、房贷 LPR 与公积金贷款利率。",
        {},
    ),
    _tool(
        "get_fund_nav",
        "按基金代码查询基金最新单位净值与日涨跌幅。",
        {"fund_code": {"type": "string", "description": "6 位基金代码，例如 000001"}},
    ),
    _tool(
        "get_stock_quote",
        "按股票代码查询 A 股最新行情，返回价格、涨跌幅与币种。",
        {"stock_code": {"type": "string", "description": "6 位股票代码，例如 600519"}},
    ),
    _tool(
        "get_exchange_rate",
        "按币种代码查询人民币汇率中间价，支持 USD、EUR、JPY、HKD、GBP。",
        {"currency": {"type": "string", "description": "币种代码，例如 USD"}},
    ),
]


async def list_tools(context: Any, params: Any) -> types.ListToolsResult:
    return types.ListToolsResult(tools=TOOLS)


async def call_tool(context: Any, params: Any) -> types.CallToolResult:
    name = params.name
    args = params.arguments or {}
    if name == "get_deposit_rates":
        result = {"source": "演示数据", "depositRates": list(DEPOSIT_RATES.values())}
    elif name == "get_loan_rates":
        result = {"source": "演示数据", "loanRates": list(LOAN_RATES.values())}
    elif name == "get_fund_nav":
        code = str(args.get("fund_code", "")).strip()
        item = FUND_NAV.get(code)
        result = (
            {"source": "演示数据", "fund": {"fundCode": code, **item}}
            if item
            else {"source": "演示数据", "error": f"未找到基金代码 {code}"}
        )
    elif name == "get_stock_quote":
        code = str(args.get("stock_code", "")).strip()
        item = STOCK_QUOTES.get(code)
        result = (
            {"source": "演示数据", "stock": {"stockCode": code, **item}}
            if item
            else {"source": "演示数据", "error": f"未找到股票代码 {code}"}
        )
    elif name == "get_exchange_rate":
        currency = str(args.get("currency", "")).strip().upper()
        item = EXCHANGE_RATES.get(currency)
        result = (
            {"source": "演示数据", "exchangeRate": {"code": currency, **item}}
            if item
            else {"source": "演示数据", "error": f"未找到币种 {currency}"}
        )
    else:
        result = {"error": f"未知工具: {name}"}

    return types.CallToolResult(
        content=[types.TextContent(type="text", text=_json_text(result))]
    )


server = Server(
    "market-data",
    on_list_tools=list_tools,
    on_call_tool=call_tool,
)


async def main() -> None:
    async with stdio_server() as (read_stream, write_stream):
        await server.run(
            read_stream,
            write_stream,
            server.create_initialization_options(),
        )


if __name__ == "__main__":
    anyio.run(main)
