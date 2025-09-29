package com.example.text2sql.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Text2SQL 步骤化结果封装类
 * 支持5个步骤的结构化输出
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Text2SqlStepResult {
    
    /**
     * 整体处理是否成功
     */
    private boolean success;
    
    /**
     * 错误信息
     */
    private String error;
    
    /**
     * 步骤1: 问题改写
     */
    private StepResult step1ProblemRewriting;
    
    /**
     * 步骤2: 数据表选取
     */
    private StepResult step2TableSelection;
    
    /**
     * 步骤3: 信息推理
     */
    private StepResult step3InformationInference;
    
    /**
     * 步骤4: 查询SQL生成
     */
    private StepResult step4SqlGeneration;
    
    /**
     * 步骤5: 执行SQL
     */
    private StepResult step5SqlExecution;
    
    /**
     * 最终查询结果数据
     */
    private List<Map<String, Object>> finalData;
    
    /**
     * 单个步骤结果
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StepResult {
        /**
         * 步骤是否完成
         */
        private boolean completed;
        
        /**
         * 步骤描述
         */
        private String description;
        
        /**
         * 步骤详细内容
         */
        private String content;
        
        /**
         * 步骤状态 (success, error, processing)
         */
        private String status;
        
        /**
         * 错误信息（如果有）
         */
        private String errorMessage;
    }
    
    /**
     * 创建成功结果
     */
    public static Text2SqlStepResult success(
            StepResult step1, StepResult step2, StepResult step3, 
            StepResult step4, StepResult step5, List<Map<String, Object>> data) {
        Text2SqlStepResult result = new Text2SqlStepResult();
        result.setSuccess(true);
        result.setStep1ProblemRewriting(step1);
        result.setStep2TableSelection(step2);
        result.setStep3InformationInference(step3);
        result.setStep4SqlGeneration(step4);
        result.setStep5SqlExecution(step5);
        result.setFinalData(data);
        return result;
    }
    
    /**
     * 创建错误结果
     */
    public static Text2SqlStepResult error(String errorMessage) {
        Text2SqlStepResult result = new Text2SqlStepResult();
        result.setSuccess(false);
        result.setError(errorMessage);
        return result;
    }
    
    /**
     * 创建部分成功结果（某些步骤失败）
     */
    public static Text2SqlStepResult partialSuccess(
            StepResult step1, StepResult step2, StepResult step3, 
            StepResult step4, StepResult step5, String errorMessage) {
        Text2SqlStepResult result = new Text2SqlStepResult();
        result.setSuccess(false);
        result.setError(errorMessage);
        result.setStep1ProblemRewriting(step1);
        result.setStep2TableSelection(step2);
        result.setStep3InformationInference(step3);
        result.setStep4SqlGeneration(step4);
        result.setStep5SqlExecution(step5);
        return result;
    }
}
