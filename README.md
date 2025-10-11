# Spring AI Text2SQL Showcase

[![CI/CD Pipeline](https://github.com/chensoul/spring-ai-text2sql-showcase/actions/workflows/ci.yml/badge.svg)](https://github.com/chensoul/spring-ai-text2sql-showcase/actions/workflows/ci.yml)
[![SonarCloud](https://sonarcloud.io/api/project_badges/measure?project=spring-ai-text2sql-showcase&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=spring-ai-text2sql-showcase)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=spring-ai-text2sql-showcase&metric=coverage)](https://sonarcloud.io/summary/new_code?id=spring-ai-text2sql-showcase)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=spring-ai-text2sql-showcase&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=spring-ai-text2sql-showcase)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=spring-ai-text2sql-showcase&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=spring-ai-text2sql-showcase)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=spring-ai-text2sql-showcase&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=spring-ai-text2sql-showcase)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=spring-ai-text2sql-showcase&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=spring-ai-text2sql-showcase)

一个使用 Spring AI 实现自然语言转 SQL 查询的演示项目。用户可以通过自然语言描述查询需求，AI 会自动生成对应的 SQL 语句并执行查询。

![Spring AI Text2SQL 直接模式界面展示 - 自然语言数据库查询系统前端界面，包含中文查询输入框、示例查询模板、SQL生成结果展示和数据库结构查看功能](https://csmos.oss-cn-beijing.aliyuncs.com/spring-ai-text2sql-showcase-01.png)

## 功能特性

- 🤖 **智能 SQL 生成**：使用 Spring AI 和 DeepSeek Chat API 将自然语言转换为 SQL 查询
- 🛡️ **安全防护**：内置 SQL 注入防护，只允许 SELECT 查询
- 🎨 **美观界面**：现代化的 Web 界面，支持实时查询和结果展示
- 📊 **数据可视化**：查询结果以表格形式展示，支持分页和滚动
- 🔍 **数据库结构**：自动获取并展示数据库结构信息
- 📝 **示例查询**：提供常用查询示例，帮助用户快速上手
- 🛠️ **工具集成**：支持 MCP 工具查询数据库结构，提高查询准确性

## 技术栈

- **后端**：Spring Boot 3.5.6, Spring AI 1.1.0-M2
- **数据库**：MySQL 9 (Docker 容器)
- **AI 模型**：DeepSeek Chat API
- **前端**：Bootstrap 5, Thymeleaf
- **构建工具**：Maven
- **容器化**：Docker Compose
- **工具集成**：Spring AI Tools, MCP 工具支持

## 项目架构

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring AI Text2SQL 架构                    │
├─────────────────────────────────────────────────────────────┤
│  前端层 (Thymeleaf + Bootstrap)                              │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Web 界面 (index.html)                                  │ │
│  │  - 自然语言输入框                                        │ │
│  │  - 示例查询模板                                          │ │
│  │  - 结果展示区域                                          │ │
│  └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  REST API 层 (Spring MVC)                                   │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Text2SqlController                                     │ │
│  │  - POST /api/query (处理查询请求)                        │ │
│  │  - GET /api/schema (获取数据库结构)                      │ │
│  │  - GET /api/health (健康检查)                           │ │
│  └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  业务逻辑层 (Spring Services)                               │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  Text2SqlService                                        │ │
│  │  - 自然语言转 SQL 核心逻辑                               │ │
│  │  - SQL 安全验证                                          │ │
│  │  - 查询结果封装                                          │ │
│  │                                                         │ │
│  │  DatabaseSchemaService                                  │ │
│  │  - 数据库结构信息获取                                    │ │
│  │  - 示例数据提供                                          │ │
│  └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  AI 集成层 (Spring AI)                                      │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  ChatClient (DeepSeek Chat API)                         │ │
│  │  - 自然语言理解                                          │ │
│  │  - SQL 生成                                              │ │
│  │  - 提示工程优化                                          │ │
│  └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  数据访问层 (Spring Data JPA + JDBC)                        │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  JdbcTemplate                                           │ │
│  │  - SQL 执行                                              │ │
│  │  - 结果集处理                                            │ │
│  │                                                         │ │
│  │  Entity Models                                          │ │
│  │  - Employee (员工)                                       │ │
│  │  - Project (项目)                                        │ │
│  │  - Department (部门)                                     │ │
│  │  - ProjectMember (项目成员)                              │ │
│  └─────────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  数据存储层 (MySQL 9 + Docker)                             │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │  MySQL 9 数据库容器                                      │ │
│  │  - Docker Compose 管理                                   │ │
│  │  - UTF-8 字符编码支持                                    │ │
│  │  - 自动初始化脚本                                        │
│  │  - 示例数据存储                                          │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 快速开始

### 1. 环境要求

- Java 25+
- Maven 3.6+
- Docker & Docker Compose
- DeepSeek API Key

### 2. 配置 API Key

```bash
export DEEPSEEK_API_KEY="your-deepseek-api-key-here"
```

或者在 `application.yml` 中直接配置：

```yaml
spring:
  ai:
    openai:
      api-key: your-deepseek-api-key-here
```

### 3. 启动数据库

```bash
# 启动 MySQL 数据库容器
docker-compose up -d

# 等待数据库启动完成（约30秒）
```

### 4. 运行项目

```bash
# 克隆项目
git clone <repository-url>
cd spring-ai-text2sql-showcase

# 编译并运行
mvn clean spring-boot:run
```

### 5. 访问应用

打开浏览器访问：
- **主页面**：http://localhost:8080 - 直接模式查询
- **分步骤页面**：http://localhost:8080/steps - 分步骤模式查询

## 使用示例

### 自然语言查询示例

1. **查询员工信息**
   - 输入：`查询所有技术部的员工信息`
   - 生成 SQL：`SELECT * FROM employees WHERE department = '技术部'`

2. **统计查询**
   - 输入：`统计每个部门的员工数量`
   - 生成 SQL：`SELECT department, COUNT(*) as count FROM employees GROUP BY department`

3. **排序查询**
   - 输入：`找出工资最高的前5名员工`
   - 生成 SQL：`SELECT * FROM employees ORDER BY salary DESC LIMIT 5`

4. **多表关联查询**
   - 输入：`查询参与项目最多的员工`
   - 生成 SQL：`SELECT e.name, COUNT(pm.project_id) as project_count FROM employees e JOIN project_members pm ON e.id = pm.employee_id GROUP BY e.id, e.name ORDER BY project_count DESC LIMIT 1`

## 数据库结构

项目包含以下示例表：

### employees 表（员工表）
- `id`: 主键
- `name`: 员工姓名
- `department`: 部门
- `position`: 职位
- `salary`: 工资
- `hire_date`: 入职日期
- `email`: 邮箱

### projects 表（项目表）
- `id`: 主键
- `name`: 项目名称
- `description`: 项目描述
- `start_date`: 开始日期
- `end_date`: 结束日期
- `status`: 项目状态
- `budget`: 项目预算

### departments 表（部门表）
- `id`: 主键
- `name`: 部门名称
- `manager_id`: 部门经理ID
- `budget`: 部门预算
- `location`: 办公地点

### project_members 表（项目成员关系表）
- `id`: 主键
- `project_id`: 项目ID
- `employee_id`: 员工ID
- `role`: 在项目中的角色
- `join_date`: 加入项目日期

## 项目结构

```
src/main/java/com/example/text2sql/
├── Text2SqlApplication.java          # 主应用类
├── config/
│   └── AppConfig.java               # 应用配置
├── controller/
│   ├── Text2SqlController.java      # REST API 控制器
│   └── StepBasedText2SqlController.java # 分步骤查询控制器
├── model/                           # 数据模型
│   ├── Employee.java
│   ├── Project.java
│   ├── Department.java
│   └── ProjectMember.java
├── service/
│   ├── Text2SqlService.java         # 核心 Text2SQL 服务接口
│   ├── DirectText2SqlService.java   # 直接 Text2SQL 服务实现
│   ├── McpText2SqlService.java      # MCP 工具集成服务
│   ├── StepBasedText2SqlService.java # 分步骤查询服务
│   ├── DatabaseTool.java            # 数据库工具类
│   ├── Text2SqlResult.java          # 查询结果封装
│   └── Text2SqlStepResult.java      # 分步骤查询结果
└── util/
    └── SqlUtils.java                # SQL 工具类
```

## API 接口

### POST /api/query
处理自然语言查询请求（直接模式）

**请求体：**
```json
{
  "query": "查询所有技术部的员工信息"
}
```

**响应：**
```json
{
  "success": true,
  "sql": "SELECT * FROM employees WHERE department = '技术部'",
  "data": [
    {
      "id": 1,
      "name": "张三",
      "department": "技术部",
      "position": "高级工程师",
      "salary": 15000.0,
      "hire_date": "2022-01-15",
      "email": "zhangsan@company.com"
    }
  ],
  "count": 1
}
```

### POST /api/steps/query
处理分步骤自然语言查询请求

**请求体：**
```json
{
  "query": "查询所有技术部的员工信息"
}
```

**响应：**
```json
{
  "success": true,
  "step1ProblemRewriting": {
    "completed": true,
    "status": "success",
    "content": "问题改写，改写为：查询技术部门的所有员工信息"
  },
  "step2TableSelection": {
    "completed": true,
    "status": "success",
    "content": "数据表选取，选择表为：employees"
  },
  "step3InformationInference": {
    "completed": true,
    "status": "success",
    "content": "信息推理，本次推理参考业务信息是：..."
  },
  "step4SqlGeneration": {
    "completed": true,
    "status": "success",
    "content": "SQL生成，生成的SQL语句是：..."
  },
  "step5SqlExecution": {
    "completed": true,
    "status": "success",
    "content": "执行SQL，执行完成，返回了X条记录"
  }
}
```

### GET /api/schema
获取数据库结构信息

### GET /api/health
健康检查接口

## 服务实现

项目提供了多种 Text2SQL 服务实现，满足不同的使用场景：

### 1. DirectText2SqlService（直接模式）
- **特点**：一步到位，直接将自然语言转换为 SQL 并执行
- **适用场景**：简单查询，快速原型开发
- **API 端点**：`POST /api/query`

### 2. McpText2SqlService（MCP 工具模式）
- **特点**：集成 MCP 工具，AI 可以主动查询数据库结构
- **适用场景**：复杂查询，需要动态了解数据库结构
- **优势**：提高查询准确性，减少 AI 调用次数

### 3. StepBasedText2SqlService（分步骤模式）
- **特点**：将查询过程分解为 5 个步骤，每步都有详细说明
- **适用场景**：教学演示，复杂查询调试
- **API 端点**：`POST /api/steps/query`
- **步骤说明**：
  1. **问题改写**：将用户查询改写为更清晰的描述
  2. **数据表选取**：选择相关的数据表
  3. **信息推理**：分析业务逻辑和查询需求
  4. **SQL 生成**：生成对应的 SQL 语句
  5. **SQL 执行**：执行查询并返回结果

## 安全特性

1. **SQL 注入防护**：只允许 SELECT 查询，禁止 DROP、DELETE、UPDATE 等危险操作
2. **查询限制**：自动添加 LIMIT 限制，防止返回过多数据
3. **输入验证**：对用户输入进行验证和清理

## 配置说明

### application.yml 主要配置

```yaml
spring:
  # 数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/text2sql_db?connectTimeout=60000&socketTimeout=60000&allowMultiQueries=true&useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: root123

  # JPA 配置
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQLDialect

  # Spring AI 配置
  ai:
    openai:
      base-url: https://api.deepseek.com
      api-key: ${DEEPSEEK_API_KEY}
      chat:
        options:
          model: deepseek-chat
          temperature: 0.1

# 日志配置
logging:
  level:
    org.springframework.ai: ERROR
    com.example: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### MCP 工具配置

项目支持 MCP（Model Context Protocol）工具，允许 AI 主动查询数据库结构：

```java
@Bean("mcpChatClient")
public ChatClient mcpChatClient(ChatClient.Builder chatClientBuilder, DatabaseTool databaseTool) {
    return chatClientBuilder
            .defaultTools(databaseTool)
            .build();
}
```

**可用的 MCP 工具：**
- `getTableNames`：获取所有表名
- `getTableSchema`：获取指定表的结构信息

## 故障排除

### 1. DeepSeek API Key 问题
```
错误：401 Unauthorized
解决：检查 DEEPSEEK_API_KEY 是否正确设置
```

### 2. 数据库连接问题
```
错误：Cannot connect to database
解决：确保 MySQL 容器正在运行
docker-compose up -d
docker logs text2sql-mysql
```

### 3. 中文字符乱码问题
```
错误：数据库中文显示乱码
解决：确保 mysql.cnf 文件正确配置
[mysql]
default-character-set=utf8mb4
```

### 4. AI 模型响应问题
```
错误：AI model not responding
解决：检查网络连接和 API 配额
```

### 5. SQL 生成问题
```
错误：Generated SQL is invalid
解决：检查查询描述是否清晰，尝试使用更具体的描述
```

## 扩展功能

### 1. 数据库配置
项目使用 MySQL 9 数据库，通过 Docker Compose 管理：

```yaml
# docker-compose.yml
services:
  mysql:
    image: mysql:8
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: text2sql_db
    volumes:
      - ./mysql.cnf:/etc/mysql/conf.d/mysql.cnf
      - ./src/main/resources/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
      - ./src/main/resources/data.sql:/docker-entrypoint-initdb.d/02-data.sql
```

**重要**：`mysql.cnf` 文件确保中文字符正确显示：
```ini
[mysql]
default-character-set=utf8mb4
```

### 2. 切换服务模式
项目支持多种服务模式，可以根据需要选择：

```java
// 在 AppConfig.java 中配置不同的服务实现
@Bean
public Text2SqlService text2SqlService() {
    // 选择服务实现
    return new DirectText2SqlService(chatClient, databaseTool);
    // 或者
    // return new McpText2SqlService(mcpChatClient, databaseTool);
    // 或者
    // return new StepBasedText2SqlService(mcpChatClient, databaseTool);
}
```

### 3. 添加更多 AI 模型
支持多种 AI 模型，修改 `application.yml` 配置：

```yaml
# DeepSeek API
spring:
  ai:
    openai:
      base-url: https://api.deepseek.com
      api-key: ${DEEPSEEK_API_KEY}
      chat:
        options:
          model: deepseek-chat

# OpenAI API
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-3.5-turbo
```

### 4. 自定义 MCP 工具
可以添加更多 MCP 工具来扩展 AI 的能力：

```java
@Tool(name = "customTool", description = "自定义工具描述")
public String customTool(@ToolParam(description = "参数描述") String param) {
    // 实现自定义逻辑
    return "结果";
}
```

## 项目亮点

1. **完整的端到端实现**：从自然语言输入到 SQL 结果展示
2. **多种服务模式**：支持直接模式、MCP 工具模式、分步骤模式
3. **安全可靠**：内置多层安全防护机制
4. **用户友好**：现代化界面和丰富的示例
5. **技术先进**：使用最新的 Spring AI 框架和 MCP 工具
6. **易于扩展**：模块化设计，便于功能扩展
7. **教学友好**：分步骤模式便于学习和调试

## 适用场景

1. **数据分析师**：快速生成复杂查询
2. **业务用户**：通过自然语言获取数据洞察
3. **开发人员**：快速原型和测试查询
4. **数据探索**：发现数据中的模式和关系
5. **教学演示**：展示 AI 在数据库查询中的应用

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

---

**注意**：本项目仅用于演示和学习目的，在生产环境中使用前请确保进行充分的安全测试和配置。
