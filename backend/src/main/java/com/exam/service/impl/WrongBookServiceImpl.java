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
    public Page<Question> getWrongQuestionPage(Long userId, Long page, Long size, String subject, String keyword) {
        log.info("查询用户{}的错题本, page={}, size={}, subject={}, keyword={}", userId, page, size, subject, keyword);
        
        // 1. 获取用户所有错题记录（按时间倒序）
        // 因为题目信息在另一张表，需要先经过筛选才能进行正确的分页，所以这里先拉取所有记录
        LambdaQueryWrapper<WrongBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongBook::getUserId, userId)
               .orderByDesc(WrongBook::getUpdateTime);
        List<WrongBook> allWrongRecords = this.list(wrapper);
        
        if (allWrongRecords.isEmpty()) {
            return new Page<>(page, size);
        }
        
        List<Long> allQuestionIds = allWrongRecords.stream()
                .map(WrongBook::getQuestionId)
                .collect(Collectors.toList());
        
        // 2. 根据筛选条件查询题目
        LambdaQueryWrapper<Question> qWrapper = new LambdaQueryWrapper<>();
        qWrapper.in(Question::getId, allQuestionIds);
        
        if (subject != null && !subject.isEmpty()) {
            qWrapper.eq(Question::getSubject, subject);
        }
        if (keyword != null && !keyword.isEmpty()) {
            qWrapper.and(w -> w.like(Question::getContent, keyword)
                             .or()
                             .eq(Question::getId, keyword)); // 支持按ID搜索
        }
        
        List<Question> filteredQuestions = questionService.list(qWrapper);
        Map<Long, Question> questionMap = filteredQuestions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));
        
        // 3. 重新聚合和排序（保持错题本的时间顺序）
        List<Question> resultList = allWrongRecords.stream()
                .filter(wb -> questionMap.containsKey(wb.getQuestionId()))
                .map(wb -> {
                    Question q = questionMap.get(wb.getQuestionId());
                    // 可以在这里设置错误次数等额外信息
                    // q.setWrongCount(wb.getErrorCount()); 
                    return q;
                })
                .collect(Collectors.toList());
        
        // 4. 内存分页
        long total = resultList.size();
        long start = (page - 1) * size;
        List<Question> pageRecords;
        
        if (start >= total) {
            pageRecords = java.util.Collections.emptyList();
        } else {
            long end = Math.min(start + size, total);
            pageRecords = resultList.subList((int)start, (int)end);
        }
        
        Page<Question> resultPage = new Page<>(page, size);
        resultPage.setTotal(total);
        resultPage.setRecords(pageRecords);
        
        return resultPage;
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
        log.info("标记已掌握并删除错题记录: userId={}, questionId={}", userId, questionId);
        
        LambdaQueryWrapper<WrongBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongBook::getUserId, userId)
               .eq(WrongBook::getQuestionId, questionId);
        
        // 直接从错题本删除该记录
        return this.remove(wrapper);
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
    
    @Override
    @Transactional
    public boolean clearAllWrongQuestions(Long userId) {
        log.info("清空用户{}的错题本", userId);
        
        LambdaQueryWrapper<WrongBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongBook::getUserId, userId);
        
        return this.remove(wrapper);
    }
    
    @Override
    @Transactional
    public boolean removeByQuestionIds(Long userId, List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return false;
        }
        log.info("按科目清空用户{}的错题, 题目数量: {}", userId, questionIds.size());
        
        LambdaQueryWrapper<WrongBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongBook::getUserId, userId)
               .in(WrongBook::getQuestionId, questionIds);
        
        return this.remove(wrapper);
    }
}
