package com.example.text2sql.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 业务规则服务
 * 提供业务术语解释、时间推理、聚合规则等智能推理功能
 */
@Service
public class BusinessRuleService {

    private static final Map<String, String> BUSINESS_TERMS = new HashMap<>();
    private static final Map<String, String> SYNONYMS = new HashMap<>();
    
    static {
        // 业务术语解释
        BUSINESS_TERMS.put("员工", "employees表中的员工记录");
        BUSINESS_TERMS.put("部门", "department字段，表示员工所属部门");
        BUSINESS_TERMS.put("工资", "salary字段，使用decimal(10,2)类型存储");
        BUSINESS_TERMS.put("入职日期", "hire_date字段，记录员工入职时间");
        BUSINESS_TERMS.put("邮箱", "email字段，具有唯一性约束");
        BUSINESS_TERMS.put("职位", "position字段，存储员工职位信息");
        BUSINESS_TERMS.put("项目", "projects表中的项目记录");
        BUSINESS_TERMS.put("项目成员", "project_members表中的项目参与记录");
        BUSINESS_TERMS.put("状态", "status字段，表示记录的状态信息");
        
        // 同义词映射
        SYNONYMS.put("薪水", "工资");
        SYNONYMS.put("薪资", "工资");
        SYNONYMS.put("收入", "工资");
        SYNONYMS.put("入职时间", "入职日期");
        SYNONYMS.put("工作日期", "入职日期");
        SYNONYMS.put("邮箱地址", "邮箱");
        SYNONYMS.put("电子邮箱", "邮箱");
        SYNONYMS.put("工作部门", "部门");
        SYNONYMS.put("所属部门", "部门");
        SYNONYMS.put("工作岗位", "职位");
        SYNONYMS.put("职务", "职位");
    }

    /**
     * 获取业务术语解释
     */
    public String getBusinessTermExplanation(String term) {
        String normalizedTerm = normalizeTerm(term);
        return BUSINESS_TERMS.getOrDefault(normalizedTerm, "未定义的业务术语: " + term);
    }

    /**
     * 标准化术语（处理同义词）
     */
    private String normalizeTerm(String term) {
        return SYNONYMS.getOrDefault(term, term);
    }

    /**
     * 时间推理 - 解析相对时间表达式
     */
    public String parseTimeExpression(String timeExpression) {
        if (timeExpression == null || timeExpression.trim().isEmpty()) {
            return null;
        }

        String expression = timeExpression.trim().toLowerCase();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 处理"近X年"、"过去X年"等表达式
        Pattern yearPattern = Pattern.compile("(近|过去|最近)(\\d+)年");
        Matcher yearMatcher = yearPattern.matcher(expression);
        if (yearMatcher.find()) {
            int years = Integer.parseInt(yearMatcher.group(2));
            LocalDate startDate = now.minusYears(years);
            return String.format("时间范围: %s 至 %s", startDate.format(formatter), now.format(formatter));
        }

        // 处理"近X个月"、"过去X个月"等表达式
        Pattern monthPattern = Pattern.compile("(近|过去|最近)(\\d+)个月");
        Matcher monthMatcher = monthPattern.matcher(expression);
        if (monthMatcher.find()) {
            int months = Integer.parseInt(monthMatcher.group(2));
            LocalDate startDate = now.minusMonths(months);
            return String.format("时间范围: %s 至 %s", startDate.format(formatter), now.format(formatter));
        }

        // 处理"今年"、"去年"等表达式
        if (expression.contains("今年")) {
            return String.format("时间范围: %s-01-01 至 %s", now.getYear(), now.format(formatter));
        }
        if (expression.contains("去年")) {
            int lastYear = now.getYear() - 1;
            return String.format("时间范围: %d-01-01 至 %d-12-31", lastYear, lastYear);
        }

        return "无法解析的时间表达式: " + timeExpression;
    }

