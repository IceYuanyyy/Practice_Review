package com.exam.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.Question;
import com.exam.mapper.QuestionMapper;
import com.exam.service.QuestionService;
import com.exam.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 题目 Service 实现类
 * 
 * @author Exam System
 * @since 2025-12-19
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Autowired
    private SubjectService subjectService;

    @Override
    public Page<Question> getQuestionPage(Long page, Long size, String subject, String type, String difficulty) {
        System.out.println("======= QuestionService.getQuestionPage =======");
        System.out.println("参数: page=" + page + ", size=" + size + ", subject=" + subject + ", type=" + type + ", difficulty=" + difficulty);
        
        Page<Question> pageParam = new Page<>(page, size);
        QueryWrapper<Question> wrapper = new QueryWrapper<>();

        // 构建查询条件
        if (StrUtil.isNotBlank(subject)) {
            System.out.println("添加subject条件: " + subject);
            wrapper.eq("subject", subject);
        }
        if (StrUtil.isNotBlank(type)) {
            System.out.println("添加type条件: " + type);
            wrapper.eq("type", type);
        }
        if (StrUtil.isNotBlank(difficulty)) {
            System.out.println("添加difficulty条件: " + difficulty);
            wrapper.eq("difficulty", difficulty);
        }

        // 按创建时间倒序
        wrapper.orderByDesc("create_time");
        
        System.out.println("SQL: " + wrapper.getTargetSql());

        Page<Question> result = this.page(pageParam, wrapper);
        System.out.println("查询结果: total=" + result.getTotal() + ", records=" + result.getRecords().size());
        System.out.println("===========================================");
        return result;
    }

    @Override
    public Question getRandomQuestion(String subject, String type, String difficulty) {
        QueryWrapper<Question> wrapper = new QueryWrapper<>();

        // 构建查询条件
        if (StrUtil.isNotBlank(subject)) {
            wrapper.eq("subject", subject);
        }
        if (StrUtil.isNotBlank(type)) {
            wrapper.eq("type", type);
        }
        if (StrUtil.isNotBlank(difficulty)) {
            wrapper.eq("difficulty", difficulty);
        }

        // 获取符合条件的题目数量
        long count = this.count(wrapper);
        if (count == 0) {
            return null;
        }

        // 随机偏移量
        int offset = RandomUtil.randomInt(0, (int) count);
        wrapper.last("LIMIT 1 OFFSET " + offset);

        return this.getOne(wrapper);
    }

    @Override
    @Transactional
    public int clearAll(String subject, String type) {
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        
        // 构建查询条件
        if (StrUtil.isNotBlank(subject)) {
            wrapper.eq("subject", subject);
        }
        if (StrUtil.isNotBlank(type)) {
            wrapper.eq("type", type);
        }
        
        // 先统计各科目的删除数量
        List<Question> questionsToDelete = this.list(wrapper);
        Map<String, Integer> subjectCountMap = new HashMap<>();
        for (Question q : questionsToDelete) {
            String subjectName = q.getSubject();
            subjectCountMap.put(subjectName, subjectCountMap.getOrDefault(subjectName, 0) + 1);
        }
        
        // 删除符合条件的题目
        int count = questionsToDelete.size();
        if (count > 0) {
            this.remove(wrapper);
            
            // 更新科目表
            for (Map.Entry<String, Integer> entry : subjectCountMap.entrySet()) {
                subjectService.decrementQuestionCount(entry.getKey(), entry.getValue());
            }
        }
        
        return count;
    }

    @Override
    @Transactional
    public boolean saveQuestion(Question question) {
        // 确保科目不为空
        if (StrUtil.isBlank(question.getSubject())) {
            question.setSubject("未分类");
        }
        
        boolean success = this.save(question);
        if (success) {
            // 更新科目表
            subjectService.incrementQuestionCount(question.getSubject(), 1);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean updateQuestion(Question question) {
        // 获取旧的题目信息
        Question oldQuestion = this.getById(question.getId());
        if (oldQuestion == null) {
            return false;
        }
        
        boolean success = this.updateById(question);
        if (success) {
            // 如果科目发生变化，需要更新两个科目的计数
            if (!oldQuestion.getSubject().equals(question.getSubject())) {
                subjectService.decrementQuestionCount(oldQuestion.getSubject(), 1);
                subjectService.incrementQuestionCount(question.getSubject(), 1);
            }
        }
        return success;
    }

    @Override
    @Transactional
    public boolean deleteQuestion(Long id) {
        Question question = this.getById(id);
        if (question == null) {
            return false;
        }
        
        boolean success = this.removeById(id);
        if (success) {
            // 更新科目表
            subjectService.decrementQuestionCount(question.getSubject(), 1);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean batchDeleteQuestions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        // 统计各科目的删除数量
        List<Question> questionsToDelete = this.listByIds(ids);
        Map<String, Integer> subjectCountMap = new HashMap<>();
        for (Question q : questionsToDelete) {
            String subject = q.getSubject();
            subjectCountMap.put(subject, subjectCountMap.getOrDefault(subject, 0) + 1);
        }
        
        boolean success = this.removeByIds(ids);
        if (success) {
            // 更新科目表
            for (Map.Entry<String, Integer> entry : subjectCountMap.entrySet()) {
                subjectService.decrementQuestionCount(entry.getKey(), entry.getValue());
            }
        }
        return success;
    }
}
