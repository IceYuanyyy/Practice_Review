package com.exam.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.exam.dto.QuestionImportDTO;
import com.exam.entity.Question;
import com.exam.service.QuestionService;
import com.exam.service.SubjectService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 选择题导入监听器
 * 
 * @author Exam System
 * @since 2025-12-19
 */
@Slf4j
public class ChoiceQuestionImportListener extends AnalysisEventListener<QuestionImportDTO> {

    private static final int BATCH_SIZE = 100;
    
    private List<Question> questions = new ArrayList<>();
    private QuestionService questionService;
    private SubjectService subjectService;
    private String customSubject; // 自定义科目名称
    private Long userId; // 导入用户ID
    private int successCount = 0;
    private int failCount = 0;
    // 统计每个科目导入的题目数量
    private Map<String, Integer> subjectCountMap = new HashMap<>();

    public ChoiceQuestionImportListener(QuestionService questionService, SubjectService subjectService) {
        this(questionService, subjectService, null, null);
    }

    public ChoiceQuestionImportListener(QuestionService questionService, SubjectService subjectService, String customSubject) {
        this(questionService, subjectService, customSubject, null);
    }

    public ChoiceQuestionImportListener(QuestionService questionService, SubjectService subjectService, String customSubject, Long userId) {
        this.questionService = questionService;
        this.subjectService = subjectService;
        this.customSubject = customSubject;
        this.userId = userId;
    }

    @Override
    public void invoke(QuestionImportDTO data, AnalysisContext context) {
        try {
            Question question = convertToQuestion(data);
            
            // 检查是否已存在相同内容的题目（同一科目下）
            QueryWrapper<Question> wrapper = new QueryWrapper<>();
            wrapper.eq("content", question.getContent());
            wrapper.eq("subject", question.getSubject());
            if (questionService.count(wrapper) > 0) {
                log.warn("题目已存在，跳过: {}", question.getContent());
                return;
            }

            questions.add(question);

            // 每100条批量保存一次
            if (questions.size() >= BATCH_SIZE) {
                saveBatch();
            }
        } catch (Exception e) {
            log.error("导入选择题失败: {}", data.getContent(), e);
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
        
        log.info("选择题导入完成，成功: {}，失败: {}", successCount, failCount);
    }

    private void saveBatch() {
        if (!questions.isEmpty()) {
            // 统计每个科目的题目数量
            for (Question question : questions) {
                String subject = question.getSubject();
                subjectCountMap.put(subject, subjectCountMap.getOrDefault(subject, 0) + 1);
            }
            
            questionService.saveBatch(questions);
            successCount += questions.size();
            questions.clear();
        }
    }

    private Question convertToQuestion(QuestionImportDTO dto) {
        Question question = new Question();
        
        // 明确设置ID为null，让数据库自增
        question.setId(null);
        
        // 根据答案长度自动判断题型
        String answer = dto.getAnswer() != null ? dto.getAnswer().toUpperCase().trim() : "";
        boolean isMultiple = answer.length() > 1;
        
        // 增强判断：如果题目内容包含(多选)标识，强制设为多选题
        if (dto.getContent() != null && (dto.getContent().contains("多选") || dto.getContent().contains("多项"))) {
            isMultiple = true;
        }

        if (isMultiple) {
            question.setType("multiple-choice");  // 多选题（答案如：ABC）
        } else {
            question.setType("single-choice");     // 单选题（答案如：A）
        }
        
        question.setContent(dto.getContent());
        
        // 构建选项 JSON 数组
        List<String> optionsList = new ArrayList<>();
        if (dto.getOptionA() != null && !dto.getOptionA().trim().isEmpty()) {
            optionsList.add("A:" + dto.getOptionA());
        }
        if (dto.getOptionB() != null && !dto.getOptionB().trim().isEmpty()) {
            optionsList.add("B:" + dto.getOptionB());
        }
        if (dto.getOptionC() != null && !dto.getOptionC().trim().isEmpty()) {
            optionsList.add("C:" + dto.getOptionC());
        }
        if (dto.getOptionD() != null && !dto.getOptionD().trim().isEmpty()) {
            optionsList.add("D:" + dto.getOptionD());
        }
        if (dto.getOptionE() != null && !dto.getOptionE().trim().isEmpty()) {
            optionsList.add("E:" + dto.getOptionE());
        }
        question.setOptions(optionsList);
        
        question.setAnswer(dto.getAnswer() != null ? dto.getAnswer().toUpperCase() : "");
        if (dto.getAnalysis() != null && !dto.getAnalysis().trim().isEmpty()) {
            question.setAnalysis(dto.getAnalysis());
        } else {
            question.setAnalysis("暂无详细解析，请参考正确答案进行复习。");
        }
        
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
        
        // 设置题目所属用户
        question.setOwnerId(userId);

        return question;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailCount() {
        return failCount;
    }
}
