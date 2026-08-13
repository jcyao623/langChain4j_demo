# Nacos 配置说明

## 1. 环境信息

本机 Docker 中运行 Nacos 2.4.3：

| 项目 | 值 |
| --- | --- |
| Nacos 地址 | `127.0.0.1:8848` |
| 控制台 | `http://127.0.0.1:8848/nacos` |
| 配置 Data ID | `langchain4j-demo.yaml` |
| 配置 Group | `DEFAULT_GROUP` |
| 配置格式 | YAML |

## 2. 配置内容

模板见 [nacos-config/langchain4j-demo.yaml.example](../nacos-config/langchain4j-demo.yaml.example)。

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/langchain4j_demo?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: chat_app
    password: chat_app123
    driver-class-name: com.mysql.cj.jdbc.Driver
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto

openai-compatible:
  aliyun:
      api-key: ${ALIYUN_OPENAI_API_KEY:}
      base-url: https://ws-2wy7rpguu4hmc4lx.cn-beijing.maas.aliyuncs.com/compatible-mode/v1
      model: qwen-plus
      embedding-model: text-embedding-v4
      embedding-dimensions: 1024
      temperature: 0.7
      max-tokens: 1024
    timeout-seconds: 60

pinecone:
  api-key: ${PINECONE_API_KEY:}
  index: ai
  namespace: default
  init-on-startup: false
  embedding-batch-size: 10
  faq-file: faq/finance-faq.txt

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
      initialization-timeout-seconds: 30
      tool-execution-timeout-seconds: 60
      timeout-seconds: 30
  ```

## 3. 发布配置

复制模板并填写真实密钥后执行：

```powershell
Copy-Item nacos-config/langchain4j-demo.yaml.example nacos-config/langchain4j-demo.yaml
.\nacos-config\publish-config.ps1
```

脚本使用 Nacos Open API 将配置发布到配置中心，本目录下的真实 `langchain4j-demo.yaml` 已被 `.gitignore` 排除，避免密钥进入版本库。

## 4. 本地启动配置

本地 `application.yml` 通过 `spring.config.import` 拉取 Nacos 配置：

```yaml
spring:
  config:
    import:
      - optional:nacos:langchain4j-demo.yaml?group=DEFAULT_GROUP&refreshEnabled=true
```

`optional:` 前缀表示 Nacos 不可用时应用仍可启动（此时缺少数据源与模型配置，业务接口不可用）。

## 5. MCP 外部数据配置

`mcp.enabled=true` 时，应用会按 `mcp.servers` 列表逐个创建 MCP 客户端，并向智能客服注册市场数据工具：

| 配置项 | 说明 |
| --- | --- |
| `mcp.enabled` | 是否启用外部 MCP 数据服务 |
| `mcp.servers[].name` | 服务器唯一名称，作为客户端标识 |
| `mcp.servers[].enabled` | 单个服务器是否启用 |
| `mcp.servers[].transport` | 传输方式，支持 `stdio`、`sse`、`http` |
| `mcp.servers[].server-command` | stdio 模式启动命令 |
| `mcp.servers[].url` | `sse` 或 `http` 模式服务地址 |
| `mcp.servers[].environment` | 子进程环境变量，默认固定 UTF-8 输出 |
| `mcp.servers[].initialization-timeout-seconds` | MCP 初始化超时（秒） |
| `mcp.servers[].tool-execution-timeout-seconds` | 工具调用超时（秒） |

如果本机未安装 Python `mcp` 依赖，可将 `mcp.enabled` 改为 `false`，此时 AI 服务使用空 ToolProvider 正常启动。

## 6. 敏感信息建议

- 密钥使用环境变量注入，例如 `${ALIYUN_OPENAI_API_KEY:}`。
- 生产环境应使用 Nacos 的权限控制与加密能力，禁止将密钥提交到 Git。
