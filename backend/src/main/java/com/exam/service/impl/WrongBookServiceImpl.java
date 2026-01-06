package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.Question;
import com.exam.entity.WrongBook;
import com.exam.mapper.WrongBookMapper;
import com.exam.service.QuestionService;
import com.exam.service.WrongBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 错题本 Service 实现类
 * 
 * @author Exam System
 * @since 2026-01-03
 */
@Slf4j
@Service
public class WrongBookServiceImpl extends ServiceImpl<WrongBookMapper, WrongBook> implements WrongBookService {

    @Autowired
    private QuestionService questionService;

    @Override
    public Page<Question> getWrongQuestionPage(Long userId, Long page, Long size) {
        log.info("查询用户{}的错题本, page={}, size={}", userId, page, size);
        
        // 1. 分页查询错题本记录
        Page<WrongBook> wrongBookPage = new Page<>(page, size);
        LambdaQueryWrapper<WrongBook> wrapper = new LambdaQueryWrapper<>();
        // 显示所有错题，不再按熟练度过滤（用户要求错题永久保留直到手动删除）
        wrapper.eq(WrongBook::getUserId, userId)
               .orderByDesc(WrongBook::getUpdateTime);
        
        this.page(wrongBookPage, wrapper);
        
        // 2. 获取题目详情
        List<Long> questionIds = wrongBookPage.getRecords().stream()
                .map(WrongBook::getQuestionId)
                .collect(Collectors.toList());
        
        Page<Question> questionPage = new Page<>();
        questionPage.setCurrent(page);
        questionPage.setSize(size);
        questionPage.setTotal(wrongBookPage.getTotal());
        
        if (!questionIds.isEmpty()) {
            List<Question> questions = questionService.listByIds(questionIds);
            
            // 使用 Map 按照 questionIds 的顺序重新排序，确保与错题本记录顺序一致
            // Source: 解决 listByIds 不保证顺序的问题
            Map<Long, Question> questionMap = questions.stream()
                    .collect(Collectors.toMap(Question::getId, q -> q));
            
            List<Question> sortedQuestions = questionIds.stream()
                    .map(questionMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            questionPage.setRecords(sortedQuestions);
        }
        
        return questionPage;
    }

    @Override
    @Transactional
    public boolean addWrongQuestion(Long userId, Long questionId) {
        log.info("添加错题: userId={}, questionId={}", userId, questionId);
        
        // 检查是否已存在
        LambdaQueryWrapper<WrongBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongBook::getUserId, userId)
               .eq(WrongBook::getQuestionId, questionId);
        
        WrongBook existing = this.getOne(wrapper);
        if (existing != null) {
            // 已存在则增加错误次数，重置熟练度
            existing.setErrorCount(existing.getErrorCount() + 1);
            existing.setMasteryLevel(Math.max(0, existing.getMasteryLevel() - 20));
            return this.updateById(existing);
        }
        
        // 新增记录
        WrongBook wrongBook = new WrongBook();
        wrongBook.setUserId(userId);
        wrongBook.setQuestionId(questionId);
        wrongBook.setMasteryLevel(0);
        wrongBook.setErrorCount(1);
        return this.save(wrongBook);
    }

    @Override
    @Transactional
    public boolean markMastered(Long userId, Long questionId) {
        log.info("标记已掌握: userId={}, questionId={}", userId, questionId);
        
        LambdaQueryWrapper<WrongBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongBook::getUserId, userId)
               .eq(WrongBook::getQuestionId, questionId);
        
        // 设置熟练度为100表示已掌握
        WrongBook wrongBook = this.getOne(wrapper);
        if (wrongBook != null) {
            wrongBook.setMasteryLevel(100);
            return this.updateById(wrongBook);
        }
        return false;
    }

    @Override
    @Transactional
    public boolean increaseMastery(Long userId, Long questionId, int increment) {
        log.info("增加熟练度: userId={}, questionId={}, increment={}", userId, questionId, increment);
        
        LambdaQueryWrapper<WrongBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongBook::getUserId, userId)
               .eq(WrongBook::getQuestionId, questionId);
        
        WrongBook wrongBook = this.getOne(wrapper);
        if (wrongBook != null) {
            int newLevel = Math.min(100, wrongBook.getMasteryLevel() + increment);
            wrongBook.setMasteryLevel(newLevel);
            return this.updateById(wrongBook);
        }
        return false;
    }

    @Override
    public List<Long> getWrongQuestionIds(Long userId) {
        return baseMapper.selectQuestionIdsByUserId(userId);
    }
}
