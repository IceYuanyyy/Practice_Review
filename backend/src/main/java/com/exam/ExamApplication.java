package com.exam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 期末复习在线题库系统 - 启动类
 * 
 * @author Exam System
 * @since 2025-12-19
 */
@SpringBootApplication
@MapperScan("com.exam.mapper")
public class ExamApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamApplication.class, args);
        System.out.println("=================================");
        System.out.println("期末复习题库系统启动成功！");
        System.out.println("=================================");
    }
}
