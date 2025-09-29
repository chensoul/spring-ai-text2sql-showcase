package com.example.text2sql.config;

import com.example.text2sql.service.DatabaseTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 应用配置类
 */
@Configuration
public class AppConfig {
    /**
     * 配置 ChatClient Bean
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();
    }

    @Bean("mcpChatClient")
    public ChatClient mcpChatClient(ChatClient.Builder chatClientBuilder, DatabaseTool databaseTool) {
        return chatClientBuilder
                .defaultTools(databaseTool)
                .build();
    }
}
