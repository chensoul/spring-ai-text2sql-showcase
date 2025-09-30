package com.example.text2sql.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.example.text2sql.util.SqlUtils.cleanSql;
import static com.example.text2sql.util.SqlUtils.isSqlSafe;

@Slf4j
@Service
@RequiredArgsConstructor
public class McpText2SqlService implements Text2SqlService {

    @Qualifier("mcpChatClient")
    private final ChatClient mcpChatClient;

    // SQL 生成提示模板
    private static final String SQL_GENERATION_PROMPT = """
            你是一个专业的 SQL 生成助手。你可以使用以下工具来获取数据库结构信息：
            
            可用工具：
            - getTableNames(): 获取所有表名
            - getTableSchema(tableName): 获取指定表的完整结构
            - getDatabaseSchema(): 获取所有表的完整结构
            - getTableColumns(tableName): 获取指定表的列信息
            - executeQuery(sql): 执行 SQL 查询验证结果
            
            请遵循以下规则：
            1. 首先使用 getTableNames() 了解数据库中有哪些表
            2. 根据用户查询需求，使用 getTableSchema() 或 getAllTablesSchema() 获取相关表的结构信息
            3. 只生成 SELECT 查询语句
            4. 使用正确的表名和字段名
            5. 添加适当的 WHERE 条件
            6. 使用 LIMIT 限制结果数量（最多 1000 条）
            7. 确保 SQL 语法正确
            8. 如果查询涉及多表，请使用适当的 JOIN
            9. 生成 SQL 后，可以使用 executeQuery() 验证结果
            10. 只返回 SQL 语句，不要包含其他解释
            
            用户查询：{userQuery}
            """;

    /**
     * 将自然语言转换为 SQL 并执行查询
     *
     * @param userQuery 用户自然语言查询
     * @return 查询结果
     */
    @Override
    public Text2SqlResult processQuery(String userQuery) {
        try {
            // 1. 验证输入
            if (userQuery == null || userQuery.trim().isEmpty()) {
                return Text2SqlResult.error("查询不能为空");
            }

            log.info("开始处理 MCP Text2SQL 查询: {}", userQuery);

            // 2. 使用 MCP 工具生成 SQL
            String sql = generateSqlWithMcpTools(userQuery);

            if (sql == null || sql.trim().isEmpty()) {
                return Text2SqlResult.error("无法生成有效的 SQL 查询");
            }

            // 3. 验证 SQL 安全性
            if (!isSqlSafe(sql)) {
                return Text2SqlResult.error("生成的 SQL 不安全，包含危险操作");
            }

            // 4. 执行查询
            List<Map<String, Object>> results = executeQuery(sql);

            log.info("MCP Text2SQL 查询完成，返回 {} 条记录", results.size());

            return Text2SqlResult.success(sql, results);

        } catch (Exception e) {
            log.error("MCP Text2SQL 处理失败", e);
            return Text2SqlResult.error("处理查询时发生错误: " + e.getMessage());
        }
    }

    /**
     * 使用 MCP 工具生成 SQL
     */
    private String generateSqlWithMcpTools(String userQuery) {
        try {
            PromptTemplate promptTemplate = new PromptTemplate(SQL_GENERATION_PROMPT);
            Prompt prompt = promptTemplate.create(Map.of("userQuery", userQuery));

            ChatResponse response = mcpChatClient.prompt(prompt).call().chatResponse();
            String sql = response.getResult().getOutput().getText();

            // 清理 SQL 语句，移除可能的解释文本
            sql = cleanSql(sql);

            log.info("MCP 工具生成的 SQL: {}", sql);
            return sql;

        } catch (Exception e) {
            log.error("使用 MCP 工具生成 SQL 失败", e);
            return null;
        }
    }

    /**
     * 使用大模型调用 MCP 工具执行查询
     */
    private List<Map<String, Object>> executeQuery(String sql) {
        log.info("使用 MCP 工具执行查询: {}", sql);

        // 使用大模型调用 MCP 工具执行查询
        List<Map<String, Object>> result = mcpChatClient.prompt()
                .user("请使用 executeQuery 工具执行以下 SQL 查询: " + sql)
                .call()
                .entity(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });

        log.info("MCP 工具执行查询完成，返回 {} 条记录", result.size());
        return result;
    }

}