# Spring AI Text2SQL Showcase

一个使用 Spring AI 实现自然语言转 SQL 查询的演示项目。用户可以通过自然语言描述查询需求，AI 会自动生成对应的 SQL 语句并执行查询。

## 功能特性

- 🤖 **智能 SQL 生成**：使用 Spring AI 和 OpenAI GPT 模型将自然语言转换为 SQL 查询
- 🛡️ **安全防护**：内置 SQL 注入防护，只允许 SELECT 查询
- 🎨 **美观界面**：现代化的 Web 界面，支持实时查询和结果展示
- 📊 **数据可视化**：查询结果以表格形式展示，支持分页和滚动
- 🔍 **数据库结构**：自动获取并展示数据库结构信息
- 📝 **示例查询**：提供常用查询示例，帮助用户快速上手

## 技术栈

- **后端**：Spring Boot 3.5.6, Spring AI 1.0.0-M4
- **数据库**：MySQL 8 (Docker 容器)
- **AI 模型**：DeepSeek Chat API
- **前端**：Bootstrap 5, Thymeleaf
- **构建工具**：Maven
- **容器化**：Docker Compose

## 快速开始

### 1. 环境要求

- Java 17+
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

打开浏览器访问：http://localhost:8080

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

## 项目结构

```
src/main/java/com/example/text2sql/
├── Text2SqlApplication.java          # 主应用类
├── config/
│   └── AppConfig.java               # 应用配置
├── controller/
│   └── Text2SqlController.java      # REST API 控制器
├── model/                           # 数据模型
│   ├── Employee.java
│   ├── Project.java
│   ├── Department.java
│   └── ProjectMember.java
└── service/
    ├── Text2SqlService.java         # 核心 Text2SQL 服务
    └── DatabaseSchemaService.java   # 数据库结构服务

src/main/resources/
├── application.yml                  # 应用配置
├── schema.sql                      # 数据库结构
├── data.sql                        # 示例数据
└── templates/
    └── index.html                  # 前端页面

# Docker 配置
├── docker-compose.yml              # Docker Compose 配置
└── mysql.cnf                       # MySQL 字符编码配置
```

## API 接口

### POST /api/query
处理自然语言查询请求

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

### GET /api/schema
获取数据库结构信息

### GET /api/health
健康检查接口

## 安全特性

1. **SQL 注入防护**：只允许 SELECT 查询，禁止 DROP、DELETE、UPDATE 等危险操作
2. **查询限制**：自动添加 LIMIT 限制，防止返回过多数据
3. **输入验证**：对用户输入进行验证和清理

## 配置说明

### application.yml 主要配置

```yaml
spring:
  ai:
    openai:
      base-url: https://api.deepseek.com
      api-key: ${DEEPSEEK_API_KEY}
      chat:
        options:
          model: deepseek-chat
```

## 扩展功能

### 1. 数据库配置
项目使用 MySQL 8 数据库，通过 Docker Compose 管理：

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

### 2. 添加更多 AI 模型
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

### 3. 查询历史记录
可以添加查询历史记录功能，保存用户的查询历史和结果。

## 故障排除

### 1. DeepSeek API Key 问题
确保设置了正确的 API Key：
```bash
export DEEPSEEK_API_KEY="your-actual-api-key"
```

### 2. 数据库连接问题
确保 MySQL 容器正在运行：
```bash
# 检查容器状态
docker ps

# 启动数据库
docker-compose up -d

# 查看数据库日志
docker logs text2sql-mysql
```

### 3. 中文字符乱码问题
确保 `mysql.cnf` 文件正确配置：
```ini
[mysql]
default-character-set=utf8mb4
```

### 4. AI 模型响应问题
检查网络连接和 API 配额是否充足。

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

如有问题或建议，请通过以下方式联系：

- 创建 Issue
- 发送邮件
- 提交 Pull Request

---

**注意**：本项目仅用于演示和学习目的，在生产环境中使用前请确保进行充分的安全测试和配置。
