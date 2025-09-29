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
            
            请严格按照以下格式返回，不要包含任何其他内容：
            问题改写，改写为：[改写后的查询描述]
            
            要求：
            1. 必须严格按照"问题改写，改写为："的格式
            2. 改写后的描述要简洁明了，突出查询的核心需求
            3. 使用标准的数据库查询术语
            4. 不要包含任何分析过程或额外说明
            """;

    // 步骤2: 数据表选取提示模板
    private static final String STEP2_PROMPT = """
            基于改写后的查询，请使用 MCP 工具选择相关的数据表。
            
            改写后的查询：{rewrittenQuery}
            
            请严格按照以下格式返回，不要包含任何其他内容：
            数据表选取，选择表为：表名1, 表名2, ...
            
            请：
            1. 使用 getTableNames() 获取所有可用表
            2. 分析查询需求，选择相关表
            3. 使用 getTableSchema(tableName) 获取选中表的结构
            4. 必须严格按照"数据表选取，选择表为："的格式
            5. 不要包含任何分析过程或额外说明
            """;

    // 步骤3: 信息推理提示模板
    private static final String STEP3_PROMPT = """
            基于选中的表和查询需求，进行信息推理。
            
            查询需求：{rewrittenQuery}
            选中表：{selectedTables}
            
            请严格按照以下格式返回：
            信息推理，本次推理参考业务信息是：
            
            然后提供详细的推理分析，包括：
            1. 需要哪些字段
            2. 需要什么条件
            3. 表之间的关联关系
            4. 业务逻辑推理
            5. 时间范围、状态字段等业务规则
            
            要求：
            1. 第一行必须是"信息推理，本次推理参考业务信息是："开头
            2. 后续内容要详细说明推理过程和业务规则
            3. 不要包含任何其他格式或额外说明
            """;

    // 步骤4: SQL生成提示模板
    private static final String STEP4_PROMPT = """
            基于前面的分析，生成SQL查询语句。
            
            查询需求：{rewrittenQuery}
            选中表：{selectedTables}
            推理结果：{inferenceResult}
            
            请严格按照以下格式返回：
            
            查询SQL生成，生成SQL查询语句为：
            
            ```sql
            [生成的SQL语句]
            ```
            
            **SQL智能注释**
            > 1. [对SQL语句的详细解释]
            > 2. [对各个子句的说明]
            > 3. [业务逻辑的解释]
            
            要求：
            1. 第一行必须是"查询SQL生成，生成SQL查询语句为："开头
            2. 生成的SQL语句必须用"```sql"和"```"包围
            3. 生成标准的SQL查询语句，只使用SELECT查询
            4. 使用正确的表名和字段名
            5. 添加适当的WHERE条件
            6. 使用LIMIT限制结果数量（最多1000条）
            7. 确保SQL语法正确
            8. 提供详细的SQL智能注释
            9. 不要包含任何其他格式或额外说明
            """;

    // 步骤5: SQL执行提示模板
    private static final String STEP5_PROMPT = """
            请使用 executeQuery 工具执行以下 SQL 查询：
            
            {sqlQuery}
            
            请严格按照以下格式返回：
            执行SQL，执行完成，返回了X条记录
            
            然后显示查询结果的详细信息。
            
            要求：
            1. 第一行必须是"执行SQL，执行完成，返回了X条记录"格式
            2. 执行查询并返回结果
            3. 显示查询结果的详细信息
            4. 不要包含任何其他格式或额外说明
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

            PromptTemplate promptTemplate = new PromptTemplate(STEP1_PROMPT);
            String promptText = promptTemplate.create(Map.of("userQuery", userQuery)).getContents();

            String result = mcpChatClient.prompt()
                    .user(promptText)
                    .call()
                    .content();

            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(true);
            stepResult.setContent(result.trim());
            stepResult.setStatus("success");

            System.out.println(result);
            return stepResult;

        } catch (Exception e) {
            log.error("步骤1执行失败", e);
            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(false);
            stepResult.setContent("问题改写，改写失败：" + e.getMessage());
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

            PromptTemplate promptTemplate = new PromptTemplate(STEP2_PROMPT);
            String promptText = promptTemplate.create(Map.of("rewrittenQuery", rewrittenQuery)).getContents();

            String result = mcpChatClient.prompt()
                    .user(promptText)
                    .call()
                    .content();

            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(true);
            stepResult.setContent(result.trim());
            stepResult.setStatus("success");

            System.out.println(result);
            return stepResult;

        } catch (Exception e) {
            log.error("步骤2执行失败", e);
            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(false);
            stepResult.setContent("数据表选取，选择失败：" + e.getMessage());
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
            stepResult.setContent(result.trim());
            stepResult.setStatus("success");

            System.out.println(result);
            return stepResult;

        } catch (Exception e) {
            log.error("步骤3执行失败", e);
            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(false);
            stepResult.setContent("信息推理，推理失败：" + e.getMessage());
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
            stepResult.setContent(result.trim());
            stepResult.setStatus("success");

            System.out.println(cleanedSql);
            return stepResult;

        } catch (Exception e) {
            log.error("步骤4执行失败", e);
            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(false);
            stepResult.setContent("查询SQL生成，生成失败：" + e.getMessage());
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

            PromptTemplate promptTemplate = new PromptTemplate(STEP5_PROMPT);
            String promptText = promptTemplate.create(Map.of("sqlQuery", sql)).getContents();

            String result = mcpChatClient.prompt()
                    .user(promptText)
                    .call()
                    .content();

            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(true);
            stepResult.setContent(result.trim());
            stepResult.setStatus("success");

            System.out.println(result);
            return stepResult;

        } catch (Exception e) {
            log.error("步骤5执行失败", e);
            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setCompleted(false);
            stepResult.setContent("执行SQL，执行失败：" + e.getMessage());
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
