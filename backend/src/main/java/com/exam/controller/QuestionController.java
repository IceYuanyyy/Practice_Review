package com.exam.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.entity.Question;
import com.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 题目管理 Controller
 * 
 * @author Exam System
 * @since 2025-12-19
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    /**
     * 分页查询题目列表
     * 
     * @param page 当前页，默认1
     * @param size 每页显示条数，默认10
     * @param subject 科目（可选）
     * @param type 题型（可选）
     * @param difficulty 难度（可选）
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<Question>> getQuestionList(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty) {
        
        System.out.println("======= QuestionController.getQuestionList =======");
        System.out.println("接收参数: page=" + page + ", size=" + size + ", subject='" + subject + "', type='" + type + "', difficulty='" + difficulty + "'");
        
        Page<Question> questionPage = questionService.getQuestionPage(page, size, subject, type, difficulty);
        
        PageResult<Question> pageResult = new PageResult<>(
            questionPage.getRecords(),
            questionPage.getTotal(),
            questionPage.getCurrent(),
            questionPage.getSize()
        );
        
        return Result.success(pageResult);
    }

    /**
     * 根据ID获取题目详情
     * 
     * @param id 题目ID
     * @return 题目详情
     */
    @GetMapping("/{id}")
    public Result<Question> getQuestionById(@PathVariable Long id) {
        Question question = questionService.getById(id);
        if (question == null) {
            return Result.error("题目不存在");
        }
        return Result.success(question);
    }

    /**
     * 新增题目
     * 
     * @param question 题目对象
     * @return 操作结果
     */
    @PostMapping
    public Result<String> addQuestion(@RequestBody Question question) {
        // 设置默认值
        if (question.getDifficulty() == null) {
            question.setDifficulty("medium");
        }
        if (question.getIsMarked() == null) {
            question.setIsMarked(false);
        }
        if (question.getWrongCount() == null) {
            question.setWrongCount(0);
        }
        if (question.getPracticeCount() == null) {
            question.setPracticeCount(0);
        }

        boolean success = questionService.saveQuestion(question);
        return success ? Result.success("添加成功") : Result.error("添加失败");
    }

    /**
     * 更新题目
     * 
     * @param id 题目ID
     * @param question 题目对象
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<String> updateQuestion(@PathVariable Long id, @RequestBody Question question) {
        question.setId(id);
        boolean success = questionService.updateQuestion(question);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除题目
     * 
     * @param id 题目ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteQuestion(@PathVariable Long id) {
        boolean success = questionService.deleteQuestion(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量删除题目
     * 
     * @param ids 题目ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public Result<String> batchDeleteQuestions(@RequestBody java.util.List<Long> ids) {
        boolean success = questionService.batchDeleteQuestions(ids);
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }

    /**
     * 清空题库
     * 
     * @param subject 科目（可选）
     * @param type 题型（可选）
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    public Result<String> clearAllQuestions(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String type) {
        int count = questionService.clearAll(subject, type);
        String msg = count > 0 ? String.format("成功清空 %d 道题目", count) : "没有符合条件的题目";
        return Result.success(msg);
    }

    /**
     * 随机获取题目
     * 
     * @param subject 科目（可选）
     * @param type 题型（可选）
     * @param difficulty 难度（可选）
     * @return 随机题目
     */
    @GetMapping("/random")
    public Result<Question> getRandomQuestion(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty) {
        
        Question question = questionService.getRandomQuestion(subject, type, difficulty);
        if (question == null) {
            return Result.error("没有找到符合条件的题目");
        }
        return Result.success(question);
    }

    /**
     * 标记/取消标记题目
     * 
     * @param id 题目ID
     * @return 操作结果
     */
    @PutMapping("/{id}/mark")
    public Result<String> toggleMarkQuestion(@PathVariable Long id) {
        Question question = questionService.getById(id);
        if (question == null) {
            return Result.error("题目不存在");
        }
        question.setIsMarked(!question.getIsMarked());
        boolean success = questionService.updateById(question);
        return success ? Result.success("操作成功") : Result.error("操作失败");
    }
}
