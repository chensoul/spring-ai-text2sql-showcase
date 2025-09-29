package com.example.text2sql.controller;

import com.example.text2sql.service.DatabaseTool;
import com.example.text2sql.service.Text2SqlResult;
import com.example.text2sql.service.Text2SqlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Text2SQL REST API 控制器
 */
@Controller
@RequiredArgsConstructor
public class Text2SqlController {
    private final Text2SqlService text2SqlService;
    private final DatabaseTool databaseTool;

    /**
     * 主页
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Spring AI Text2SQL 演示");
        return "index";
    }

    /**
     * 处理自然语言查询的 API 接口
     */
    @PostMapping("/api/query")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processQuery(@RequestBody Map<String, String> request) {
        String query = request.get("query");

        if (query == null || query.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "查询内容不能为空");
            return ResponseEntity.badRequest().body(response);
        }

        // 处理查询
        Text2SqlResult result = text2SqlService.processQuery(query);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());

        if (result.isSuccess()) {
            response.put("sql", result.getSql());
            response.put("data", result.getData());
            response.put("count", result.getData().size());
        } else {
            response.put("error", result.getError());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取数据库结构信息的 API
     */
    @GetMapping("/api/schema")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getSchema() {
        Map<String, String> response = new HashMap<>();
        response.put("schema", databaseTool.getDatabaseSchema());
        return ResponseEntity.ok(response);
    }
}
