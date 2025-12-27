package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.UserQuestionStats;
import com.exam.mapper.UserQuestionStatsMapper;
import com.exam.service.UserQuestionStatsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户题目统计 Service 实现类
 * 
 * @author Exam System
 * @since 2025-12-27
 */
@Slf4j
@Service
public class UserQuestionStatsServiceImpl extends ServiceImpl<UserQuestionStatsMapper, UserQuestionStats> 
        implements UserQuestionStatsService {

    @Override
    public UserQuestionStats getOrCreate(Long userId, Long questionId) {
        UserQuestionStats stats = baseMapper.selectByUserAndQuestion(userId, questionId);
        if (stats == null) {
            stats = new UserQuestionStats();
            stats.setUserId(userId);
            stats.setQuestionId(questionId);
            stats.setPracticeCount(0);
            stats.setWrongCount(0);
            stats.setIsMarked(false);
            stats.setCreateTime(LocalDateTime.now());
            baseMapper.insert(stats);
        }
        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePracticeStats(Long userId, Long questionId, boolean isCorrect) {
        UserQuestionStats stats = getOrCreate(userId, questionId);
        
        // 更新练习次数
        stats.setPracticeCount(stats.getPracticeCount() + 1);
        
        // 如果答错，增加错误次数
        if (!isCorrect) {
            stats.setWrongCount(stats.getWrongCount() + 1);
        }
        
        // 更新最后练习时间
        stats.setLastPracticeTime(LocalDateTime.now());
        
        baseMapper.updateById(stats);
        log.debug("更新用户题目统计: userId={}, questionId={}, isCorrect={}", userId, questionId, isCorrect);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleMark(Long userId, Long questionId) {
        UserQuestionStats stats = getOrCreate(userId, questionId);
        boolean newMarkStatus = !Boolean.TRUE.equals(stats.getIsMarked());
        stats.setIsMarked(newMarkStatus);
        baseMapper.updateById(stats);
        log.debug("切换收藏状态: userId={}, questionId={}, isMarked={}", userId, questionId, newMarkStatus);
        return newMarkStatus;
    }

    @Override
    public List<Long> getWrongQuestionIds(Long userId) {
        return baseMapper.selectWrongQuestionIds(userId);
    }

    @Override
    public List<Long> getMarkedQuestionIds(Long userId) {
        return baseMapper.selectMarkedQuestionIds(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearWrongRecord(Long userId, Long questionId) {
        UpdateWrapper<UserQuestionStats> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId)
               .eq("question_id", questionId)
               .set("wrong_count", 0);
        baseMapper.update(null, wrapper);
        log.debug("清除错题记录: userId={}, questionId={}", userId, questionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearAllWrongRecords(Long userId) {
        UpdateWrapper<UserQuestionStats> wrapper = new UpdateWrapper<>();
        wrapper.eq("user_id", userId)
               .gt("wrong_count", 0)
               .set("wrong_count", 0);
        baseMapper.update(null, wrapper);
        log.info("清除用户所有错题记录: userId={}", userId);
    }

    @Override
    public int getWrongCount(Long userId, Long questionId) {
        UserQuestionStats stats = baseMapper.selectByUserAndQuestion(userId, questionId);
        return stats != null ? stats.getWrongCount() : 0;
    }

    @Override
    public boolean isMarked(Long userId, Long questionId) {
        UserQuestionStats stats = baseMapper.selectByUserAndQuestion(userId, questionId);
        return stats != null && Boolean.TRUE.equals(stats.getIsMarked());
    }
}
