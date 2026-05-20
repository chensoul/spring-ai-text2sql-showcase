package io.zhijun.spring.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

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
public class DatabaseTool {
    private final JdbcTemplate jdbcTemplate;

    private static final String SCHEMA_SQL = loadSql("sql/database-schema.sql");
    private static final String TABLE_NAMES_SQL = loadSql("sql/table-names.sql");
    private static final String TABLE_COLUMNS_SQL = loadSql("sql/table-columns.sql");

    private static String loadSql(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8).trim();
            if (sql.isEmpty()) {
                throw new IllegalStateException("SQL file is empty: " + path);
            }
            return sql;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }

    /**
     * 获取所有业务表列表
     */
    @Tool(name = "getTableNames", description = "获取数据库中所有表的名称列表")
    public List<String> getTableNames() {
        try {
            return jdbcTemplate.queryForList(TABLE_NAMES_SQL, String.class);
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
        List<Map<String, Object>> results;
        if (table != null) {
            String sql = "SELECT * FROM (" + SCHEMA_SQL + ") schema_tables WHERE table_name = ?";
            results = jdbcTemplate.queryForList(sql, table);
        } else {
            results = jdbcTemplate.queryForList(SCHEMA_SQL);
        }
        return formatSchema(results);
    }

    private String formatSchema(List<Map<String, Object>> results) {
        StringBuilder schema = new StringBuilder();
        for (Map<String, Object> row : results) {
            String tableName = (String) row.get("table_name");
            String tableComment = (String) row.get("table_comment");
            String columnDefinitions = (String) row.get("column_definitions");
            String primaryKeys = (String) row.get("primary_keys");
            String uniqueKeys = (String) row.get("unique_keys");

            if (tableComment != null && !tableComment.trim().isEmpty()) {
                schema.append("-- ").append(tableComment);
            } else {
                schema.append("-- ").append(tableName).append(" 表");
            }
            schema.append("\n");

            schema.append("CREATE TABLE ").append(tableName).append(" (\n");

            if (columnDefinitions != null) {
                schema.append(columnDefinitions);
            }

            if (primaryKeys != null && !primaryKeys.trim().isEmpty()) {
                schema.append(",\n").append(primaryKeys);
            }

            if (uniqueKeys != null && !uniqueKeys.trim().isEmpty()) {
                schema.append(",\n").append(uniqueKeys);
            }

            schema.append("\n);\n\n");
        }
        return schema.toString();
    }

    @Tool(name = "getTableColumns", description = "获取指定表的所有列信息")
    public List<Map<String, Object>> getTableColumns(@ToolParam(description = "表名") String tableName) {
        return jdbcTemplate.queryForList(TABLE_COLUMNS_SQL, tableName);
    }

    @Tool(name = "executeQuery", description = "执行 SQL 查询并返回结果（仅支持 SELECT 查询）")
    public List<Map<String, Object>> executeQuery(@ToolParam(description = "SQL 查询语句") String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}
