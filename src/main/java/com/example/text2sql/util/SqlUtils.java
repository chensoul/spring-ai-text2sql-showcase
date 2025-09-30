package com.example.text2sql.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlUtils {

    /**
     * 清理 SQL 语句
     */
    public static String cleanSql(String sql) {
        if (sql == null) return "";

        // 移除可能的代码块标记
        sql = sql.replaceAll("```sql\\s*", "").replaceAll("```\\s*", "");

        // 查找第一个 SELECT 语句
        Pattern pattern = Pattern.compile("(SELECT.*?)(?=\\n\\n|$)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return sql.trim();
    }

    /**
     * 验证 SQL 安全性
     */
    public static boolean isSqlSafe(String sql) {
        String upperSql = sql.toUpperCase();

        // 检查是否包含危险操作
        String[] dangerousKeywords = {
                "DROP", "DELETE", "UPDATE", "INSERT", "ALTER", "CREATE", "TRUNCATE",
                "EXEC", "EXECUTE", "UNION", "INFORMATION_SCHEMA"
        };

        for (String keyword : dangerousKeywords) {
            if (upperSql.contains(keyword)) {
                return false;
            }
        }

        // 确保是 SELECT 查询
        return upperSql.trim().startsWith("SELECT");
    }
}
