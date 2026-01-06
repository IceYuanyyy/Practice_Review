package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.Question;
import com.exam.entity.Subject;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.SubjectMapper;
import com.exam.service.SubjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 科目 Service 实现类
 * 
 * @author Exam System
 * @since 2025-12-19
 */
@Slf4j
@Service
public class SubjectServiceImpl extends ServiceImpl<SubjectMapper, Subject> implements SubjectService {

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    @Transactional
    public Subject addOrUpdateSubject(String subjectName) {
        if (subjectName == null || subjectName.trim().isEmpty()) {
            subjectName = "未分类";
        }

        // 查询科目是否存在
        QueryWrapper<Subject> wrapper = new QueryWrapper<>();
        wrapper.eq("name", subjectName);
        Subject subject = this.getOne(wrapper);

        // 如果不存在则创建
        if (subject == null) {
            subject = new Subject();
            subject.setName(subjectName);
            subject.setQuestionCount(0);
            this.save(subject);
            log.info("创建新科目: {}", subjectName);
        }

        return subject;
    }

    @Override
    @Transactional
    public void incrementQuestionCount(String subjectName, int count) {
        if (count <= 0) return;
        
        Subject subject = addOrUpdateSubject(subjectName);
        subject.setQuestionCount(subject.getQuestionCount() + count);
        this.updateById(subject);
        log.debug("科目 {} 题目数量 +{}, 当前: {}", subjectName, count, subject.getQuestionCount());
    }

    @Override
    @Transactional
    public void decrementQuestionCount(String subjectName, int count) {
        if (count <= 0) return;

        QueryWrapper<Subject> wrapper = new QueryWrapper<>();
        wrapper.eq("name", subjectName);
        Subject subject = this.getOne(wrapper);

        if (subject != null) {
            int newCount = Math.max(0, subject.getQuestionCount() - count);
            subject.setQuestionCount(newCount);
            this.updateById(subject);
            log.debug("科目 {} 题目数量 -{}, 当前: {}", subjectName, count, newCount);
        }
    }

    @Override
    public List<Subject> getAllSubjects() {
        // 直接根据 question 表实时统计，确保数量准确
        List<Subject> subjectCounts = questionMapper.selectSubjectCounts();

        // 如果库里还没有题目，退回到已有的科目表
        if (subjectCounts == null || subjectCounts.isEmpty()) {
            QueryWrapper<Subject> wrapper = new QueryWrapper<>();
            wrapper.orderByDesc("question_count")
                   .orderByAsc("name");
            return this.list(wrapper);
        }

        return subjectCounts;
    }

    @Override
    @Transactional
    public int recountQuestions(String subjectName) {
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        wrapper.eq("subject", subjectName);
        Long countLong = questionMapper.selectCount(wrapper);
        int count = countLong != null ? countLong.intValue() : 0;

        // 更新科目表
        Subject subject = addOrUpdateSubject(subjectName);
        subject.setQuestionCount(count);
        this.updateById(subject);

        log.info("重新统计科目 {} 的题目数量: {}", subjectName, count);
        return count;
    }

    @Override
    @Transactional
    public void recountAllSubjects() {
        // 获取所有不同的科目名称
        List<Subject> allSubjects = this.list();
        
        for (Subject subject : allSubjects) {
            recountQuestions(subject.getName());
        }

        // 同时检查是否有题目的科目不在subject表中
        List<String> distinctSubjects = questionMapper.selectDistinctSubjects();
        for (String subjectName : distinctSubjects) {
            addOrUpdateSubject(subjectName);
            recountQuestions(subjectName);
        }

        log.info("已重新统计所有科目的题目数量");
    }

    @Override
    @Transactional
    public void removeEmptySubjects() {
        QueryWrapper<Subject> wrapper = new QueryWrapper<>();
        wrapper.le("question_count", 0);
        long count = this.count(wrapper);
        if (count > 0) {
            this.remove(wrapper);
            log.info("已清理 {} 个空闲科目", count);
        }
    }
}
