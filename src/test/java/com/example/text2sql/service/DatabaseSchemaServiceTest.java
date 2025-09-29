package com.example.text2sql.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * DatabaseSchemaService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class DatabaseSchemaServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DatabaseSchemaService databaseSchemaService;

    @BeforeEach
    void setUp() {
        // 测试前的准备工作
    }

    @Test
    void testGetTableNames_Success() {
        // Given
        List<String> expectedTables = Arrays.asList("departments", "employees", "projects");
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(expectedTables);

        // When
        List<String> actualTables = databaseSchemaService.getTableNames();

        // Then
        assertEquals(expectedTables, actualTables);
        verify(jdbcTemplate).queryForList(anyString(), eq(String.class));
    }

    @Test
    void testGetTableNames_Exception() {
        // Given
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When
        List<String> actualTables = databaseSchemaService.getTableNames();

        // Then
        assertTrue(actualTables.isEmpty());
    }

    @Test
    void testGetTableSchema_Success() {
        // Given
        String tableName = "departments";
        String expectedCreateTable = "CREATE TABLE `departments` (\n" +
                "  `id` bigint NOT NULL AUTO_INCREMENT,\n" +
                "  `name` varchar(50) NOT NULL,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(Arrays.asList(Map.of("Create Table", expectedCreateTable)));

        // When
        String actualSchema = databaseSchemaService.getTableSchema(tableName);

        // Then
        assertNotNull(actualSchema);
        assertTrue(actualSchema.contains("CREATE TABLE"));
        verify(jdbcTemplate).queryForList(anyString());
    }

    @Test
    void testGetTableSchema_ShowCreateTableFails_UsesBackup() {
        // Given
        String tableName = "departments";
        when(jdbcTemplate.queryForList("SHOW CREATE TABLE " + tableName))
                .thenThrow(new RuntimeException("SHOW CREATE TABLE failed"));
        when(jdbcTemplate.queryForList(anyString(), eq(tableName)))
                .thenReturn(Arrays.asList(
                        Map.of("COLUMN_NAME", "id", "COLUMN_TYPE", "bigint", 
                               "IS_NULLABLE", "NO", "COLUMN_KEY", "PRI", "COLUMN_COMMENT", "ID"),
                        Map.of("COLUMN_NAME", "name", "COLUMN_TYPE", "varchar(50)", 
                               "IS_NULLABLE", "NO", "COLUMN_KEY", "", "COLUMN_COMMENT", "名称")
                ));

        // When
        String actualSchema = databaseSchemaService.getTableSchema(tableName);

        // Then
        assertNotNull(actualSchema);
        assertTrue(actualSchema.contains("CREATE TABLE"));
        // 由于SHOW CREATE TABLE失败，会使用备用方案，所以应该包含列信息
        assertTrue(actualSchema.contains("id") || actualSchema.contains("name"));
    }

    @Test
    void testGetAllTablesSchema_Success() {
        // Given
        List<String> tableNames = Arrays.asList("departments", "employees");
        when(jdbcTemplate.queryForList(anyString(), eq(String.class)))
                .thenReturn(tableNames);
        when(jdbcTemplate.queryForList(anyString(), eq("departments")))
                .thenReturn(Arrays.asList(Map.of("TABLE_COMMENT", "部门信息表")));
        when(jdbcTemplate.queryForList(anyString(), eq("employees")))
                .thenReturn(Arrays.asList(Map.of("TABLE_COMMENT", "员工信息表")));
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(Arrays.asList(Map.of("Create Table", "CREATE TABLE departments (...)")))
                .thenReturn(Arrays.asList(Map.of("Create Table", "CREATE TABLE employees (...)")));

        // When
        String allTablesSchema = databaseSchemaService.getAllTablesSchema();

        // Then
        assertNotNull(allTablesSchema);
        assertTrue(allTablesSchema.contains("-- departments 表 (部门信息表)"));
        assertTrue(allTablesSchema.contains("-- employees 表 (员工信息表)"));
        assertTrue(allTablesSchema.contains("CREATE TABLE departments"));
        assertTrue(allTablesSchema.contains("CREATE TABLE employees"));
    }

    @Test
    void testGetTableComment_Success() {
        // Given
        String tableName = "departments";
        String expectedComment = "部门信息表";
        when(jdbcTemplate.queryForList(anyString(), eq(tableName)))
                .thenReturn(Arrays.asList(Map.of("TABLE_COMMENT", expectedComment)));

        // When - 通过反射调用私有方法
        try {
            var method = DatabaseSchemaService.class.getDeclaredMethod("getTableComment", String.class);
            method.setAccessible(true);
            String actualComment = (String) method.invoke(databaseSchemaService, tableName);

            // Then
            assertEquals(expectedComment, actualComment);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    void testGetTableComment_NoComment() {
        // Given
        String tableName = "departments";
        when(jdbcTemplate.queryForList(anyString(), eq(tableName)))
                .thenReturn(Arrays.asList(Map.of("TABLE_COMMENT", "")));

        // When
        try {
            var method = DatabaseSchemaService.class.getDeclaredMethod("getTableComment", String.class);
            method.setAccessible(true);
            String actualComment = (String) method.invoke(databaseSchemaService, tableName);

            // Then
            assertTrue(actualComment == null || actualComment.trim().isEmpty());
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }
}
