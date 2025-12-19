package com.exam.controller;

import com.exam.common.Result;
import com.exam.entity.Subject;
import com.exam.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 科目管理 Controller
 * 
 * @author Exam System
 * @since 2025-12-19
 */
@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    /**
     * 获取所有科目列表
     * 
     * @return 科目列表
     */
    @GetMapping
    public Result<List<Subject>> getAllSubjects() {
        List<Subject> subjects = subjectService.getAllSubjects();
        return Result.success(subjects);
    }

    /**
     * 重新统计所有科目的题目数量
     * 
     * @return 操作结果
     */
    @PostMapping("/recount")
    public Result<String> recountAllSubjects() {
        subjectService.recountAllSubjects();
        return Result.success("重新统计完成");
    }

    /**
     * 重新统计某个科目的题目数量
     * 
     * @param subjectName 科目名称
     * @return 题目数量
     */
    @PostMapping("/recount/{subjectName}")
    public Result<Integer> recountSubject(@PathVariable String subjectName) {
        int count = subjectService.recountQuestions(subjectName);
        return Result.success("重新统计完成", count);
    }
}
