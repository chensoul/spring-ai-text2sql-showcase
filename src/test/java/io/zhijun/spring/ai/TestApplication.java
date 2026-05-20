package io.zhijun.spring.ai;

import org.springframework.boot.SpringApplication;

public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(TestApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}