package com.example.text2sql;

import com.example.text2sql.service.DatabaseTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TableCommentTest {

    @Autowired
    private DatabaseTool databaseTool;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testTableComments() {
        System.out.println("=== 测试表名注释功能 ===");
        
        // 测试获取所有表的schema（包含表注释）
        String allTablesSchema = databaseTool.getDatabaseSchema();
        System.out.println("所有表的Schema（包含表注释）:");
        System.out.println(allTablesSchema);
        
        // 验证输出包含表注释
        assertTrue(allTablesSchema.contains("-- 部门信息表"), "应该包含部门信息表的注释");
        assertTrue(allTablesSchema.contains("-- 员工信息表"), "应该包含员工信息表的注释");
        assertTrue(allTablesSchema.contains("-- 项目信息表"), "应该包含项目信息表的注释");
        assertTrue(allTablesSchema.contains("-- 项目成员关系表"), "应该包含项目成员关系表的注释");
        
        System.out.println("✅ 表名注释功能测试通过");
    }
}
