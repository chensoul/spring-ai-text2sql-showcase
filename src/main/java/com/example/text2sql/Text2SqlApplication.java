package com.example.text2sql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring AI Text2SQL 应用主类
 * 
 * 功能：
 * - 将自然语言查询转换为 SQL 语句
 * - 执行 SQL 查询并返回结果
 * - 提供 Web 界面进行交互
 */
@SpringBootApplication
public class Text2SqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(Text2SqlApplication.class, args);
    }
}
