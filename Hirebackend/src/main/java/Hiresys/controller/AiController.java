package Hiresys.controller;

import Hiresys.dto.Result;
import Hiresys.service.IDeepSeekService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "AI 智能助手", description = "AI 聊天、面试题生成、简历分析、职位JD优化、候选人匹配、面试反馈总结")
@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private IDeepSeekService deepSeekService;

    @Operation(summary = "AI 对话（招聘助手）")
    @PostMapping("/chat")
    public Result<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        if (userMessage == null || userMessage.isBlank()) {
            return Result.fail("消息不能为空");
        }
        String reply = deepSeekService.chat(userMessage);
        return Result.ok(Map.of("reply", reply));
    }

    @Operation(summary = "根据职位描述生成面试题")
    @PostMapping("/interview-questions")
    public Result<Map<String, Object>> generateInterviewQuestions(@RequestBody Map<String, String> request) {
        String jobTitle = request.get("jobTitle");
        String jobDescription = request.get("jobDescription");
        String requirement = request.get("requirement");
        if (jobTitle == null || jobTitle.isBlank()) {
            return Result.fail("职位名称不能为空");
        }
        String reply = deepSeekService.generateInterviewQuestions(jobTitle, jobDescription, requirement);
        return Result.ok(Map.of("questions", reply));
    }

    @Operation(summary = "简历智能分析（评分+优势+建议）")
    @PostMapping("/analyze-resume")
    public Result<Map<String, Object>> analyzeResume(@RequestBody Map<String, String> request) {
        String resumeText = request.get("resumeText");
        String targetPosition = request.get("targetPosition");
        if (resumeText == null || resumeText.isBlank()) {
            return Result.fail("简历内容不能为空");
        }
        String reply = deepSeekService.analyzeResume(resumeText, targetPosition);
        return Result.ok(Map.of("result", reply));
    }

    @Operation(summary = "职位与简历匹配度评分")
    @PostMapping("/match-score")
    public Result<Map<String, Object>> matchScore(@RequestBody Map<String, String> request) {
        String jobRequirements = request.get("jobRequirements");
        String resumeText = request.get("resumeText");
        if (jobRequirements == null || jobRequirements.isBlank()) {
            return Result.fail("职位要求不能为空");
        }
        if (resumeText == null || resumeText.isBlank()) {
            return Result.fail("简历内容不能为空");
        }
        String reply = deepSeekService.matchScore(jobRequirements, resumeText);
        return Result.ok(Map.of("result", reply));
    }

    @Operation(summary = "优化职位描述 JD")
    @PostMapping("/optimize-jd")
    public Result<Map<String, Object>> optimizeJD(@RequestBody Map<String, String> request) {
        String draftJD = request.get("draftJD");
        if (draftJD == null || draftJD.isBlank()) {
            return Result.fail("职位描述草稿不能为空");
        }
        String reply = deepSeekService.optimizeJD(draftJD);
        return Result.ok(Map.of("result", reply));
    }

    @Operation(summary = "面试反馈总结")
    @PostMapping("/summarize-feedback")
    public Result<Map<String, Object>> summarizeFeedback(@RequestBody Map<String, String> request) {
        String candidateName = request.get("candidateName");
        String jobTitle = request.get("jobTitle");
        String feedbackList = request.get("feedbackList");
        if (candidateName == null || candidateName.isBlank()) {
            return Result.fail("候选人姓名不能为空");
        }
        if (feedbackList == null || feedbackList.isBlank()) {
            return Result.fail("面试反馈不能为空");
        }
        String reply = deepSeekService.summarizeFeedback(candidateName, jobTitle, feedbackList);
        return Result.ok(Map.of("result", reply));
    }
}
