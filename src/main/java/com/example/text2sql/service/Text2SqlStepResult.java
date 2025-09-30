package com.example.text2sql.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Text2SQL 步骤化结果封装类
 * 支持5个步骤的结构化输出
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Text2SqlStepResult {
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
     * 单个步骤结果
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StepResult {
        private static final String SUCCESS_STATUS = "success";
        private static final String ERROR_STATUS = "error";

        /**
         * 步骤详细内容
         */
        private String content;

        /**
         * 步骤状态 (success, error, processing)
         */
        private String status;

        /**
         * 判断步骤是否完成
         */
        public boolean isError() {
            return ERROR_STATUS.equals(status);
        }

        /**
         * 创建成功的步骤结果
         */
        public static Text2SqlStepResult.StepResult success(String content) {
            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setContent(content != null ? content.trim() : "");
            stepResult.setStatus(SUCCESS_STATUS);
            return stepResult;
        }

        /**
         * 创建错误的步骤结果
         */
        public static Text2SqlStepResult.StepResult error(String errorMessage) {
            Text2SqlStepResult.StepResult stepResult = new Text2SqlStepResult.StepResult();
            stepResult.setContent(errorMessage);
            stepResult.setStatus(ERROR_STATUS);
            return stepResult;
        }
    }

    /**
     * 创建部分成功结果（某些步骤失败）
     */
    public static Text2SqlStepResult create(
            StepResult step1, StepResult step2, StepResult step3,
            StepResult step4, StepResult step5) {
        Text2SqlStepResult result = new Text2SqlStepResult();
        result.setStep1ProblemRewriting(step1);
        result.setStep2TableSelection(step2);
        result.setStep3InformationInference(step3);
        result.setStep4SqlGeneration(step4);
        result.setStep5SqlExecution(step5);
        return result;
    }
}
