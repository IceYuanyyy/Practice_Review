package com.exam.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.exam.common.Result;
import com.exam.entity.Question;
import com.exam.entity.Subject;
import com.exam.entity.User;
import com.exam.service.QuestionService;
import com.exam.service.SubjectService;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 科目管理 Controller
 * 已修改为多用户模式：只返回当前用户可见题目的科目
 * 
 * @author Exam System
 * @since 2025-12-19
 * @modified 2026-01-06 添加权限过滤
 */
@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private UserService userService;

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }
    
    /**
     * 检查是否是管理员
     */
    private boolean isAdmin() {
        Long userId = getCurrentUserId();
        User user = userService.getById(userId);
        return user != null && "admin".equals(user.getRole());
    }

    /**
     * 获取科目列表 - 基于当前用户可见的题目动态生成
     * 普通用户：只能看到自己的题目和公共题库的科目
     * 管理员：可以看到所有科目
     * 
     * @return 科目列表（包含题目数量统计）
     */
    @GetMapping
    public Result<List<Subject>> getAllSubjects() {
        Long userId = getCurrentUserId();
        boolean isAdmin = isAdmin();
        
        // 查询当前用户可见的所有题目
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        if (!isAdmin) {
            // 普通用户：只能看到自己的题目和公共题库
            wrapper.and(w -> w.isNull("owner_id").or().eq("owner_id", userId));
        }
        // 管理员：可以看到所有题目（无额外条件）
        
        List<Question> questions = questionService.list(wrapper);
        
        // 按科目分组并统计数量
        Map<String, Long> subjectCountMap = questions.stream()
            .collect(Collectors.groupingBy(
                q -> q.getSubject() != null ? q.getSubject() : "未分类",
                Collectors.counting()
            ));
        
        // 转换为 Subject 对象列表
        List<Subject> subjects = subjectCountMap.entrySet().stream()
            .map(entry -> {
                Subject subject = new Subject();
                subject.setName(entry.getKey());
                subject.setQuestionCount(entry.getValue().intValue());
                return subject;
            })
            .sorted((a, b) -> b.getQuestionCount().compareTo(a.getQuestionCount())) // 按题目数量降序
            .collect(Collectors.toList());
        
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
