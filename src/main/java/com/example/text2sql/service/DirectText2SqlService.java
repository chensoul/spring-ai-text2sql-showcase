package com.example.text2sql.service;

import com.example.text2sql.util.SqlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.List;
import java.util.Map;

/**
 * Text2SQL 核心服务
 * 使用 Spring AI 将自然语言转换为 SQL 查询
 */
@Slf4j
@RequiredArgsConstructor
public class DirectText2SqlService implements Text2SqlService {
    private final ChatClient chatClient;
    private final DatabaseTool databaseTool;

    // SQL 生成提示模板
    private static final String SQL_GENERATION_PROMPT = """
            你是一个专业的 SQL 生成助手。基于以下数据库结构信息，将用户的自然语言查询转换为 SQL 语句。
            
            数据库结构信息：
            {schema}
            
            请遵循以下规则：
            1. 只生成 SELECT 查询语句
            2. 使用正确的表名和字段名
            3. 添加适当的 WHERE 条件
            4. 使用 LIMIT 限制结果数量（最多 1000 条）
            5. 确保 SQL 语法正确
            6. 如果查询涉及多表，请使用适当的 JOIN
            7. 只返回 SQL 语句，不要包含其他解释
            
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
                return Text2SqlResult.error("查询内容不能为空");
            }

            // 2. 生成 SQL
            String sql = generateSql(userQuery);
            if (sql == null || sql.trim().isEmpty()) {
                return Text2SqlResult.error("无法生成有效的SQL查询，请检查您的查询描述");
            }

            // 3. 验证 SQL 安全性
            if (!SqlUtils.isSqlSafe(sql)) {
                return Text2SqlResult.error("生成的 SQL 包含不安全的操作，请重新描述您的查询需求");
            }

            log.info("sql: {}", sql);

            // 4. 执行 SQL 查询
            List<Map<String, Object>> results = databaseTool.executeQuery(sql);

            return Text2SqlResult.success(sql, results);

        } catch (Exception e) {
            log.error("处理查询时发生错误: {}", e.getMessage(), e);
            return Text2SqlResult.error("处理查询时发生错误: " + e.getMessage());
        }
    }

    /**
     * 生成 SQL 查询语句
     */
    private String generateSql(String userQuery) {
        // 获取数据库结构信息
        String schema = databaseTool.getDatabaseSchema();

        // 创建提示模板
        PromptTemplate promptTemplate = new PromptTemplate(SQL_GENERATION_PROMPT);

        // 构建提示
        Prompt prompt = promptTemplate.create(Map.of(
                "schema", schema,
                "userQuery", userQuery
        ));

        // 调用 AI 生成 SQL
        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        String sql = response.getResult().getOutput().getText();

        // 清理 SQL 语句（移除可能的代码块标记）
        sql = sql.replaceAll("```sql", "").replaceAll("```", "").trim();

        return sql;
    }
}
