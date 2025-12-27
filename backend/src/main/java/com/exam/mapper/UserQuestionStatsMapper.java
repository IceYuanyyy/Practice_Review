package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.UserQuestionStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户题目统计 Mapper 接口
 * 
 * @author Exam System
 * @since 2025-12-27
 */
@Mapper
public interface UserQuestionStatsMapper extends BaseMapper<UserQuestionStats> {

    /**
     * 根据用户ID和题目ID查询统计
     * 
     * @param userId 用户ID
     * @param questionId 题目ID
     * @return 统计对象
     */
    @Select("SELECT * FROM user_question_stats WHERE user_id = #{userId} AND question_id = #{questionId}")
    UserQuestionStats selectByUserAndQuestion(@Param("userId") Long userId, @Param("questionId") Long questionId);

    /**
     * 获取用户的错题ID列表
     * 
     * @param userId 用户ID
     * @return 题目ID列表
     */
    @Select("SELECT question_id FROM user_question_stats WHERE user_id = #{userId} AND wrong_count > 0")
    List<Long> selectWrongQuestionIds(@Param("userId") Long userId);

    /**
     * 获取用户收藏的题目ID列表
     * 
     * @param userId 用户ID
     * @return 题目ID列表
     */
    @Select("SELECT question_id FROM user_question_stats WHERE user_id = #{userId} AND is_marked = 1")
    List<Long> selectMarkedQuestionIds(@Param("userId") Long userId);
}
