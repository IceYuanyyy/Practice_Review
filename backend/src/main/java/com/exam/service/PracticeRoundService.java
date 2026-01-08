package com.exam.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.PracticeRound;
import com.exam.entity.Question;

/**
 * 练习轮次 Service 接口
 * 
 * @author Exam System
 * @since 2026-01-03
 */
public interface PracticeRoundService extends IService<PracticeRound> {

    /**
     * 开始或继续某科目的练习轮次
     * 如果存在未完成的轮次则继续，否则创建新轮次
     * 
     * @param userId 用户ID
     * @param subject 科目
     * @param ownerId (所有者ID，可选，用于管理员筛选)
     * @return 当前题目
     */
    Question startOrContinueRound(Long userId, String subject, Long ownerId);

    /**
     * 获取轮次中的下一题
     * 
     * @param userId 用户ID
     * @param subject 科目
     * @return 下一题，如果已完成本轮则返回 null
     */
    Question nextQuestion(Long userId, String subject);

    /**
     * 获取轮次中的上一题
     * 
     * @param userId 用户ID
     * @param subject 科目
     * @return 上一题，如果已是第一题则返回 null
     */
    Question prevQuestion(Long userId, String subject);

    /**
     * 获取当前轮次进度信息
     * 
     * @param userId 用户ID
     * @param subject 科目
     * @return 轮次信息
     */
    PracticeRound getProgress(Long userId, String subject);

    /**
     * 跳转到指定索引的题目
     * 
     * @param userId 用户ID
     * @param subject 科目
     * @param index 索引位置
     * @return 对应的题目
     */
    Question jumpToIndex(Long userId, String subject, int index);

    /**
     * 重置轮次（开始新一轮）
     * 
     * @param userId 用户ID
     * @param subject 科目
     * @return 新轮次的第一题
     */
    Question resetRound(Long userId, String subject, Long ownerId);

    /**
     * 获取指定轮次的答题状态结果
     *
     * @param userId 用户ID
     * @param subject 科目
     * @return 索引到状态的映射
     */
    java.util.Map<Integer, Integer> getRoundResults(Long userId, String subject);
}
