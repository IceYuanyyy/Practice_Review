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
    private Long importLogId; // 导入日志ID
    private int successCount = 0;
    private int failCount = 0;
    // 统计每个科目导入的题目数量
    private Map<String, Integer> subjectCountMap = new HashMap<>();
    // 记录每个 科目+题型 当前已使用的最大 display_order
    private Map<String, Integer> displayOrderCounter = new HashMap<>();

    public ChoiceQuestionImportListener(QuestionService questionService, SubjectService subjectService) {
        this(questionService, subjectService, null, null, null);
    }

    public ChoiceQuestionImportListener(QuestionService questionService, SubjectService subjectService, String customSubject) {
        this(questionService, subjectService, customSubject, null, null);
    }
    
    public ChoiceQuestionImportListener(QuestionService questionService, SubjectService subjectService, String customSubject, Long userId) {
        this(questionService, subjectService, customSubject, userId, null);
    }

    public ChoiceQuestionImportListener(QuestionService questionService, SubjectService subjectService, String customSubject, Long userId, Long importLogId) {
        this.questionService = questionService;
        this.subjectService = subjectService;
        this.customSubject = customSubject;
        this.userId = userId;
        this.importLogId = importLogId;
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

            // 为该 科目+题型 分配下一个 display_order
            int nextOrder = getNextDisplayOrder(question.getSubject(), question.getType());
            question.setDisplayOrder(nextOrder);

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
        if (!questions.isEmpty()) {
            saveBatch();
        }
        
        // 更新科目表统计数据
        for (Map.Entry<String, Integer> entry : subjectCountMap.entrySet()) {
            try {
                subjectService.incrementQuestionCount(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.error("更新科目统计失败: {}", entry.getKey(), e);
            }
        }

        log.info("选择题导入完成: 成功={}, 失败={}", successCount, failCount);
    }

    /**
     * 获取指定 科目+题型 的下一个显示序号
     * 规则：先从数据库统计当前数量作为起点，再在内存中递增，确保本次导入的题目顺序连续
     */
    private int getNextDisplayOrder(String subject, String type) {
        if (subject == null) subject = "未分类";
        if (type == null) type = "single-choice";

        String key = subject + "||" + type;
        Integer current = displayOrderCounter.get(key);
        if (current == null) {
            // 首次遇到该组合：从数据库中统计已有题目数量
            QueryWrapper<Question> wrapper = new QueryWrapper<>();
            wrapper.eq("subject", subject);
            wrapper.eq("type", type);
            int count = (int) questionService.count(wrapper);
            current = count;
        }
        current = current + 1;
        displayOrderCounter.put(key, current);
        return current;
    }

    private void saveBatch() {
        try {
            questionService.saveBatch(questions);
            successCount += questions.size();
            // 统计科目数量
            for (Question q : questions) {
                subjectCountMap.merge(q.getSubject(), 1, Integer::sum);
            }
            log.info("批量保存选择题: {} 条", questions.size());
        } catch (Exception e) {
            failCount += questions.size();
            log.error("批量保存选择题失败", e);
        } finally {
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
        question.setImageUrl(dto.getImageUrl());
        
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
        // display_order 在导入时由 getNextDisplayOrder 统一分配
        question.setIsMarked(false);
        question.setWrongCount(0);
        question.setPracticeCount(0);
        
        // 设置题目所属用户
        question.setOwnerId(userId);
        
        // 设置导入日志ID
        question.setImportLogId(importLogId);

        return question;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailCount() {
        return failCount;
    }
}
