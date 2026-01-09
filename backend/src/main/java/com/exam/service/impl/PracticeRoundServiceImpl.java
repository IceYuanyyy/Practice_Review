package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.exam.entity.PracticeRound;
import com.exam.entity.Question;
import com.exam.entity.PracticeRecord;
import com.exam.mapper.PracticeRoundMapper;
import com.exam.service.PracticeRoundService;
import com.exam.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 练习轮次 Service 实现类
 * 
 * @author Exam System
 * @since 2026-01-03
 */
@Slf4j
@Service
public class PracticeRoundServiceImpl extends ServiceImpl<PracticeRoundMapper, PracticeRound> implements PracticeRoundService {

    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private com.exam.mapper.PracticeRecordMapper practiceRecordMapper;

    @Override
    @Transactional
    public Question startOrContinueRound(Long userId, String subject, Long ownerId) {
        log.info("开始/继续轮次: userId={}, subject={}, ownerId={}", userId, subject, ownerId);
        
        // 查找现有轮次
        PracticeRound round = baseMapper.selectByUserAndSubject(userId, subject);
        
        if (round == null || round.getIsFinished() || round.getQuestionIds() == null || round.getQuestionIds().isEmpty()) {
            // 创建新轮次（如果旧轮次的 questionIds 为 null，也需要重新创建）
            int nextRoundNumber = (round == null) ? 1 : round.getRoundNumber() + 1;
            round = createNewRound(userId, subject, nextRoundNumber, ownerId);
        }
        
        if (round == null || round.getQuestionIds() == null || round.getQuestionIds().isEmpty()) {
            log.warn("该科目没有题目: subject={}", subject);
            return null;
        }
        
        // 返回当前索引对应的题目
        Long questionId = round.getQuestionIds().get(round.getCurrentIndex());
        return questionService.getById(questionId);
    }

    /**
     * 创建新的练习轮次
     */
    private PracticeRound createNewRound(Long userId, String subject, int roundNumber, Long ownerId) {
        log.info("创建新轮次: userId={}, subject={}, roundNumber={}, ownerId={}", userId, subject, roundNumber, ownerId);
        
        // 获取该科目的所有题目ID
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getSubject, subject)
               .select(Question::getId);
        
        // 权限过滤：根据ownerId参数决定查询范围
        if (ownerId != null) {
            if (ownerId == -1L) {
                // 管理员查看"公共题库"：查询所有用户的题目（不添加owner_id过滤）
                // 不添加任何条件，让SQL查询该科目下的所有题目
            } else {
                // 查询指定用户的题目
                wrapper.eq(Question::getOwnerId, ownerId);
            }
        }
        
