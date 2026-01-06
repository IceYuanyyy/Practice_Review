package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.Subject;

import java.util.List;

/**
 * 科目 Service 接口
 * 
 * @author Exam System
 * @since 2025-12-19
 */
public interface SubjectService extends IService<Subject> {

    /**
     * 添加或更新科目（如果不存在则创建，如果存在则更新题目数量）
     * 
     * @param subjectName 科目名称
     * @return Subject 科目对象
     */
    Subject addOrUpdateSubject(String subjectName);

    /**
     * 增加科目题目数量
     * 
     * @param subjectName 科目名称
     * @param count 增加的数量
     */
    void incrementQuestionCount(String subjectName, int count);

    /**
     * 减少科目题目数量
     * 
     * @param subjectName 科目名称
     * @param count 减少的数量
     */
    void decrementQuestionCount(String subjectName, int count);

    /**
     * 获取所有科目列表
     * 
     * @return 科目列表
     */
    List<Subject> getAllSubjects();

    /**
     * 重新统计某个科目的题目数量
     * 
     * @param subjectName 科目名称
     * @return 题目数量
     */
    int recountQuestions(String subjectName);

    /**
     * 重新统计所有科目的题目数量
     */
    void recountAllSubjects();

    /**
     * 清理没有题目的科目
     */
    void removeEmptySubjects();
}
