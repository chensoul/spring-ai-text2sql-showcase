# Spring AI Text2SQL Showcase

[![GitHub release](https://img.shields.io/github/v/release/chensoul/spring-ai-text2sql-showcase)](https://github.com/chensoul/spring-ai-text2sql-showcase/releases)
[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/chensoul/spring-ai-text2sql-showcase/maven-build.yml?branch=main)](https://github.com/chensoul/spring-ai-text2sql-showcase/actions/workflows/maven-build.yml)
[![License](https://img.shields.io/github/license/chensoul/spring-ai-text2sql-showcase)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/chensoul/spring-ai-text2sql-showcase)](https://github.com/chensoul/spring-ai-text2sql-showcase/stargazers)

一个使用 Spring AI 实现自然语言转 SQL 查询的演示项目，详细说明请参考[《Spring AI 让数据库听懂人话：Text2SQL 自然语言查询实践指南》](https://blog.chensoul.cc/posts/2025/09/30/spring-ai-text2sql-showcase/)。用户可以通过自然语言描述查询需求，AI 会自动生成对应的 SQL 语句并执行查询。

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

- **后端**：Spring Boot 3.5.7, Spring AI 1.1.0
- **数据库**：MySQL 9 (Docker 容器)
- **AI 模型**：DeepSeek Chat API
- **前端**：Bootstrap 5, Thymeleaf
- **构建工具**：Maven
- **容器化**：Docker Compose
- **工具集成**：Spring AI Tools, MCP 工具支持