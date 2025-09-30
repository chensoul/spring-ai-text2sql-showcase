package com.example.text2sql.util;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlUtils {

    // 危险操作黑名单
    private static final Set<String> DANGEROUS_KEYWORDS = Set.of(
        "DROP", "DELETE", "UPDATE", "INSERT", "ALTER", "CREATE", 
        "TRUNCATE", "EXEC", "EXECUTE", "CALL", "MERGE", "REPLACE",
        "UNION", "INFORMATION_SCHEMA"
    );
    
    // 系统表黑名单
    private static final Set<String> SYSTEM_TABLES = Set.of(
        "information_schema", "mysql", "performance_schema", 
        "sys", "test", "tmp"
    );

    /**
     * 清理 SQL 语句
     */
    public static String cleanSql(String sql) {
        if (sql == null) return "";

        // 移除可能的代码块标记
        sql = sql.replaceAll("```sql\\s*", "").replaceAll("```\\s*", "");

        // 查找第一个 SELECT 语句
        Pattern pattern = Pattern.compile("(SELECT.*?)(?=\\n\\n|$)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return sql.trim();
    }

    /**
     * 验证 SQL 安全性
     */
    public static boolean isSqlSafe(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }
        
        String upperSql = sql.toUpperCase().trim();
        
        // 1. 检查是否只包含SELECT语句
        if (!upperSql.startsWith("SELECT")) {
            return false;
        }
        
        // 2. 检查危险关键词
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                return false;
            }
        }
        
        // 3. 检查系统表访问
        for (String systemTable : SYSTEM_TABLES) {
            if (upperSql.contains(systemTable)) {
                return false;
            }
        }
        
        // 4. 检查SQL注入模式
        if (containsSqlInjection(sql)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 检测SQL注入攻击
     */
    private static boolean containsSqlInjection(String sql) {
        if (sql == null) return false;
        
        String upperSql = sql.toUpperCase();
        
        // 检测常见的SQL注入模式
        String[] injectionPatterns = {
            "--", "';", "UNION", "OR 1=1", "AND 1=1", 
            "OR '1'='1", "AND '1'='1", "OR 1=1--", "AND 1=1--"
        };
        
        for (String pattern : injectionPatterns) {
            if (upperSql.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 清理和标准化SQL
     */
    public static String sanitizeSql(String sql) {
        if (sql == null) return "";
        
        // 移除代码块标记
        sql = cleanSql(sql);
        
        // 确保有LIMIT限制
        if (!sql.toUpperCase().contains("LIMIT")) {
            sql += " LIMIT 1000";
        }
        
        return sql;
    }
    
    /**
     * 获取SQL安全验证的详细错误信息
     */
    public static String getSecurityError(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "SQL语句不能为空";
        }
        
        String upperSql = sql.toUpperCase().trim();
        
        if (!upperSql.startsWith("SELECT")) {
            return "只允许执行SELECT查询语句";
        }
        
        for (String keyword : DANGEROUS_KEYWORDS) {
            if (upperSql.contains(keyword)) {
                return "检测到危险操作: " + keyword;
            }
        }
        
        for (String systemTable : SYSTEM_TABLES) {
            if (upperSql.contains(systemTable.toUpperCase())) {
                return "禁止访问系统表: " + systemTable;
            }
        }
        
        if (containsSqlInjection(sql)) {
            return "检测到SQL注入攻击模式";
        }
        
        return "SQL语句安全";
    }
}

