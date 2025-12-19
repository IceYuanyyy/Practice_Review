package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 题目 Mapper 接口
 * 
 * @author Exam System
 * @since 2025-12-19
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    
    /**
     * 查询所有不同的科目名称
     * 
     * @return 科目名称列表
     */
    @Select("SELECT DISTINCT subject FROM question ORDER BY subject")
    List<String> selectDistinctSubjects();
}
