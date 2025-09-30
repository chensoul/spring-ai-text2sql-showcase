package com.example.text2sql.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SqlUtilsTest {

    @Test
    public void testCleanSql() {
        // 测试清理代码块标记
        String sqlWithMarkdown = "```sql\nSELECT * FROM employees\n```";
        String cleaned = SqlUtils.cleanSql(sqlWithMarkdown);
        assertEquals("SELECT * FROM employees", cleaned);
        
        // 测试普通SQL
        String normalSql = "SELECT * FROM employees WHERE id = 1";
        assertEquals(normalSql, SqlUtils.cleanSql(normalSql));
    }

    @Test
    public void testIsSqlSafe() {
        // 测试安全的SQL
        assertTrue(SqlUtils.isSqlSafe("SELECT * FROM employees"));
        assertTrue(SqlUtils.isSqlSafe("SELECT name, salary FROM employees WHERE department = '技术部'"));
        
        // 测试不安全的SQL
        assertFalse(SqlUtils.isSqlSafe("DROP TABLE employees"));
        assertFalse(SqlUtils.isSqlSafe("DELETE FROM employees"));
        assertFalse(SqlUtils.isSqlSafe("UPDATE employees SET salary = 0"));
        assertFalse(SqlUtils.isSqlSafe("SELECT * FROM information_schema.tables"));
        assertFalse(SqlUtils.isSqlSafe("SELECT * FROM employees UNION SELECT * FROM users"));
        
        // 测试空值和空字符串
        assertFalse(SqlUtils.isSqlSafe(null));
        assertFalse(SqlUtils.isSqlSafe(""));
        assertFalse(SqlUtils.isSqlSafe("   "));
    }

    @Test
    public void testContainsSqlInjection() {
        // 这些测试需要访问私有方法，所以通过isSqlSafe间接测试
        assertFalse(SqlUtils.isSqlSafe("SELECT * FROM employees WHERE name = 'admin'--"));
        assertFalse(SqlUtils.isSqlSafe("SELECT * FROM employees WHERE name = 'admin'; DROP TABLE employees"));
        assertFalse(SqlUtils.isSqlSafe("SELECT * FROM employees WHERE id = 1 OR 1=1"));
        assertFalse(SqlUtils.isSqlSafe("SELECT * FROM employees WHERE id = 1 AND 1=1"));
        assertFalse(SqlUtils.isSqlSafe("SELECT * FROM employees UNION SELECT * FROM users"));
    }

    @Test
    public void testSanitizeSql() {
        // 测试添加LIMIT
        String sqlWithoutLimit = "SELECT * FROM employees";
        String sanitized = SqlUtils.sanitizeSql(sqlWithoutLimit);
        assertTrue(sanitized.contains("LIMIT 1000"));
        
        // 测试已有LIMIT的情况
        String sqlWithLimit = "SELECT * FROM employees LIMIT 10";
        assertEquals(sqlWithLimit, SqlUtils.sanitizeSql(sqlWithLimit));
    }

    @Test
    public void testGetSecurityError() {
        // 测试空值
        assertEquals("SQL语句不能为空", SqlUtils.getSecurityError(null));
        assertEquals("SQL语句不能为空", SqlUtils.getSecurityError(""));
        
        // 测试非SELECT语句
        assertEquals("只允许执行SELECT查询语句", SqlUtils.getSecurityError("DROP TABLE employees"));
        
        // 测试危险操作
        String error = SqlUtils.getSecurityError("SELECT * FROM employees; DROP TABLE employees");
        assertTrue(error.contains("检测到危险操作"));
        
        // 测试系统表访问（INFORMATION_SCHEMA 被检测为危险操作）
        String systemTableError = SqlUtils.getSecurityError("SELECT * FROM information_schema.tables");
        assertTrue(systemTableError.contains("检测到危险操作"));
        
        // 测试其他系统表访问
        String mysqlTableError = SqlUtils.getSecurityError("SELECT * FROM mysql.user");
        assertTrue(mysqlTableError.contains("禁止访问系统表"));
        
        // 测试SQL注入
        String injectionError = SqlUtils.getSecurityError("SELECT * FROM employees WHERE id = 1 OR 1=1");
        assertTrue(injectionError.contains("检测到SQL注入攻击模式"));
        
        // 测试安全的SQL
        assertEquals("SQL语句安全", SqlUtils.getSecurityError("SELECT * FROM employees"));
    }
}
