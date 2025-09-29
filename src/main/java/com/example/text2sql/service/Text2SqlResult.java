package com.example.text2sql.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Text2SQL 结果封装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Text2SqlResult {
    private boolean success;
    private String sql;
    private List<Map<String, Object>> data;
    private String error;

    public static Text2SqlResult success(String sql, List<Map<String, Object>> data) {
        return new Text2SqlResult(true, sql, data, null);
    }

    public static Text2SqlResult error(String error) {
        return new Text2SqlResult(false, null, null, error);
    }
}