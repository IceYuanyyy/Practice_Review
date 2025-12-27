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
     * Excel 批量导入题目 - 导入的题目归属当前用户
     * 
     * @param file Excel 文件
     * @param subject 自定义科目名称（可选，如果提供则覆盖Excel中的科目）
     * @return 导入结果
     */
    @PostMapping("/excel")
    @OperationLog(type = "IMPORT", desc = "Excel批量导入题目")
    public Result<Map<String, Object>> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subject", required = false) String subject) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls") && !filename.endsWith(".csv"))) {
            return Result.error("文件格式不正确，请上传 Excel 文件");
        }
        
        // 简单的文件类型检查
        String contentType = file.getContentType();
        if (contentType != null && !contentType.contains("excel") && !contentType.contains("spreadsheet") && !contentType.contains("csv")) {
            // 注意：某些浏览器可能无法正确识别 content-type，这里作为警告日志而非强制拦截，避免兼容性问题
            log.warn("上传文件的 Content-Type 异常: {}", contentType);
        }

        Long userId = getCurrentUserId();

        try {
            // 创建监听器，传入自定义科目名称和当前用户ID
            ChoiceQuestionImportListener choiceListener = new ChoiceQuestionImportListener(questionService, subjectService, subject, userId);
            JudgeQuestionImportListener judgeListener = new JudgeQuestionImportListener(questionService, subjectService, subject, userId);

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

            // 返回结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("total", totalSuccess + totalFail);
            resultMap.put("success", totalSuccess);
            resultMap.put("fail", totalFail);

            return Result.success("导入完成", resultMap);

        } catch (Exception e) {
            log.error("导入失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 导出当前用户的题目到 Excel
     * 
     * @param response HTTP 响应
     * @param subject 科目筛选（可选）
     */
    @GetMapping("/export")
    public void exportExcel(
            HttpServletResponse response,
            @RequestParam(required = false) String subject) {
        try {
            Long userId = getCurrentUserId();
            
            // 只导出当前用户的题目（owner_id = userId）
            QueryWrapper<Question> wrapper = new QueryWrapper<>();
            wrapper.eq("owner_id", userId);
            
            if (subject != null && !subject.isEmpty()) {
                wrapper.eq("subject", subject);
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
            String fileName = URLEncoder.encode("我的题库_" + System.currentTimeMillis(), "UTF-8");
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
