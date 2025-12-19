package com.exam.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.exam.dto.JudgeQuestionImportDTO;
import com.exam.entity.Question;
import com.exam.service.QuestionService;
import com.exam.service.SubjectService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 判断题导入监听器
 * 
 * @author Exam System
 * @since 2025-12-19
 */
@Slf4j
public class JudgeQuestionImportListener extends AnalysisEventListener<JudgeQuestionImportDTO> {

    private static final int BATCH_SIZE = 100;
    
    private List<Question> questions = new ArrayList<>();
    private QuestionService questionService;
    private SubjectService subjectService;
    private String customSubject; // 自定义科目名称
    private int successCount = 0;
    private int failCount = 0;
    // 统计每个科目导入的题目数量
    private Map<String, Integer> subjectCountMap = new HashMap<>();

    public JudgeQuestionImportListener(QuestionService questionService, SubjectService subjectService) {
        this(questionService, subjectService, null);
    }

    public JudgeQuestionImportListener(QuestionService questionService, SubjectService subjectService, String customSubject) {
        this.questionService = questionService;
        this.subjectService = subjectService;
        this.customSubject = customSubject;
    }

    @Override
    public void invoke(JudgeQuestionImportDTO data, AnalysisContext context) {
        try {
            Question question = convertToQuestion(data);
            questions.add(question);

            // 每100条批量保存一次
            if (questions.size() >= BATCH_SIZE) {
                saveBatch();
            }
        } catch (Exception e) {
            log.error("导入判断题失败: {}", data.getContent(), e);
            failCount++;
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 保存剩余数据
        saveBatch();
        
        // 更新科目表中的题目数量
        for (Map.Entry<String, Integer> entry : subjectCountMap.entrySet()) {
            subjectService.incrementQuestionCount(entry.getKey(), entry.getValue());
        }
        
        log.info("判断题导入完成，成功: {}，失败: {}", successCount, failCount);
    }

    private void saveBatch() {
        if (!questions.isEmpty()) {
            // 统计每个科目的题目数量
            for (Question question : questions) {
                String subject = question.getSubject();
                subjectCountMap.put(subject, subjectCountMap.getOrDefault(subject, 0) + 1);
                //由于mybatis的saveBatch方法不会自动填充字段，这里先将创建和修改时间赋值
                question.setCreateTime(LocalDateTime.now());
                question.setUpdateTime(LocalDateTime.now());
            }

            questionService.saveBatch(questions);
            //导入后排序
            questionService.reorderDisplayOrder("judge");
            successCount += questions.size();
            questions.clear();
        }
    }

    private Question convertToQuestion(JudgeQuestionImportDTO dto) {
        Question question = new Question();
        
        // 明确设置ID为null，让数据库自增
        question.setId(null);
        
        question.setType("judge");
        question.setContent(dto.getContent());
        question.setOptions(null); // 判断题没有选项
        
        // 标准化答案格式
        String answer = dto.getAnswer();
        if (answer != null) {
            answer = answer.trim();
            if (answer.equals("正确") || answer.equalsIgnoreCase("true") || answer.equals("T") || answer.equals("√")) {
                answer = "正确";
            } else if (answer.equals("错误") || answer.equalsIgnoreCase("false") || answer.equals("F") || answer.equals("×")) {
                answer = "错误";
            }
        }
        question.setAnswer(answer);
        
        question.setAnalysis(dto.getAnalysis());
        
        // 如果有自定义科目，使用自定义科目；否则使用Excel中的科目
        if (customSubject != null && !customSubject.trim().isEmpty()) {
            question.setSubject(customSubject.trim());
        } else {
            question.setSubject(dto.getSubject() != null ? dto.getSubject() : "未分类");
        }
        
        question.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : "medium");
        question.setIsMarked(false);
        question.setWrongCount(0);
        question.setPracticeCount(0);

        return question;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailCount() {
        return failCount;
    }
}
