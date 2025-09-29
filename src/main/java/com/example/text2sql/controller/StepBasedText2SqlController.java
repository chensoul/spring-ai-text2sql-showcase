package com.example.text2sql.controller;

import com.example.text2sql.service.StepBasedText2SqlService;
import com.example.text2sql.service.Text2SqlStepResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于步骤的 Text2SQL 控制器
 * 提供5个步骤的结构化输出接口
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class StepBasedText2SqlController {

    private final StepBasedText2SqlService stepBasedText2SqlService;

    /**
     * 显示步骤化 Text2SQL 页面
     *
     * @return 步骤化页面视图
     */
    @GetMapping("/step")
    public String showStepsPage() {
        return "step";
    }

    /**
     * 处理步骤化查询请求
     *
     * @param request 查询请求
     * @return 包含5个步骤的查询结果
     */
    @PostMapping("/api/steps/query")
    @ResponseBody
    public Text2SqlStepResult queryWithSteps(@RequestBody Map<String, String> request) {
        String query = request.get("query");

        if (query == null || query.trim().isEmpty()) {
            return Text2SqlStepResult.error("查询内容不能为空");
        }

        try {
            log.info("收到步骤化 Text2SQL 查询请求: {}", query);

            Text2SqlStepResult result = stepBasedText2SqlService.processQueryWithSteps(query);

            log.info("步骤化 Text2SQL 查询完成，成功: {}", result.isSuccess());

            return result;
        } catch (Exception e) {
            log.error("步骤化 Text2SQL 查询处理失败", e);
            return Text2SqlStepResult.error("查询处理失败: " + e.getMessage());
        }
    }
}
