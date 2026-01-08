package com.exam.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.exam.entity.Question;
import com.exam.entity.WrongBook;

import java.util.List;

/**
 * 错题本 Service 接口
 * 
 * @author Exam System
 * @since 2026-01-03
 */
public interface WrongBookService extends IService<WrongBook> {

    /**
     * 分页查询用户的错题列表
     * 
     * @param userId 用户ID
     * @param page 当前页
     * @param size 每页条数
     * @return 分页结果（包含题目详情）
     */
    Page<Question> getWrongQuestionPage(Long userId, Long page, Long size);

    /**
     * 添加错题到错题本
     * 
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 是否成功
     */
    boolean addWrongQuestion(Long userId, Long questionId);

    /**
     * 标记题目为已掌握（从错题本移除）
     * 
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 是否成功
     */
    boolean markMastered(Long userId, Long questionId);

    /**
     * 增加熟练度
     * 
     * @param userId 用户ID
     * @param questionId 题目ID
     * @param increment 增加值
     * @return 是否成功
     */
    boolean increaseMastery(Long userId, Long questionId, int increment);

    /**
     * 获取用户错题本中的题目ID列表（用于刷题模式）
     * 
     * @param userId 用户ID
     * @return 题目ID列表
     */
    List<Long> getWrongQuestionIds(Long userId);
    
    /**
     * 清空用户错题本
     * 
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean clearAllWrongQuestions(Long userId);
}
