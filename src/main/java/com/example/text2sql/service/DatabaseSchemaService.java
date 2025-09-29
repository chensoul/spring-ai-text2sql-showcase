package com.example.text2sql.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 简化的数据库 Schema 服务
 * 只提供核心功能：获取表列表和表 schema
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseSchemaService {
    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取所有业务表列表
     */
    public List<String> getTableNames() {
        try {
            String sql = """
                    SELECT TABLE_NAME 
                    FROM INFORMATION_SCHEMA.TABLES 
                    WHERE TABLE_SCHEMA = DATABASE()
                    AND TABLE_TYPE = 'BASE TABLE'
                    ORDER BY TABLE_NAME
                    """;

            return jdbcTemplate.queryForList(sql, String.class);
        } catch (Exception e) {
            log.error("获取表列表失败", e);
            return List.of();
        }
    }

    /**
     * 获取指定表的 schema 信息
     */
    public String getTableSchema(String tableName) {
        try {
            // 优先使用 SHOW CREATE TABLE，性能更好
            String createTableSql = getCreateTableSql(tableName);
            if (createTableSql != null && !createTableSql.trim().isEmpty()) {
                return createTableSql;
            }

            // 备用方案：使用 INFORMATION_SCHEMA
            return getTableColumnsInfo(tableName);
        } catch (Exception e) {
            log.error("获取表 {} schema 失败", tableName, e);
            return "无法获取表结构信息";
        }
    }

    /**
     * 使用 SHOW CREATE TABLE 获取表结构（性能更好）
     */
    private String getCreateTableSql(String tableName) {
        try {
            String sql = "SHOW CREATE TABLE " + tableName;
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

            if (!result.isEmpty()) {
                return (String) result.get(0).get("Create Table");
            }
        } catch (Exception e) {
            log.warn("SHOW CREATE TABLE 失败，使用备用方案: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取所有表的 schema 信息
     */
    public String getAllTablesSchema() {
        StringBuilder schema = new StringBuilder();

        List<String> tableNames = getTableNames();
        for (String tableName : tableNames) {
            // 获取表注释
            String tableComment = getTableComment(tableName);
            if (tableComment != null && !tableComment.trim().isEmpty()) {
                schema.append("-- ").append(tableComment).append("\n");
            } else {
                schema.append("-- ").append(tableName).append(" 表\n");
            }
            schema.append(getTableSchema(tableName)).append(";\n\n");
        }

        return schema.toString();
    }

    /**
     * 获取表注释
     */
    private String getTableComment(String tableName) {
        try {
            String sql = """
                    SELECT TABLE_COMMENT 
                    FROM INFORMATION_SCHEMA.TABLES 
                    WHERE TABLE_SCHEMA = DATABASE() 
                    AND TABLE_NAME = ?
                    """;

            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, tableName);
            if (!result.isEmpty()) {
                String comment = (String) result.get(0).get("TABLE_COMMENT");
                return comment;
            }
        } catch (Exception e) {
            log.warn("获取表 {} 注释失败: {}", tableName, e.getMessage());
        }
        return null;
    }

    /**
     * 获取表列信息（备用方案）
     */
    private String getTableColumnsInfo(String tableName) {
        try {
            List<Map<String, Object>> columns = getTableColumns(tableName);
            return buildCreateTableStatement(tableName, columns);
        } catch (Exception e) {
            log.warn("获取表 {} 列信息失败: {}", tableName, e.getMessage());
            return "无法获取表结构";
        }
    }

    /**
     * 查询表列信息
     */
    private List<Map<String, Object>> getTableColumns(String tableName) {
        String sql = """
                SELECT 
                    COLUMN_NAME,
                    COLUMN_TYPE,
                    IS_NULLABLE,
                    COLUMN_KEY,
                    COLUMN_COMMENT
                FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_SCHEMA = DATABASE() 
                AND TABLE_NAME = ?
                ORDER BY ORDINAL_POSITION
                """;
        return jdbcTemplate.queryForList(sql, tableName);
    }

    /**
     * 构建CREATE TABLE语句
     */
    private String buildCreateTableStatement(String tableName, List<Map<String, Object>> columns) {
        StringBuilder info = new StringBuilder();
        info.append("CREATE TABLE ").append(tableName).append(" (\n");

        for (int i = 0; i < columns.size(); i++) {
            Map<String, Object> column = columns.get(i);
            String columnDefinition = buildColumnDefinition(column);
            info.append("  ").append(columnDefinition);

            if (i < columns.size() - 1) {
                info.append(",");
            }
            info.append("\n");
        }

        info.append(")");
        return info.toString();
    }

    /**
     * 构建单个列定义
     */
    private String buildColumnDefinition(Map<String, Object> column) {
        String columnName = (String) column.get("COLUMN_NAME");
        String columnType = (String) column.get("COLUMN_TYPE");
        String isNullable = (String) column.get("IS_NULLABLE");
        String columnKey = (String) column.get("COLUMN_KEY");
        String comment = (String) column.get("COLUMN_COMMENT");

        StringBuilder definition = new StringBuilder();
        definition.append(columnName).append(" ").append(columnType);

        if ("NO".equals(isNullable)) {
            definition.append(" NOT NULL");
        }

        if ("PRI".equals(columnKey)) {
            definition.append(" PRIMARY KEY");
        }

        if (comment != null && !comment.trim().isEmpty()) {
            definition.append(" COMMENT '").append(comment).append("'");
        }

        return definition.toString();
    }
}