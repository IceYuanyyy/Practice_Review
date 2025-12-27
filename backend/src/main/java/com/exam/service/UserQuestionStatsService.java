package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.UserQuestionStats;

import java.util.List;

/**
 * 用户题目统计 Service 接口
 * 
 * @author Exam System
 * @since 2025-12-27
 */
public interface UserQuestionStatsService extends IService<UserQuestionStats> {

    /**
     * 获取或创建用户题目统计
     * 
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 统计对象
     */
    UserQuestionStats getOrCreate(Long userId, Long questionId);

    /**
     * 更新练习统计（答题后调用）
     * 
     * @param userId 用户ID
     * @param questionId 题目ID
     * @param isCorrect 是否正确
     */
    void updatePracticeStats(Long userId, Long questionId, boolean isCorrect);

    /**
     * 切换收藏状态
     * 
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 切换后的收藏状态
     */
    boolean toggleMark(Long userId, Long questionId);

    /**
     * 获取用户的错题ID列表
     * 
     * @param userId 用户ID
     * @return 题目ID列表
     */
    List<Long> getWrongQuestionIds(Long userId);

    /**
     * 获取用户收藏的题目ID列表
     * 
     * @param userId 用户ID
     * @return 题目ID列表
     */
    List<Long> getMarkedQuestionIds(Long userId);

    /**
     * 清除用户某道题的错误记录
     * 
     * @param userId 用户ID
     * @param questionId 题目ID
     */
    void clearWrongRecord(Long userId, Long questionId);

    /**
     * 清除用户所有错题记录
     * 
     * @param userId 用户ID
     */
    void clearAllWrongRecords(Long userId);

    /**
     * 获取用户题目的错误次数
     * 
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 错误次数
     */
    int getWrongCount(Long userId, Long questionId);

    /**
     * 获取用户题目的收藏状态
     * 
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 是否收藏
     */
    boolean isMarked(Long userId, Long questionId);
}
