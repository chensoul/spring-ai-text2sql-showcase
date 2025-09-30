package com.example.text2sql.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    private final BusinessRuleService businessRuleService;

    private static final String SQL_PATTERN = "(SELECT.*?)(?=\\n\\n|$)";
    private static final String SQL_EXTRACTION_FAILED = "无法从内容中提取有效的SQL语句";
    private static final String SQL_UNSAFE_MSG = "生成的 SQL 包含危险操作";

    // 步骤1: 问题改写提示模板
    private static final String STEP1_PROMPT = """
            请将用户的自然语言查询改写为更清晰、更具体的查询描述。
            
            用户查询：{userQuery}
            
            判断规则：
            1. 数据库查询特征词：查询、统计、查找、获取、显示、列出、计算、汇总、分析、筛选、排序、分组、连接、关联
            2. 非数据库查询特征：问候语、自我介绍、聊天、天气、新闻、娱乐、技术问题、编程问题、系统问题
            
            请使用 getDatabaseSchema MCP工具查询数据库表结构
            
            判断流程：
            1. 检查是否包含数据库查询特征词
            2. 检查是否涉及数据库里面的业务实体
            3. 检查是否包含数据操作意图
            4. 排除明显的非数据库查询内容
            5. 使用MCP工具查询实际表结构，判断查询是否可行
            
            请严格按照以下格式返回，不要包含任何其他内容：
            
            如果是数据库查询且与现有表相关：
            问题改写，改写为：[改写后的查询描述]
            
            如果不是数据库查询：
            提示：请输入与数据库查询相关的问题，例如"查询员工信息"、"统计销售数据"等
            
            如果是数据库查询但与现有表无关：
            提示：当前数据库中没有相关的业务表，请查询员工、部门、项目等相关信息
            
            要求：
            1. 必须严格按照上述格式返回
            2. 对于数据库查询，改写后的描述要简洁明了，突出查询的核心需求
            3. 使用标准的数据库查询术语
            4. 不要包含任何分析过程或额外说明
            5. 必须使用MCP工具查询数据库结构后再做判断
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
            基于选中的表和查询需求，进行智能信息推理。
            
            查询需求：{rewrittenQuery}
            选中表：{selectedTables}
            业务规则参考：{businessRules}
            
            请严格按照以下格式返回，不要添加任何其他内容：
            
            信息推理，本次推理参考业务信息是：
            
            - 需要查询的字段：[具体列出需要查询的字段名]
            - 筛选条件：[说明WHERE条件，如无特定条件则说明"无特定筛选条件"]
            - 表关联关系：[说明表关联情况，如单表查询则说明"单表查询，无需表关联"]
            - 排序规则：[说明ORDER BY的排序逻辑和目的]
            - 分组统计：[说明是否需要GROUP BY，如不需要则说明"无需分组统计"]
            - 结果限制：[说明LIMIT限制数量和目的]
            - 业务含义：[解释查询的业务价值和实际应用场景]
            
            要求：
            1. 严格按照上述格式输出，每行一个要点
            2. 每个要点都要简洁明了，用一句话说明
            3. 重点突出业务逻辑和实际应用场景
            4. 避免过于技术化的描述，让用户容易理解
            5. 不要包含任何其他格式、标题或额外说明
            """;

    // 步骤4: SQL生成提示模板
    private static final String STEP4_PROMPT = """
            基于前面的深度分析，生成高质量的SQL查询语句。
            
            查询需求：{rewrittenQuery}
            选中表：{selectedTables}
            推理结果：{inferenceResult}
            
            请严格按照以下格式返回：
            
            查询SQL生成，生成SQL查询语句为：
            
            ```sql
            [生成的SQL语句]
            ```
            
            **SQL智能注释**
            > 1. **查询目的**: [说明这个查询要解决什么业务问题]
            > 2. **字段说明**: [解释SELECT子句中每个字段的作用和含义]
            > 3. **表关联**: [说明FROM和JOIN子句的表关联逻辑]
            > 4. **条件筛选**: [解释WHERE子句的筛选条件和业务含义]
            > 5. **分组聚合**: [说明GROUP BY和聚合函数的使用原因]
            > 6. **排序逻辑**: [解释ORDER BY的排序规则和业务价值]
            > 7. **性能优化**: [说明LIMIT、索引使用等性能考虑]
            > 8. **业务价值**: [总结查询结果对业务决策的帮助]
            
            要求：
            1. 第一行必须是"查询SQL生成，生成SQL查询语句为："开头
            2. 生成的SQL语句必须用"```sql"和"```"包围
            3. 生成标准的SQL查询语句，只使用SELECT查询
            4. 使用正确的表名和字段名，遵循数据库命名规范
            5. 根据推理结果添加适当的WHERE条件、JOIN、GROUP BY等
            6. 使用LIMIT限制结果数量（最多1000条）
            7. 确保SQL语法正确，符合MySQL标准
            8. 提供详细的SQL智能注释，每个注释都要有业务价值
            9. 考虑查询性能和可读性
            10. 不要包含任何其他格式或额外说明
            """;

    // 步骤5: SQL执行提示模板
    private static final String STEP5_PROMPT = """
            请使用 executeQuery 工具执行以下 SQL 查询：
            
            {sqlQuery}
            
            请严格按照以下格式返回：
            
            执行成功/失败，找到 X 条记录
            
            然后使用 Markdown 表格格式展示查询结果：
            
            | 字段名1 | 字段名2 | 字段名3 |
            |---------|---------|---------|
            | 值1     | 值2     | 值3     |
            | 值4     | 值5     | 值6     |
            
            要求：
            1. 第一行必须是"执行成功/失败，找到 X 条记录"格式
            2. 使用 Markdown 表格展示数据，表头使用字段名
            3. 数据行按顺序排列，每行一个记录
            4. 如果查询失败，显示错误信息而不是表格
            5. 如果查询成功但无数据，显示"无查询结果"
            6. 不要包含任何其他格式或额外说明
            """;

    /**
     * 执行步骤的简化方法（无后处理函数）
     */
    private Text2SqlStepResult.StepResult executeStep(int stepNumber,
                                                      String promptTemplate, Map<String, Object> variables) {
        return executeStep(stepNumber, promptTemplate, variables, null);
    }

    /**
     * 执行步骤的通用方法
     */
    private Text2SqlStepResult.StepResult executeStep(int stepNumber, String promptTemplate, Map<String, Object> variables, Function<String, String> function) {
        try {
            System.out.println("执行步骤" + stepNumber);

            PromptTemplate template = new PromptTemplate(promptTemplate);
            String promptText = template.create(variables).getContents();

            String result = mcpChatClient.prompt()
                    .user(promptText)
                    .call()
                    .content();

            if (function != null) {
                result = function.apply(result);
            }

            System.out.println(result);

            return Text2SqlStepResult.StepResult.success(result);
        } catch (Exception e) {
            log.error("步骤{}执行失败", stepNumber, e);
            return Text2SqlStepResult.StepResult.error(e.getMessage());
        }
    }

    /**
     * 处理查询请求，返回5个步骤的结果
     */
    public Text2SqlStepResult processQueryWithSteps(String userQuery) {
        log.info("开始处理步骤化 Text2SQL 查询: {}", userQuery);

        // 步骤1: 问题改写
        Text2SqlStepResult.StepResult step1 = executeStep1(userQuery);
        if (step1.isError()) {
            return Text2SqlStepResult.create(step1, null, null, null, null);
        }

        // 检查步骤1是否判断为数据库查询
        if (isNonDatabaseQuery(step1.getContent())) {
            // 将步骤1标记为失败
            Text2SqlStepResult.StepResult failedStep1 = Text2SqlStepResult.StepResult.error(
                    "非数据库查询，请输入与数据库相关的问题");
            return Text2SqlStepResult.create(failedStep1, null, null, null, null);
        }

        // 步骤2: 数据表选取
        Text2SqlStepResult.StepResult step2 = executeStep2(step1.getContent());
        if (step2.isError()) {
            return Text2SqlStepResult.create(step1, step2, null, null, null);
        }

        // 步骤3: 信息推理
        Text2SqlStepResult.StepResult step3 = executeStep3(step1.getContent(), step2.getContent());
        if (step3.isError()) {
            return Text2SqlStepResult.create(step1, step2, step3, null, null);
        }

        // 步骤4: SQL生成
        Text2SqlStepResult.StepResult step4 = executeStep4(step1.getContent(), step2.getContent(),
                step3.getContent());
        if (step4.isError()) {
            return Text2SqlStepResult.create(step1, step2, step3, step4, null);
        }

        // 步骤5: SQL执行
        Text2SqlStepResult.StepResult step5 = executeStep5(step4.getContent());

        return Text2SqlStepResult.create(step1, step2, step3, step4, step5);

    }

    /**
     * 判断步骤1的结果是否为非数据库查询
     */
    private boolean isNonDatabaseQuery(String step1Content) {
        if (step1Content == null || step1Content.trim().isEmpty()) {
            return true;
        }

        String content = step1Content.trim().toLowerCase();

        // 检查是否包含非数据库查询的提示信息
        return
                content.contains("请输入与数据库查询相关的问题") ||
                        content.contains("当前数据库中没有相关的业务表");
    }

    /**
     * 执行步骤1: 问题改写
     */
    private Text2SqlStepResult.StepResult executeStep1(String userQuery) {
        return executeStep(1, STEP1_PROMPT, Map.of("userQuery", userQuery));
    }

    /**
     * 执行步骤2: 数据表选取
     */
    private Text2SqlStepResult.StepResult executeStep2(String rewrittenQuery) {
        return executeStep(2, STEP2_PROMPT, Map.of("rewrittenQuery", rewrittenQuery));
    }

    /**
     * 执行步骤3: 信息推理
     */
    private Text2SqlStepResult.StepResult executeStep3(String rewrittenQuery, String selectedTables) {
        // 生成业务规则参考信息
        String businessRules = generateBusinessRules(rewrittenQuery, selectedTables);
        
        return executeStep(3, STEP3_PROMPT,
                Map.of("rewrittenQuery", rewrittenQuery, 
                       "selectedTables", selectedTables,
                       "businessRules", businessRules));
    }
    
    /**
     * 生成业务规则参考信息
     */
    private String generateBusinessRules(String query, String selectedTables) {
        StringBuilder rules = new StringBuilder();
        
        // 时间推理 - 简化输出
        String timeLogic = businessRuleService.parseTimeExpression(query);
        if (timeLogic != null && !timeLogic.contains("无法解析")) {
            rules.append("时间范围: ").append(timeLogic.replace("时间范围: ", "")).append("; ");
        }
        
        // 业务逻辑推理 - 简化输出
        String businessLogic = businessRuleService.getBusinessLogic(query, selectedTables);
        if (!businessLogic.isEmpty()) {
            // 提取关键信息，去掉"推理:"等前缀
            String simplifiedLogic = businessLogic.replaceAll("(时间推理|状态推理|排序推理|分组推理|限制推理): ", "").trim();
            if (!simplifiedLogic.isEmpty()) {
                rules.append("业务规则: ").append(simplifiedLogic).append("; ");
            }
        }
        
        // 字段需求推理 - 简化输出
        if (selectedTables != null && !selectedTables.isEmpty()) {
            String[] tables = selectedTables.split(",");
            for (String table : tables) {
                String fieldRequirements = businessRuleService.getFieldRequirements(query, table.trim());
                if (!fieldRequirements.isEmpty()) {
                    // 提取字段名，去掉"需要"等前缀
                    String fields = fieldRequirements.replaceAll("需要", "").replaceAll("字段", "").trim();
                    if (!fields.isEmpty()) {
                        rules.append("关键字段: ").append(fields).append("; ");
                    }
                }
            }
        }
        
        // 表关联规则 - 简化输出
        if (selectedTables != null && selectedTables.contains(",")) {
            String[] tables = selectedTables.split(",");
            if (tables.length >= 2) {
                String joinRule = businessRuleService.getTableJoinRule(tables[0].trim(), tables[1].trim());
                if (!joinRule.isEmpty() && !joinRule.contains("无法确定")) {
                    rules.append("表关联: ").append(joinRule).append("; ");
                }
            }
        }
        
        // 聚合规则推理 - 简化输出
        String aggregationRule = businessRuleService.getAggregationRule(query, selectedTables);
        if (!aggregationRule.isEmpty() && !aggregationRule.contains("未指定指标")) {
            rules.append("聚合方式: ").append(aggregationRule).append("; ");
        }
        
        String result = rules.toString().trim();
        // 去掉最后的分号
        if (result.endsWith("; ")) {
            result = result.substring(0, result.length() - 2);
        }
        
        return result.isEmpty() ? "基于查询需求进行智能分析" : result;
    }

    /**
     * 执行步骤4: SQL生成
     */
    private Text2SqlStepResult.StepResult executeStep4(String rewrittenQuery, String selectedTables,
                                                       String inferenceResult) {
        Map<String, Object> variables = Map.of(
                "rewrittenQuery", rewrittenQuery,
                "selectedTables", selectedTables,
                "inferenceResult", inferenceResult
        );
        return executeStep(4, STEP4_PROMPT, variables);
    }

    /**
     * 执行步骤5: SQL执行
     */
    private Text2SqlStepResult.StepResult executeStep5(String sqlContent) {
        // 从步骤4的内容中提取SQL语句
        String sql = extractSqlFromContent(sqlContent);
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException(SQL_EXTRACTION_FAILED);
        }

        if (!isSqlSafe(sql)) {
            throw new IllegalArgumentException(SQL_UNSAFE_MSG);
        }

        return executeStep(5, STEP5_PROMPT, Map.of("sqlQuery", sql));
    }

    /**
     * 从内容中提取SQL语句
     */
    private String extractSqlFromContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            log.warn("内容为空，无法提取SQL语句");
            return null;
        }

        try {
            // 查找SQL语句
            Pattern pattern = Pattern.compile(SQL_PATTERN, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);

            if (matcher.find()) {
                String sql = matcher.group(1).trim();
                // 清理SQL语句
                sql = sql.replaceAll("```sql\\s*", "").replaceAll("```\\s*", "");
                return sql;
            }

            log.warn("无法从内容中提取SQL语句，内容: {}", content.length() > 200 ? content.substring(0, 200) + "..." : content);
            return null;
        } catch (Exception e) {
            log.error("提取SQL语句时发生错误", e);
            return null;
        }
    }
}
