# langchain4j-demo

互联网金融智能客服系统，基于 Spring Boot 3.2、LangChain4j、Nacos 配置中心与 MySQL 构建。

## 技术栈

- Java 17 + Maven 多模块工程
- Spring Boot 3.2 + Spring Cloud Alibaba Nacos
- LangChain4j + 阿里云百炼 OpenAI 兼容模式
- Spring Data JPA + MySQL 8
- JUnit 5 + Mockito + H2（测试）

## 模块结构

```text
langchain4j-demo
├── common   公共模块：统一返回、异常、枚举、工具类
├── service  服务模块：对话业务、JPA 实体、数据访问
├── biz      接入模块：启动类、Web 接口、模型与 Nacos 配置
├── docs     项目文档
├── nacos-config  Nacos 配置模板与发布脚本
└── sql      数据库初始化脚本
```

## 快速开始

1. 启动本机 Docker 基础设施：

   ```powershell
   docker start mysql8 nacos-server
   ```

2. 初始化数据库（root 密码为 `root123`，可自行修改）：

   ```powershell
   Get-Content sql/init.sql | docker exec -i mysql8 mysql -uroot -proot123
   ```

3. 发布 Nacos 配置：

   ```powershell
   Copy-Item nacos-config/langchain4j-demo.yaml.example nacos-config/langchain4j-demo.yaml
   # 编辑 nacos-config/langchain4j-demo.yaml，填入 ALIYUN_OPENAI_API_KEY
   .\nacos-config\publish-config.ps1
   ```

4. 构建并启动：

   ```powershell
   mvn -pl biz -am clean package
   java -jar biz/target/langchain4j-demo-biz-1.0.0-SNAPSHOT.jar
   ```

5. 调用接口：

   ```powershell
   Invoke-RestMethod -Method Post -Uri http://127.0.0.1:8080/api/v1/chat -ContentType 'application/json' -Body '{"message":"你好","sessionId":"demo-001"}'
   ```

## 文档

- [项目概述](docs/01-项目概述.md)
- [技术架构](docs/02-技术架构.md)
- [模块设计](docs/03-模块设计.md)
- [数据库设计](docs/04-数据库设计.md)
- [接口设计](docs/05-接口设计.md)
- [Nacos 配置说明](docs/06-Nacos配置说明.md)
- [开发规范](docs/07-开发规范.md)
- [测试规范](docs/08-测试规范.md)
- [部署与启动](docs/09-部署与启动.md)
