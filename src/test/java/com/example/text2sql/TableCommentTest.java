package com.example.text2sql;

import com.example.text2sql.service.DatabaseSchemaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TableCommentTest {

    @Autowired
    private DatabaseSchemaService databaseSchemaService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testTableComments() {
        System.out.println("=== 测试表名注释功能 ===");
        
        // 测试获取所有表的schema（包含表注释）
        String allTablesSchema = databaseSchemaService.getAllTablesSchema();
        System.out.println("所有表的Schema（包含表注释）:");
        System.out.println(allTablesSchema);
        
        // 验证输出包含表注释
        assertTrue(allTablesSchema.contains("-- departments 表"), "应该包含departments表的注释");
        assertTrue(allTablesSchema.contains("-- employees 表"), "应该包含employees表的注释");
        assertTrue(allTablesSchema.contains("-- projects 表"), "应该包含projects表的注释");
        assertTrue(allTablesSchema.contains("-- project_members 表"), "应该包含project_members表的注释");
        
        System.out.println("✅ 表名注释功能测试通过");
    }
}
