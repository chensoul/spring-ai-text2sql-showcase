package com.example.text2sql.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Text2SqlService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class Text2SqlServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private DatabaseSchemaService schemaService;

    @InjectMocks
    private Text2SqlService text2SqlService;

    @BeforeEach
    void setUp() {
        // 测试前的准备工作
    }

    @Test
    void testProcessQuery_EmptyQuery() {
        // Given
        String userQuery = "";

        // When
        Text2SqlService.Text2SqlResult result = text2SqlService.processQuery(userQuery);

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getSql());
        assertNull(result.getData());
        assertEquals("查询内容不能为空", result.getError());
    }

    @Test
    void testProcessQuery_NullQuery() {
        // Given
        String userQuery = null;

        // When
        Text2SqlService.Text2SqlResult result = text2SqlService.processQuery(userQuery);

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getSql());
        assertNull(result.getData());
        assertEquals("查询内容不能为空", result.getError());
    }

    @Test
    void testProcessQuery_Exception() {
        // Given
        String userQuery = "查询员工信息";

        when(schemaService.getAllTablesSchema()).thenThrow(new RuntimeException("Database error"));

        // When
        Text2SqlService.Text2SqlResult result = text2SqlService.processQuery(userQuery);

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getSql());
        assertNull(result.getData());
        assertTrue(result.getError().contains("处理查询时发生错误"));
    }

    @Test
    void testGetDatabaseSchema() {
        // Given
        String expectedSchema = "CREATE TABLE employees (...);";
        when(schemaService.getAllTablesSchema()).thenReturn(expectedSchema);

        // When
        String actualSchema = text2SqlService.getDatabaseSchema();

        // Then
        assertEquals(expectedSchema, actualSchema);
        verify(schemaService).getAllTablesSchema();
    }

    @Test
    void testText2SqlResult_Success() {
        // Given
        String sql = "SELECT * FROM employees";
        List<Map<String, Object>> data = Arrays.asList(Map.of("id", 1L, "name", "张三"));

        // When
        Text2SqlService.Text2SqlResult result = Text2SqlService.Text2SqlResult.success(sql, data);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(sql, result.getSql());
        assertEquals(data, result.getData());
        assertNull(result.getError());
    }

    @Test
    void testText2SqlResult_Error() {
        // Given
        String error = "测试错误";

        // When
        Text2SqlService.Text2SqlResult result = Text2SqlService.Text2SqlResult.error(error);

        // Then
        assertFalse(result.isSuccess());
        assertNull(result.getSql());
        assertNull(result.getData());
        assertEquals(error, result.getError());
    }
}