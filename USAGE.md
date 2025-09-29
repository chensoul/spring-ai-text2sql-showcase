# Spring AI Text2SQL 使用指南

## 项目概述

这是一个使用 Spring AI 框架实现的自然语言转 SQL 查询的演示项目。用户可以通过自然语言描述查询需求，AI 会自动生成对应的 SQL 语句并执行查询。

## 核心功能

### 1. 智能 SQL 生成
- 使用 DeepSeek Chat API 模型
- 基于数据库结构信息生成准确的 SQL
- 支持复杂的多表关联查询

### 2. 安全防护
- 只允许 SELECT 查询操作
- 防止 SQL 注入攻击
- 自动添加查询结果限制

### 3. 用户友好界面
- 现代化的 Web 界面
- 实时查询和结果展示
- 提供示例查询模板

## 快速开始

### 1. 环境准备

确保您的系统已安装：
- Java 17 或更高版本
- Maven 3.6 或更高版本
- Docker & Docker Compose
- 有效的 DeepSeek API Key

### 2. 配置 API Key

```bash
# 设置环境变量
export DEEPSEEK_API_KEY="your-deepseek-api-key-here"
```

### 3. 启动数据库

```bash
# 启动 MySQL 数据库容器
docker-compose up -d

# 等待数据库启动完成（约30秒）
```

### 4. 启动项目

```bash
# 编译并运行
mvn clean spring-boot:run
```

### 5. 访问应用

- 主界面：http://localhost:8080
- MySQL 数据库连接信息：
  - 主机: localhost:3306
  - 数据库: text2sql_db
  - 用户名: root
  - 密码: root123

## 使用示例

### 基础查询示例

1. **查询所有员工**
   - 输入：`查询所有员工信息`
   - 生成 SQL：`SELECT * FROM employees`

2. **条件查询**
   - 输入：`查询技术部的员工`
   - 生成 SQL：`SELECT * FROM employees WHERE department = '技术部'`

3. **排序查询**
   - 输入：`按工资从高到低排序的员工`
   - 生成 SQL：`SELECT * FROM employees ORDER BY salary DESC`

4. **统计查询**
   - 输入：`统计每个部门的员工数量`
   - 生成 SQL：`SELECT department, COUNT(*) as count FROM employees GROUP BY department`

### 复杂查询示例

1. **多表关联**
   - 输入：`查询参与项目最多的员工`
   - 生成 SQL：`SELECT e.name, COUNT(pm.project_id) as project_count FROM employees e JOIN project_members pm ON e.id = pm.employee_id GROUP BY e.id, e.name ORDER BY project_count DESC LIMIT 1`

2. **聚合函数**
   - 输入：`计算所有员工的平均工资`
   - 生成 SQL：`SELECT AVG(salary) as average_salary FROM employees`

3. **日期范围查询**
   - 输入：`查询2022年入职的员工`
   - 生成 SQL：`SELECT * FROM employees WHERE YEAR(hire_date) = 2022`

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

## API 接口

### POST /api/query
处理自然语言查询请求

**请求示例：**
```bash
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d '{"query": "查询所有技术部的员工"}'
```

**响应示例：**
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

## 配置说明

### application.yml 主要配置项

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

### 支持的 AI 模型

项目默认使用 DeepSeek Chat API，也可以配置其他模型：

- OpenAI GPT-3.5-turbo / GPT-4
- Azure OpenAI
- Anthropic Claude
- Ollama 本地模型

## 故障排除

### 1. API Key 问题
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

### 1. 添加新的数据表
1. 创建实体类
2. 更新 `data.sql` 添加示例数据
3. 更新 `DatabaseSchemaService` 获取新表结构

### 2. 数据库配置
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

### 3. 自定义 AI 提示模板
修改 `Text2SqlService` 中的 `SQL_GENERATION_PROMPT` 常量。

## 最佳实践

1. **查询描述要具体**：提供明确的表名、字段名和条件
2. **使用示例查询**：参考界面提供的示例查询模板
3. **检查生成结果**：验证 AI 生成的 SQL 是否符合预期
4. **合理使用限制**：避免查询过大的数据集

## 技术支持

如遇到问题，请：
1. 查看控制台日志输出
2. 检查网络连接和 API 配置
3. 参考项目 README.md 文档
4. 提交 Issue 或联系开发团队
