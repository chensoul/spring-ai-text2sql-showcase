package com.example.text2sql.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 简化的数据库 Schema 服务
 * 只提供核心功能：获取表列表和表 schema
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseTool {
    private final JdbcTemplate jdbcTemplate;

    /**
     * 获取所有业务表列表
     */
    @Tool(name = "getTableNames", description = "获取数据库中所有表的名称列表")
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
    @Tool(name = "getTableSchema", description = "获取指定表的完整结构信息，包括列定义、主键、唯一键等")
    public String getTableSchema(@ToolParam(description = "表名") String tableName) {
        return getDatabaseSchema(tableName);
    }

    @Tool(name = "getDatabaseSchema", description = "获取数据库中所有表的结构信息")
    public String getDatabaseSchema() {
        return getDatabaseSchema(null);
    }

    private String getDatabaseSchema(String table) {
        // 一次查询获取所有表结构
        String sql = """
                SELECT 
                    t.TABLE_NAME,
                    t.TABLE_COMMENT,
                    t.ENGINE,
                    t.TABLE_COLLATION,
                    GROUP_CONCAT(
                        CONCAT(
                            '  `', c.COLUMN_NAME, '` ', c.COLUMN_TYPE,
                            CASE WHEN c.IS_NULLABLE = 'NO' THEN ' NOT NULL' ELSE '' END,
                            CASE WHEN c.COLUMN_DEFAULT IS NOT NULL THEN CONCAT(' DEFAULT ', c.COLUMN_DEFAULT) ELSE '' END,
                            CASE WHEN c.COLUMN_COMMENT != '' THEN CONCAT(' COMMENT ''', c.COLUMN_COMMENT, '''') ELSE '' END
                        ) ORDER BY c.ORDINAL_POSITION SEPARATOR ',\n'
                    ) AS COLUMN_DEFINITIONS,
                    GROUP_CONCAT(
                        CASE WHEN c.COLUMN_KEY = 'PRI' THEN CONCAT('  PRIMARY KEY (`', c.COLUMN_NAME, '`)') ELSE NULL END
                        ORDER BY c.ORDINAL_POSITION SEPARATOR ',\n'
                    ) AS PRIMARY_KEYS,
                    GROUP_CONCAT(
                        CASE WHEN c.COLUMN_KEY = 'UNI' THEN CONCAT('  UNIQUE KEY `', c.COLUMN_NAME, '` (`', c.COLUMN_NAME, '`)') ELSE NULL END
                        ORDER BY c.ORDINAL_POSITION SEPARATOR ',\n'
                    ) AS UNIQUE_KEYS
                FROM INFORMATION_SCHEMA.TABLES t
                LEFT JOIN INFORMATION_SCHEMA.COLUMNS c ON t.TABLE_NAME = c.TABLE_NAME AND t.TABLE_SCHEMA = c.TABLE_SCHEMA
                WHERE t.TABLE_SCHEMA = DATABASE() and 1=1
                GROUP BY t.TABLE_NAME, t.TABLE_COMMENT, t.ENGINE, t.TABLE_COLLATION
                ORDER BY t.TABLE_NAME
                """;

        if (table != null) {
            sql = sql.replace("1=1", "t.TABLE_NAME='" + table + "'");
        }
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

        StringBuilder schema = new StringBuilder();
        for (Map<String, Object> row : results) {
            String tableName = (String) row.get("TABLE_NAME");
            String tableComment = (String) row.get("TABLE_COMMENT");
            String engine = (String) row.get("ENGINE");
            String collation = (String) row.get("TABLE_COLLATION");
            String columnDefinitions = (String) row.get("COLUMN_DEFINITIONS");
            String primaryKeys = (String) row.get("PRIMARY_KEYS");
            String uniqueKeys = (String) row.get("UNIQUE_KEYS");

            // 表注释
            if (tableComment != null && !tableComment.trim().isEmpty()) {
                schema.append("-- ").append(tableComment);
            } else {
                schema.append("-- ").append(tableName).append(" 表");
            }
            schema.append("\n");

            // CREATE TABLE 语句
            schema.append("CREATE TABLE `").append(tableName).append("` (\n");

            // 列定义
            if (columnDefinitions != null) {
                schema.append(columnDefinitions);
            }

            // 主键约束
            if (primaryKeys != null && !primaryKeys.trim().isEmpty()) {
                schema.append(",\n").append(primaryKeys);
            }

            // 唯一键约束
            if (uniqueKeys != null && !uniqueKeys.trim().isEmpty()) {
                schema.append(",\n").append(uniqueKeys);
            }

            schema.append("\n)");

            // 表注释
            if (tableComment != null && !tableComment.trim().isEmpty()) {
                schema.append(" COMMENT='").append(tableComment).append("'");
            }

            // 表选项
            if (engine != null) {
                schema.append(" ENGINE=").append(engine);
            }
            if (collation != null) {
                schema.append(" DEFAULT CHARSET=").append(collation.split("_")[0]);
                schema.append(" COLLATE=").append(collation);
            }

            schema.append(";\n\n");
        }
        return schema.toString();
    }

    @Tool(name = "getTableColumns", description = "获取指定表的所有列信息")
    public List<Map<String, Object>> getTableColumns(@ToolParam(description = "表名") String tableName) {
        String sql = """
                SELECT 
                    COLUMN_NAME,
                    COLUMN_TYPE,
                    IS_NULLABLE,
                    COLUMN_DEFAULT,
                    COLUMN_COMMENT,
                    COLUMN_KEY,
                    EXTRA
                FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?
                ORDER BY ORDINAL_POSITION
                """;
        return jdbcTemplate.queryForList(sql, tableName);
    }

    @Tool(name = "executeQuery", description = "执行 SQL 查询并返回结果（仅支持 SELECT 查询）")
    public List<Map<String, Object>> executeQuery(@ToolParam(description = "SQL 查询语句") String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}