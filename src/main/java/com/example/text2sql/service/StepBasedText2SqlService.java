package com.example.text2sql.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.text2sql.util.SqlUtils.cleanSql;
import static com.example.text2sql.util.SqlUtils.isSqlSafe;

/**
 * 基于步骤的 Text2SQL 服务
 * 实现5个步骤的结构化输出
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StepBasedText2SqlService {

    @Qualifier("mcpChatClient")
    private final ChatClient mcpChatClient;

    // 步骤1: 问题改写提示模板
    private static final String STEP1_PROMPT = """
            请将用户的自然语言查询改写为更清晰、更具体的查询描述。
            
            用户查询：{userQuery}
            
            请提供：
            1. 改写后的查询描述
            2. 查询意图分析
            
            格式：
            改写为：[改写后的查询描述]
            意图：[查询意图分析]
            """;

    // 步骤2: 数据表选取提示模板
    private static final String STEP2_PROMPT = """
            基于改写后的查询，请使用 MCP 工具选择相关的数据表。
            
            改写后的查询：{rewrittenQuery}
            
            请：
            1. 使用 getTableNames() 获取所有可用表
            2. 分析查询需求，选择相关表
            3. 使用 getTableSchema(tableName) 获取选中表的结构
            
            格式：
            选择表为：[表名1, 表名2, ...]
            选择理由：[选择理由]
            """;

    // 步骤3: 信息推理提示模板
    private static final String STEP3_PROMPT = """
            基于选中的表和查询需求，进行信息推理。
            
            查询需求：{rewrittenQuery}
            选中表：{selectedTables}
            
            请分析：
            1. 需要哪些字段
            2. 需要什么条件
            3. 表之间的关联关系
            4. 业务逻辑推理
            
            格式：
            字段需求：[需要的字段]
            条件需求：[WHERE条件]
            关联关系：[JOIN关系]
            业务推理：[业务逻辑分析]
            """;

    // 步骤4: SQL生成提示模板
    private static final String STEP4_PROMPT = """
            基于前面的分析，生成SQL查询语句。
            
            查询需求：{rewrittenQuery}
            选中表：{selectedTables}
            推理结果：{inferenceResult}
            
            请生成标准的SQL查询语句，要求：
            1. 只使用SELECT查询
            2. 使用正确的表名和字段名
            3. 添加适当的WHERE条件
            4. 使用LIMIT限制结果数量（最多1000条）
            5. 确保SQL语法正确
            
            格式：
            生成SQL查询语句为：
            [SQL语句]
            """;

    // 步骤5: SQL执行提示模板
    private static final String STEP5_PROMPT = """
            请使用 executeQuery 工具执行以下 SQL 查询：
            
            {sqlQuery}
            
            请执行查询并返回结果。
            """;

    /**
     * 处理查询请求，返回5个步骤的结果
     */
    public Text2SqlStepResult processQueryWithSteps(String userQuery) {
        try {
            System.out.println("开始处理步骤化 Text2SQL 查询: " + userQuery);

            // 步骤1: 问题改写
            Text2SqlStepResult.StepResult step1 = executeStep1(userQuery);
            if (!step1.isCompleted()) {
                return Text2SqlStepResult.error("步骤1失败: " + step1.getErrorMessage());
            }

            // 步骤2: 数据表选取
            Text2SqlStepResult.StepResult step2 = executeStep2(step1.getContent());
            if (!step2.isCompleted()) {
                return Text2SqlStepResult.partialSuccess(step1, step2, null, null, null,
                        "步骤2失败: " + step2.getErrorMessage());
            }

            // 步骤3: 信息推理
            Text2SqlStepResult.StepResult step3 = executeStep3(step1.getContent(), step2.getContent());
            if (!step3.isCompleted()) {
                return Text2SqlStepResult.partialSuccess(step1, step2, step3, null, null,
                        "步骤3失败: " + step3.getErrorMessage());
            }

            // 步骤4: SQL生成
            Text2SqlStepResult.StepResult step4 = executeStep4(step1.getContent(), step2.getContent(),
                    step3.getContent());
            if (!step4.isCompleted()) {
                return Text2SqlStepResult.partialSuccess(step1, step2, step3, step4, null,
                        "步骤4失败: " + step4.getErrorMessage());
            }

            // 步骤5: SQL执行
            Text2SqlStepResult.StepResult step5 = executeStep5(step4.getContent());
            List<Map<String, Object>> finalData = null;
            if (step5.isCompleted()) {
                finalData = extractDataFromStep5(step5.getContent());
            }

            return Text2SqlStepResult.success(step1, step2, step3, step4, step5, finalData);
        } catch (Exception e) {
            log.error("步骤化 Text2SQL 处理失败", e);
            return Text2SqlStepResult.error("处理查询时发生错误: " + e.getMessage());
        }
    }

    /**
     * 执行步骤1: 问题改写
     */
    private Text2SqlStepResult.StepResult executeStep1(String userQuery) {
        try {
            System.out.println("\n执行步骤1: 问题改写");

            // 使用 PromptTemplate 进行问题改写
            PromptTemplate promptTemplate = new PromptTemplate(STEP1_PROMPT);
            String promptText = promptTemplate.create(Map.of("userQuery", userQuery)).getContents();

            String result = mcpChatClient.prompt()
                    .user(promptText)
                    .call()
                    .content();

            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(true);
            stepResult.setDescription("问题改写");
            stepResult.setContent(result);
            stepResult.setStatus("success");

            System.out.println(result);
            return stepResult;

        } catch (Exception e) {
            log.error("步骤1执行失败", e);
            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(false);
            stepResult.setDescription("问题改写");
            stepResult.setStatus("error");
            stepResult.setErrorMessage(e.getMessage());
            return stepResult;
        }
    }

    /**
     * 执行步骤2: 数据表选取
     */
    private Text2SqlStepResult.StepResult executeStep2(String rewrittenQuery) {
        try {
            System.out.println("\n执行步骤2: 数据表选取");

            // 使用 PromptTemplate 进行数据表选取
            PromptTemplate promptTemplate = new PromptTemplate(STEP2_PROMPT);
            String promptText = promptTemplate.create(Map.of("rewrittenQuery", rewrittenQuery)).getContents();

            String result = mcpChatClient.prompt()
                    .user(promptText)
                    .call()
                    .content();

            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(true);
            stepResult.setDescription("数据表选取");
            stepResult.setContent(result);
            stepResult.setStatus("success");

            System.out.println(result);
            return stepResult;

        } catch (Exception e) {
            log.error("步骤2执行失败", e);
            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(false);
            stepResult.setDescription("数据表选取");
            stepResult.setStatus("error");
            stepResult.setErrorMessage(e.getMessage());
            return stepResult;
        }
    }

    /**
     * 执行步骤3: 信息推理
     */
    private Text2SqlStepResult.StepResult executeStep3(String rewrittenQuery, String selectedTables) {
        try {
            System.out.println("\n执行步骤3: 信息推理");

            // 使用 PromptTemplate 进行信息推理
            PromptTemplate promptTemplate = new PromptTemplate(STEP3_PROMPT);
            String promptText = promptTemplate.create(Map.of(
                    "rewrittenQuery", rewrittenQuery,
                    "selectedTables", selectedTables
            )).getContents();

            String result = mcpChatClient.prompt()
                    .user(promptText)
                    .call()
                    .content();

            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(true);
            stepResult.setDescription("信息推理");
            stepResult.setContent(result);
            stepResult.setStatus("success");

            System.out.println(result);
            return stepResult;

        } catch (Exception e) {
            log.error("步骤3执行失败", e);
            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(false);
            stepResult.setDescription("信息推理");
            stepResult.setStatus("error");
            stepResult.setErrorMessage(e.getMessage());
            return stepResult;
        }
    }

    /**
     * 执行步骤4: SQL生成
     */
    private Text2SqlStepResult.StepResult executeStep4(String rewrittenQuery, String selectedTables,
                                                       String inferenceResult) {
        try {
            System.out.println("\n执行步骤4: SQL生成");

            // 使用 PromptTemplate 进行 SQL 生成
            PromptTemplate promptTemplate = new PromptTemplate(STEP4_PROMPT);
            String promptText = promptTemplate.create(Map.of(
                    "rewrittenQuery", rewrittenQuery,
                    "selectedTables", selectedTables,
                    "inferenceResult", inferenceResult
            )).getContents();

            String result = mcpChatClient.prompt()
                    .user(promptText)
                    .call()
                    .content();

            // 清理 SQL 语句
            String cleanedSql = cleanSql(result);

            // 验证 SQL 安全性
            if (!isSqlSafe(cleanedSql)) {
                throw new IllegalArgumentException("生成的 SQL 不安全，包含危险操作");
            }

            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(true);
            stepResult.setDescription("查询SQL生成");
            stepResult.setContent("生成SQL查询语句为：" + cleanedSql);
            stepResult.setStatus("success");

            System.out.println(cleanedSql);
            return stepResult;

        } catch (Exception e) {
            log.error("步骤4执行失败", e);
            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(false);
            stepResult.setDescription("查询SQL生成");
            stepResult.setStatus("error");
            stepResult.setErrorMessage(e.getMessage());
            return stepResult;
        }
    }

    /**
     * 执行步骤5: SQL执行
     */
    private Text2SqlStepResult.StepResult executeStep5(String sqlContent) {
        try {
            System.out.println("\n执行步骤5: SQL执行");

            String sql = extractSqlFromContent(sqlContent);
            if (sql == null || sql.trim().isEmpty()) {
                throw new IllegalArgumentException("无法从内容中提取有效的SQL语句");
            }

            // 使用 PromptTemplate 执行 SQL 查询
            PromptTemplate promptTemplate = new PromptTemplate(STEP5_PROMPT);
            String promptText = promptTemplate.create(Map.of("sqlQuery", sql)).getContents();

            String result = mcpChatClient.prompt()
                    .user(promptText)
                    .call()
                    .content();

            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(true);
            stepResult.setDescription("执行SQL");
            stepResult.setContent(result);
            stepResult.setStatus("success");

            System.out.println(result);
            return stepResult;

        } catch (Exception e) {
            log.error("步骤5执行失败", e);
            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(false);
            stepResult.setDescription("执行SQL");
            stepResult.setStatus("error");
            stepResult.setErrorMessage(e.getMessage());
            return stepResult;
        }
    }

    /**
     * 从步骤5的结果中提取数据
     */
    private List<Map<String, Object>> extractDataFromStep5(String step5Content) {
        try {
            // 尝试使用 MCP 工具直接执行查询获取结构化数据
            String sql = extractSqlFromContent(step5Content);
            if (sql != null) {
                return mcpChatClient.prompt()
                        .user("请使用 executeQuery 工具执行以下 SQL 查询: " + sql)
                        .call()
                        .entity(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                        });
            }
            return List.of();
        } catch (Exception e) {
            log.warn("无法提取结构化数据: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 从内容中提取字段需求
     */
    private String extractFieldRequirements(String content) {
        Pattern pattern = Pattern.compile("字段需求：([^\\n]+)");
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1).trim() : "所有字段";
    }

    /**
     * 从内容中提取条件需求
     */
    private String extractConditionRequirements(String content) {
        Pattern pattern = Pattern.compile("条件需求：([^\\n]+)");
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1).trim() : "无特殊条件";
    }

    /**
     * 从内容中提取关联关系
     */
    private String extractJoinRelations(String content) {
        Pattern pattern = Pattern.compile("关联关系：([^\\n]+)");
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? matcher.group(1).trim() : "无关联关系";
    }

    /**
     * 从内容中提取SQL语句
     */
    private String extractSqlFromContent(String content) {
        // 查找SQL语句
        Pattern pattern = Pattern.compile("(SELECT.*?)(?=\\n\\n|$)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String sql = matcher.group(1).trim();
            // 清理SQL语句
            sql = sql.replaceAll("```sql\\s*", "").replaceAll("```\\s*", "");
            return sql;
        }

        return null;
    }
}
