package com.exam.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.exam.annotation.OperationLog;
import com.exam.common.Result;
import com.exam.dto.QuestionExportDTO;
import com.exam.dto.QuestionImportDTO;
import com.exam.dto.JudgeQuestionImportDTO;
import com.exam.entity.Question;
import com.exam.listener.ChoiceQuestionImportListener;
import com.exam.listener.JudgeQuestionImportListener;
import com.exam.service.QuestionService;
import com.exam.service.SubjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Excel 导入导出 Controller
 * 已修改为多用户模式：导入的题目归属当前用户，导出只导出当前用户的题目
 * 
 * @author Exam System
 * @since 2025-12-19
 * @modified 2025-12-27 添加用户隔离
 */
@Slf4j
@RestController
@RequestMapping("/api/import")
public class ImportController {

    @Autowired
    private com.exam.mapper.UserOperationLogMapper userOperationLogMapper;

    @Autowired
    private com.exam.service.UserService userService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private SubjectService subjectService;

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
        com.exam.entity.User user = userService.getById(userId);
        return user != null && "admin".equals(user.getRole());
    }

    /**
     * Excel 批量导入题目 - 导入的题目归属当前用户
     * 
     * @param file Excel 文件
     * @param subject 自定义科目名称（可选，如果提供则覆盖Excel中的科目）
     * @param request HTTP请求
     * @return 导入结果
     */
    @PostMapping("/excel")
    public Result<Map<String, Object>> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subject", required = false) String subject,
            javax.servlet.http.HttpServletRequest request) {
        
        long startTime = System.currentTimeMillis();
        
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls") && !filename.endsWith(".csv"))) {
            return Result.error("文件格式不正确，请上传 Excel 文件");
        }
        
        Long userId = getCurrentUserId();
        String username = "unknown";
        try {
            com.exam.entity.User user = userService.getById(userId);
            if (user != null) {
                username = user.getUsername();
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败", e);
        }

        // 1. 创建并保存操作日志
        com.exam.entity.UserOperationLog opLog = new com.exam.entity.UserOperationLog();
        opLog.setUserId(userId);
        opLog.setUsername(username);
        opLog.setOperationType("IMPORT");
        opLog.setOperationDesc("Excel批量导入题目");
        opLog.setRequestMethod(request.getMethod());
        opLog.setRequestUrl(request.getRequestURI());
        opLog.setRequestIp(getClientIp(request));
        opLog.setOperationTime(java.time.LocalDateTime.now());
        
        // 记录文件名和科目信息
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("fileName", filename);
        if (subject != null) dataMap.put("subject", subject);
        try {
            opLog.setOperationData(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(dataMap));
        } catch (Exception e) {
            log.warn("JSON序列化失败", e);
        }
        
        // 初始状态
        opLog.setOperationStatus(1);
        userOperationLogMapper.insert(opLog);
        Long logId = opLog.getId();

        try {
            // 创建监听器，传入自定义科目名称、当前用户ID和日志ID
            ChoiceQuestionImportListener choiceListener = new ChoiceQuestionImportListener(questionService, subjectService, subject, userId, logId);
            JudgeQuestionImportListener judgeListener = new JudgeQuestionImportListener(questionService, subjectService, subject, userId, logId);

            int totalSuccess = 0;
            int totalFail = 0;

            // 读取第一个 sheet：选择题
            try {
                EasyExcel.read(file.getInputStream(), QuestionImportDTO.class, choiceListener)
                        .sheet(0)
                        .doRead();
                totalSuccess += choiceListener.getSuccessCount();
                totalFail += choiceListener.getFailCount();
            } catch (Exception e) {
                log.warn("读取选择题 sheet 失败: {}", e.getMessage());
            }

            // 读取第二个 sheet：判断题
            try {
                EasyExcel.read(file.getInputStream(), JudgeQuestionImportDTO.class, judgeListener)
                        .sheet(1)
                        .doRead();
                totalSuccess += judgeListener.getSuccessCount();
                totalFail += judgeListener.getFailCount();
            } catch (Exception e) {
                log.warn("读取判断题 sheet 失败: {}", e.getMessage());
            }

            log.info("用户 {} 导入题目: 成功={}, 失败={}", userId, totalSuccess, totalFail);

            // 更新日志耗时
            opLog.setCostTime(System.currentTimeMillis() - startTime);
            opLog.setOperationDesc("Excel批量导入题目 (成功:" + totalSuccess + ", 失败:" + totalFail + ")");
            userOperationLogMapper.updateById(opLog);

            // 返回结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("total", totalSuccess + totalFail);
            resultMap.put("success", totalSuccess);
            resultMap.put("fail", totalFail);

            return Result.success("导入完成", resultMap);

        } catch (Exception e) {
            log.error("导入失败", e);
            // 更新日志为失败状态
            opLog.setOperationStatus(0);
            opLog.setErrorMessage(e.getMessage());
            opLog.setCostTime(System.currentTimeMillis() - startTime);
            userOperationLogMapper.updateById(opLog);
            
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 导出当前用户的题目到 Excel
     * 
     * @param response HTTP 响应
     * @param subject 科目筛选（可选）
     * @param importLogId 导入日志ID（可选，用于导出特定批次）
     */
    @GetMapping("/export")
    public void exportExcel(
            HttpServletResponse response,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) Long importLogId) {
        try {
            Long userId = getCurrentUserId();
            
            // 权限逻辑调整：
            QueryWrapper<Question> wrapper = new QueryWrapper<>();
            
            // 如果是普通用户，或者管理员未指定具体的导入批次（即导出所有），则默认只导出自己的题目
            // 如果是管理员且指定了 importLogId，则不限制 owner_id（允许导出该批次的所有题目）
            boolean isAdmin = isAdmin();
            if (!isAdmin || importLogId == null) {
                // 普通用户或常规导出：只导出当前用户的题目
                // 特殊情况：管理员导出全部时（无 importLogId），也暂时只导出自己的，或者可以改为导出所有？
                // 根据需求：修复的是"管理员无法下载其他用户导入的题库"，即有 importLogId 的情况。
                // 保持原逻辑变动最小：常规导出仍为"我的题库"，特定批次导出放宽权限。
                wrapper.eq("owner_id", userId);
            }
            // 如果是管理员且有 importLogId，不添加 owner_id 限制，即可导出该批次所有题目
            
            if (subject != null && !subject.isEmpty()) {
                wrapper.eq("subject", subject);
            }
            if (importLogId != null) {
                wrapper.eq("import_log_id", importLogId);
            }
            
            List<Question> questions = questionService.list(wrapper);
            
            log.info("用户 {} 导出题目: 数量={}", userId, questions.size());
            
            // 转换为导出 DTO
            List<QuestionExportDTO> exportList = questions.stream()
                    .map(QuestionExportDTO::fromQuestion)
                    .collect(Collectors.toList());

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileNamePrefix = importLogId != null ? "导入记录_" + importLogId : "我的题库";
            String fileName = URLEncoder.encode(fileNamePrefix + "_" + System.currentTimeMillis(), "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            // 写出 Excel
            EasyExcel.write(response.getOutputStream(), QuestionExportDTO.class)
                    .sheet("题目列表")
                    .doWrite(exportList);

        } catch (IOException e) {
            log.error("导出失败", e);
        }
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(javax.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 导出所有可见题目（包含公共题库）到 Excel
     * 
     * @param response HTTP 响应
     */
    @GetMapping("/export/all")
    public void exportAllExcel(HttpServletResponse response) {
        try {
            Long userId = getCurrentUserId();
            
            // 导出当前用户的题目 + 公共题库
            QueryWrapper<Question> wrapper = new QueryWrapper<>();
            wrapper.isNull("owner_id").or().eq("owner_id", userId);
            
            List<Question> questions = questionService.list(wrapper);
            
            log.info("用户 {} 导出所有可见题目: 数量={}", userId, questions.size());
            
            // 转换为导出 DTO
            List<QuestionExportDTO> exportList = questions.stream()
                    .map(QuestionExportDTO::fromQuestion)
                    .collect(Collectors.toList());

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("题库数据_" + System.currentTimeMillis(), "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            // 写出 Excel
            EasyExcel.write(response.getOutputStream(), QuestionExportDTO.class)
                    .sheet("题目列表")
                    .doWrite(exportList);

        } catch (IOException e) {
            log.error("导出失败", e);
        }
    }
}