        List<Question> questions = questionService.list(wrapper);
        if (questions.isEmpty()) {
            return null;
        }
        
        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toCollection(ArrayList::new));
        
        // 随机打乱顺序
        Collections.shuffle(questionIds);
        
        // 创建或更新轮次记录
        PracticeRound round = new PracticeRound();
        round.setUserId(userId);
        round.setSubject(subject);
        round.setQuestionIds(questionIds);
        round.setCurrentIndex(0);
        round.setTotalCount(questionIds.size());
        round.setIsFinished(false);
        round.setRoundNumber(roundNumber);
        
        // 使用 insertOrUpdate 逻辑
        PracticeRound existing = baseMapper.selectByUserAndSubject(userId, subject);
        if (existing != null) {
            round.setId(existing.getId());
            this.updateById(round);
        } else {
            this.save(round);
        }
        
        return round;
    }

    @Override
    @Transactional
    public Question nextQuestion(Long userId, String subject) {
        log.info("获取下一题: userId={}, subject={}", userId, subject);
        
        PracticeRound round = baseMapper.selectByUserAndSubject(userId, subject);
        
        // 详细日志诊断
        if (round != null) {
            log.info("轮次数据: questionIds大小={}, currentIndex={}, totalCount={}, isFinished={}", 
                round.getQuestionIds() != null ? round.getQuestionIds().size() : "null",
                round.getCurrentIndex(),
                round.getTotalCount(),
                round.getIsFinished());
        }
        
        // 添加 questionIds 为 null 的检查
        if (round == null || round.getIsFinished() || round.getQuestionIds() == null) {
            return null;
        }
        
        int nextIndex = round.getCurrentIndex() + 1;
        if (nextIndex >= round.getTotalCount() || nextIndex >= round.getQuestionIds().size()) {
            // 本轮已完成
            round.setIsFinished(true);
            round.setCurrentIndex(round.getTotalCount() - 1);
            this.updateById(round);
            return null;
        }
        
        // 更新索引
        round.setCurrentIndex(nextIndex);
        this.updateById(round);
        
        Long questionId = round.getQuestionIds().get(nextIndex);
        return questionService.getById(questionId);
    }

    @Override
    @Transactional
    public Question prevQuestion(Long userId, String subject) {
        log.info("获取上一题: userId={}, subject={}", userId, subject);
        
        PracticeRound round = baseMapper.selectByUserAndSubject(userId, subject);
        // 添加 questionIds 为 null 的检查
        if (round == null || round.getQuestionIds() == null) {
            return null;
        }
        
        int prevIndex = round.getCurrentIndex() - 1;
        if (prevIndex < 0 || prevIndex >= round.getQuestionIds().size()) {
            // 已是第一题
            return null;
        }
        
        // 更新索引
        round.setCurrentIndex(prevIndex);
        this.updateById(round);
        
        Long questionId = round.getQuestionIds().get(prevIndex);
        return questionService.getById(questionId);
    }

    @Override
    public PracticeRound getProgress(Long userId, String subject) {
        return baseMapper.selectByUserAndSubject(userId, subject);
    }

    @Override
    @Transactional
    public Question jumpToIndex(Long userId, String subject, int index) {
        log.info("跳转到指定索引: userId={}, subject={}, index={}", userId, subject, index);
        
        PracticeRound round = baseMapper.selectByUserAndSubject(userId, subject);
        // 添加 questionIds 为 null 的检查
        if (round == null || round.getQuestionIds() == null || index < 0 || index >= round.getQuestionIds().size()) {
            return null;
        }
        
        round.setCurrentIndex(index);
        this.updateById(round);
        
        Long questionId = round.getQuestionIds().get(index);
        return questionService.getById(questionId);
    }

    @Override
    @Transactional
    public Question resetRound(Long userId, String subject, Long ownerId) {
        log.info("重置轮次: userId={}, subject={}, ownerId={}", userId, subject, ownerId);
        
        PracticeRound existing = baseMapper.selectByUserAndSubject(userId, subject);
        // 重置通常意味着重新在这个轮次练习，所以不应该增加轮次号
        int nextRoundNumber = existing == null ? 1 : existing.getRoundNumber();

        // 清空当前轮次的练习记录，确保答题卡与历史状态被重置
        if (existing != null && existing.getQuestionIds() != null && !existing.getQuestionIds().isEmpty()) {
            LambdaQueryWrapper<PracticeRecord> clearWrapper = new LambdaQueryWrapper<>();
            clearWrapper.eq(PracticeRecord::getUserId, userId)
                        .eq(PracticeRecord::getRoundNumber, nextRoundNumber)
                        .in(PracticeRecord::getQuestionId, existing.getQuestionIds());
            int removed = practiceRecordMapper.delete(clearWrapper);
            log.info("已清理当前轮次练习记录: removed={}, roundNumber={}, subject={}", removed, nextRoundNumber, subject);
        }
        
        PracticeRound newRound = createNewRound(userId, subject, nextRoundNumber, ownerId);
        if (newRound != null && !newRound.getQuestionIds().isEmpty()) {
            Long questionId = newRound.getQuestionIds().get(0);
            return questionService.getById(questionId);
        }
        return null;
    }

    @Override
    public java.util.Map<Integer, Integer> getRoundResults(Long userId, String subject) {
        PracticeRound round = baseMapper.selectByUserAndSubject(userId, subject);
        if (round == null || round.getQuestionIds() == null) {
            return Collections.emptyMap();
        }

        List<Long> questionIds = round.getQuestionIds();
        java.util.Map<Integer, Integer> results = new java.util.HashMap<>();

        // 查询这些题目的练习记录
        // 状态定义: 0-未做, 1-正确, 2-错误
        for (int i = 0; i < questionIds.size(); i++) {
            Long qid = questionIds.get(i);
            
            // 优先检查是否有针对该题目的正确记录（必须是当前轮次的）
            LambdaQueryWrapper<com.exam.entity.PracticeRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(com.exam.entity.PracticeRecord::getUserId, userId)
                         .eq(com.exam.entity.PracticeRecord::getQuestionId, qid)
                         .eq(com.exam.entity.PracticeRecord::getRoundNumber, round.getRoundNumber()) // 关键修改：只查当前轮次
                         .orderByDesc(com.exam.entity.PracticeRecord::getPracticeTime)
                         .last("LIMIT 1");
            
            com.exam.entity.PracticeRecord latestRecord = practiceRecordMapper.selectOne(recordWrapper);
            
            if (latestRecord == null) {
                results.put(i, 0);
            } else {
                results.put(i, latestRecord.getIsCorrect() ? 1 : 2);
            }
        }

        return results;
    }
}
