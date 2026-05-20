# Spring AI Text2SQL Showcase

[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/chensoul/spring-ai-text2sql-showcase/maven-build.yml?branch=main)](https://github.com/zhijunio/spring-ai-text2sql-showcase/actions/workflows/maven-build.yml)
[![License](https://img.shields.io/github/license/zhijunio/spring-ai-text2sql-showcase)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/zhijunio/spring-ai-text2sql-showcase)](https://github.com/zhijunio/spring-ai-text2sql-showcase/stargazers)

一个使用 Spring AI 实现自然语言转 SQL 查询的演示项目，详细说明请参考[《Spring AI 让数据库听懂人话：Text2SQL 自然语言查询实践指南》](https://blog.chensoul.cc/posts/spring-ai-text2sql-showcase/)。用户可以通过自然语言描述查询需求，AI 会自动生成对应的 SQL 语句并执行查询。

## 功能特性

- 🤖 **智能 SQL 生成**：使用 Spring AI 和 DeepSeek Chat API 将自然语言转换为 SQL 查询
- 🛡️ **安全防护**：内置 SQL 注入防护，只允许 SELECT 查询
- 🎨 **美观界面**：现代化的 Web 界面，支持实时查询和结果展示
- 📊 **数据可视化**：查询结果以表格形式展示，支持分页和滚动
- 🔍 **数据库结构**：自动获取并展示数据库结构信息
- 📝 **示例查询**：提供常用查询示例，帮助用户快速上手
- 🛠️ **工具集成**：支持 MCP 工具查询数据库结构，提高查询准确性

## Prerequisites

- JDK 25+
- Docker and Docker Compose

## Technology Stack

- Java
- Spring Boot
- Spring AI
- PostgreSQL 18
- Maven
- Bootstrap 5
- Thymeleaf

## How to run?

```bash
# Full build with tests (integration tests use Testcontainers + PostgreSQL)
$ ./mvnw clean verify

# Run application(using Docker Compose)
$ ./mvnw spring-boot:run

# Run application(using Testcontainers)
$ ./mvnw spring-boot:test-run

# Code formatting
$ ./mvnw spotless:apply

# Docker image build
$ ./mvnw spring-boot:build-image -DskipTests
```