package com.exam.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.PageResult;
import com.exam.common.Result;
import com.exam.entity.Question;
import com.exam.entity.User;
import com.exam.service.QuestionService;
import com.exam.service.UserQuestionStatsService;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目管理 Controller
 * 已修改为多用户模式：题目按用户隔离，支持公共题库
 * 
 * @author Exam System
 * @since 2025-12-19
 * @modified 2025-12-27 添加用户隔离
 */
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserQuestionStatsService userQuestionStatsService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.exam.service.SubjectService subjectService;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 检查是否是管理员
     */
    private boolean isAdmin() {
        Long userId = getCurrentUserId();
        User user = userService.getById(userId);
        return user != null && "admin".equals(user.getRole());
    }

    /**
     * 分页查询题目列表 - 只返回当前用户的题目和公共题库
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
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Long importLogId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String keyword) {
        
        Long userId = getCurrentUserId();
        boolean isAdmin = isAdmin();
        
        Page<Question> questionPage = new Page<>(page, size);
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        
        // 权限控制
        if (!isAdmin) {
            // 普通用户：仅能看到自己的题目
            wrapper.eq("owner_id", userId);
        } else {
             // 管理员: 如果指定了 ownerId，则筛选该用户或公共题库的题目
             if (ownerId != null) {
                 if (ownerId == -1L) {
                     wrapper.isNull("owner_id");
                 } else {
                     wrapper.eq("owner_id", ownerId);
                 }
             }
        }
        // 管理员：可以看到所有题目（无需额外过滤条件）
        
        // 筛选条件
        if (subject != null && !subject.isEmpty()) {
            wrapper.eq("subject", subject);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            wrapper.eq("difficulty", difficulty);
        }
        if (importLogId != null) {
            wrapper.eq("import_log_id", importLogId);
        }
        // 关键词搜索（搜索题目内容）
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like("content", keyword);
        }
        
        // 按类型和显示顺序排序
        wrapper.orderByAsc("type", "display_order");
        questionService.page(questionPage, wrapper);
        
        // 收集所有题目拥有者的ID
        List<Long> ownerIds = questionPage.getRecords().stream()
                .map(Question::getOwnerId)
                .filter(id -> id != null)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
                
        // 批量查询用户名
        java.util.Map<Long, String> userMap = new java.util.HashMap<>();
        if (!ownerIds.isEmpty()) {
            List<User> users = userService.listByIds(ownerIds);
            for (User user : users) {
                userMap.put(user.getId(), user.getUsername());
            }
        }
        
        // 为每道题设置统计数据和拥有者名称
        for (Question q : questionPage.getRecords()) {
            int wrongCount = userQuestionStatsService.getWrongCount(userId, q.getId());
            boolean isMarked = userQuestionStatsService.isMarked(userId, q.getId());
            q.setWrongCount(wrongCount);
            q.setIsMarked(isMarked);
            
            // 设置拥有者名称
            if (q.getOwnerId() != null) {
                q.setOwnerName(userMap.getOrDefault(q.getOwnerId(), "未知用户"));
            } else {
                q.setOwnerName("公共题库");
            }
        }
        
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
        Long userId = getCurrentUserId();
        
        Question question = questionService.getById(id);
        if (question == null) {
            return Result.error("题目不存在");
        }
        
        // 权限检查：只能查看自己的题目或公共题库
        if (question.getOwnerId() != null && !question.getOwnerId().equals(userId) && !isAdmin()) {
            return Result.error("无权访问此题目");
        }
        
        // 设置当前用户的统计数据
        question.setWrongCount(userQuestionStatsService.getWrongCount(userId, id));
        question.setIsMarked(userQuestionStatsService.isMarked(userId, id));
        
        return Result.success(question);
    }

    /**
     * 新增题目 - 自动设置 ownerId 为当前用户
     * 
     * @param question 题目对象
     * @return 操作结果
     */
    @PostMapping
    public Result<String> addQuestion(@RequestBody Question question) {
        Long userId = getCurrentUserId();
        
        // 设置题目所属用户
        question.setOwnerId(userId);
        
        // 设置默认值
        if (question.getDifficulty() == null) {
            question.setDifficulty("medium");
        }
        // 这些字段现在由 user_question_stats 表管理，但保留默认值用于兼容
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
     * 更新题目 - 需要权限验证
     * 
     * @param id 题目ID
     * @param question 题目对象
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public Result<String> updateQuestion(@PathVariable Long id, @RequestBody Question question) {
        Long userId = getCurrentUserId();
        
        // 获取原题目
        Question existingQuestion = questionService.getById(id);
        if (existingQuestion == null) {
            return Result.error("题目不存在");
        }
        
        // 权限检查：只能修改自己的题目，管理员可以修改所有
        if (existingQuestion.getOwnerId() != null && !existingQuestion.getOwnerId().equals(userId) && !isAdmin()) {
            return Result.error("无权修改此题目");
        }
        
        question.setId(id);
        // 不允许修改 ownerId
        question.setOwnerId(existingQuestion.getOwnerId());
        
        boolean success = questionService.updateQuestion(question);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除题目 - 需要权限验证
     * 
     * @param id 题目ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteQuestion(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        
        // 获取题目
        Question existingQuestion = questionService.getById(id);
        if (existingQuestion == null) {
            return Result.error("题目不存在");
        }
        
        // 权限检查：只能删除自己的题目，管理员可以删除所有
        if (existingQuestion.getOwnerId() != null && !existingQuestion.getOwnerId().equals(userId) && !isAdmin()) {
            return Result.error("无权删除此题目");
        }
        
        boolean success = questionService.deleteQuestion(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量删除题目 - 需要权限验证
     * 
     * @param ids 题目ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public Result<String> batchDeleteQuestions(@RequestBody List<Long> ids) {
        Long userId = getCurrentUserId();
        boolean admin = isAdmin();
        
        // 检查权限：过滤出有权删除的题目
        for (Long id : ids) {
            Question q = questionService.getById(id);
            if (q != null && q.getOwnerId() != null && !q.getOwnerId().equals(userId) && !admin) {
                return Result.error("包含无权删除的题目");
            }
        }
        
        boolean success = questionService.batchDeleteQuestions(ids);
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }

    /**
     * 清空题库 - 清空题目
     * 普通用户：只能清空自己的题目
     * 管理员：可以清空所有用户的题目（包括公共题库），并可按用户ID筛选
     * 
     * @param subject 科目（可选）
     * @param type 题型（可选）
     * @param importLogId 导入日志ID（可选）
     * @param ownerId 题目所有者ID（可选，仅管理员可用）
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    public Result<String> clearAllQuestions(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long importLogId,
            @RequestParam(required = false) Long ownerId) {
        
        Long userId = getCurrentUserId();
        boolean admin = isAdmin();
        
        // 构建查询条件
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        
        if (!admin) {
            // 普通用户只能清空自己的题目
            wrapper.eq("owner_id", userId);
        } else if (ownerId != null) {
            // 管理员可以按指定用户ID筛选（包括 null 表示公共题库）
            if (ownerId == -1) {
                // -1 表示公共题库（owner_id IS NULL）
                wrapper.isNull("owner_id");
            } else {
                wrapper.eq("owner_id", ownerId);
            }
        }
        // 管理员未指定 ownerId 时可以清空所有题目（不添加 owner_id 过滤条件）
        
        if (subject != null && !subject.isEmpty()) {
            wrapper.eq("subject", subject);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        if (importLogId != null) {
            wrapper.eq("import_log_id", importLogId);
        }
        
        long count = questionService.count(wrapper);
        questionService.remove(wrapper);

        // 如果题库已被完全清空，则重置自增主键，从 1 开始
        if (questionService.count() == 0) {
            questionService.resetAutoIncrement();
        }
        
        // 同步清理科目表
        // 1. 重新统计所有科目数量
        subjectService.recountAllSubjects();
        // 2. 清理空的科目
        subjectService.removeEmptySubjects();
        
        String msg = count > 0 ? String.format("成功清空 %d 道题目", count) : "没有符合条件的题目";
        return Result.success(msg);
    }

    /**
     * 随机获取题目 - 只从用户可见题目中随机
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
        
        Long userId = getCurrentUserId();
        
        // 构建查询条件：当前用户的题目 + 公共题库
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.isNull("owner_id").or().eq("owner_id", userId));
        
        if (subject != null && !subject.isEmpty()) {
            wrapper.eq("subject", subject);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            wrapper.eq("difficulty", difficulty);
        }
        
        // 随机排序取一条
        wrapper.orderByAsc("RAND()").last("LIMIT 1");
        
        Question question = questionService.getOne(wrapper);
        if (question == null) {
            return Result.error("没有找到符合条件的题目");
        }
        
        // 设置当前用户的统计数据
        question.setWrongCount(userQuestionStatsService.getWrongCount(userId, question.getId()));
        question.setIsMarked(userQuestionStatsService.isMarked(userId, question.getId()));
        
        return Result.success(question);
    }

    /**
     * 标记/取消标记题目 - 使用用户统计表
     * 
     * @param id 题目ID
     * @return 操作结果
     */
    @PutMapping("/{id}/mark")
    public Result<String> toggleMarkQuestion(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        
        Question question = questionService.getById(id);
        if (question == null) {
            return Result.error("题目不存在");
        }
        
        boolean newMarkStatus = userQuestionStatsService.toggleMark(userId, id);
        String msg = newMarkStatus ? "已收藏" : "已取消收藏";
        return Result.success(msg);
    }
}
