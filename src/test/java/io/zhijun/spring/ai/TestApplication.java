package io.zhijun.spring.ai;

import org.springframework.boot.SpringApplication;

public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(Text2SqlApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}