    /**
     * 聚合规则推理
     */
    public String getAggregationRule(String metric, String dimension) {
        if (metric == null || metric.trim().isEmpty()) {
            return "未指定指标，无法确定聚合规则";
        }

        String metricLower = metric.toLowerCase();
        
        // 数值型指标通常需要SUM
        if (metricLower.contains("数量") || metricLower.contains("总数") || 
            metricLower.contains("销量") || metricLower.contains("金额") ||
            metricLower.contains("工资") || metricLower.contains("薪水")) {
            return "数值型指标，建议使用SUM聚合函数";
        }
        
        // 计数类指标使用COUNT
        if (metricLower.contains("人数") || metricLower.contains("记录数") || 
            metricLower.contains("条数")) {
            return "计数类指标，建议使用COUNT聚合函数";
        }
        
        // 平均值类指标使用AVG
        if (metricLower.contains("平均") || metricLower.contains("均值")) {
            return "平均值类指标，建议使用AVG聚合函数";
        }
        
        return "根据指标类型选择合适的聚合函数";
    }

    /**
     * 表关联规则推理
     */
    public String getTableJoinRule(String table1, String table2) {
        if (table1 == null || table2 == null) {
            return "无法确定表关联规则";
        }

        String t1 = table1.toLowerCase();
        String t2 = table2.toLowerCase();

        // 员工表与项目成员表的关联
        if ((t1.contains("employee") && t2.contains("project_member")) ||
            (t1.contains("project_member") && t2.contains("employee"))) {
            return "通过employee_id字段关联员工表和项目成员表";
        }

        // 项目表与项目成员表的关联
        if ((t1.contains("project") && t2.contains("project_member")) ||
            (t1.contains("project_member") && t2.contains("project"))) {
            return "通过project_id字段关联项目表和项目成员表";
        }

        // 员工表与部门表的关联（如果有部门表）
        if ((t1.contains("employee") && t2.contains("department")) ||
            (t1.contains("department") && t2.contains("employee"))) {
            return "通过department字段关联员工表和部门表";
        }

        return "需要根据具体表结构确定关联字段";
    }

    /**
     * 业务逻辑推理
     */
    public String getBusinessLogic(String query, String selectedTables) {
        StringBuilder logic = new StringBuilder();
        
        // 时间相关推理
        if (query.contains("年") || query.contains("月") || query.contains("日")) {
            String timeLogic = parseTimeExpression(query);
            if (timeLogic != null) {
                logic.append("时间推理: ").append(timeLogic).append("\n");
            }
        }

        // 状态相关推理
        if (query.contains("状态") || query.contains("进行中") || query.contains("已完成")) {
            logic.append("状态推理: 需要查询status字段来确定记录状态\n");
        }

        // 排序相关推理
        if (query.contains("最高") || query.contains("最低") || query.contains("前") || query.contains("排序")) {
            logic.append("排序推理: 需要添加ORDER BY子句进行排序\n");
        }

        // 分组相关推理
        if (query.contains("每个") || query.contains("按") || query.contains("分组")) {
            logic.append("分组推理: 需要添加GROUP BY子句进行分组统计\n");
        }

        // 限制结果数量推理
        if (query.contains("前") && (query.contains("条") || query.contains("个"))) {
            logic.append("限制推理: 需要添加LIMIT子句限制返回结果数量\n");
        }

        return logic.toString().trim();
    }

    /**
     * 字段需求推理
     */
    public String getFieldRequirements(String query, String tableName) {
        StringBuilder requirements = new StringBuilder();
        
        String queryLower = query.toLowerCase();
        String tableLower = tableName.toLowerCase();

        if (tableLower.contains("employee")) {
            if (queryLower.contains("姓名") || queryLower.contains("名字")) {
                requirements.append("需要name字段\n");
            }
            if (queryLower.contains("工资") || queryLower.contains("薪水") || queryLower.contains("薪资")) {
                requirements.append("需要salary字段\n");
            }
            if (queryLower.contains("部门")) {
                requirements.append("需要department字段\n");
            }
            if (queryLower.contains("职位") || queryLower.contains("岗位")) {
                requirements.append("需要position字段\n");
            }
            if (queryLower.contains("入职") || queryLower.contains("日期")) {
                requirements.append("需要hire_date字段\n");
            }
            if (queryLower.contains("邮箱") || queryLower.contains("邮件")) {
                requirements.append("需要email字段\n");
            }
        }

        if (tableLower.contains("project")) {
            if (queryLower.contains("项目名称") || queryLower.contains("项目名")) {
                requirements.append("需要project_name字段\n");
            }
            if (queryLower.contains("状态")) {
                requirements.append("需要status字段\n");
            }
            if (queryLower.contains("开始") || queryLower.contains("结束")) {
                requirements.append("需要start_date和end_date字段\n");
            }
        }

        return requirements.toString().trim();
    }
}
