package com.exam.controller;

import com.alibaba.excel.EasyExcel;
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
 * 
 * @author Exam System
 * @since 2025-12-19
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
     * Excel 批量导入题目
     * 
     * @param file Excel 文件
     * @param subject 自定义科目名称（可选，如果提供则覆盖Excel中的科目）
     * @return 导入结果
     */
    @PostMapping("/excel")
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

        try {
            // 创建监听器，传入自定义科目名称
            ChoiceQuestionImportListener choiceListener = new ChoiceQuestionImportListener(questionService, subjectService, subject);
            JudgeQuestionImportListener judgeListener = new JudgeQuestionImportListener(questionService, subjectService, subject);

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
     * 导出所有题目到 Excel
     * 
     * @param response HTTP 响应
     */
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        try {
            // 查询所有题目
            List<Question> questions = questionService.list();
            
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
