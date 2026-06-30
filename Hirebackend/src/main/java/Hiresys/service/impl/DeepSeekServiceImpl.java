package Hiresys.service.impl;

import Hiresys.service.IDeepSeekService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DeepSeekServiceImpl implements IDeepSeekService {

    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final String MODEL = "deepseek-chat";

    @Value("${deepseek.api-key:${DEEPSEEK_API_KEY:}}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String chat(String userMessage) {
        String systemPrompt = "你是一个专业的招聘系统助手，帮助求职者和HR解答招聘相关问题。请用友好、专业的中文回答，回答要简洁有条理。";
        return callApi(systemPrompt, userMessage);
    }

    @Override
    public String generateInterviewQuestions(String jobTitle, String jobDescription, String requirement) {
        String systemPrompt = "你是一位资深技术面试官。只返回 JSON 数组，不要加其他文字。";
        String userPrompt = String.format("""
                请根据以下职位信息，生成5道专业面试题。
                职位名称: %s
                职位描述: %s
                岗位要求: %s
                
                要求：题目覆盖技术能力、项目经验、综合素质三个维度。每题包含 question 和 point 两个字段。
                返回格式: [{"question": "...", "point": "..."}, ...]
                """, jobTitle,
                jobDescription != null ? jobDescription : "不限",
                requirement != null ? requirement : "不限");

        return callApi(systemPrompt, userPrompt);
    }

    @Override
    public String analyzeResume(String resumeText, String targetPosition) {
        String systemPrompt = "你是一位资深HR和职业规划师。请对简历进行专业分析，用JSON格式返回，不要加其他文字。";
        String userPrompt = String.format("""
                请分析以下简历，目标职位: %s
                
                简历内容:
                %s
                
                请从以下维度分析并返回JSON:
                1. 综合评分（1-100分）
                2. 核心优势（3-5条）
                3. 待改进点（3-5条）
                4. 优化建议（3-5条具体可操作的建议）
                5. 简历亮点提炼
                
                返回格式: {"score": 85, "strengths": ["..."], "weaknesses": ["..."], "suggestions": ["..."], "highlight": "..."}
                """, targetPosition != null ? targetPosition : "通用职位", resumeText);

        return callApi(systemPrompt, userPrompt);
    }

    @Override
    public String matchScore(String jobRequirements, String resumeText) {
        String systemPrompt = "你是一位专业的人才匹配顾问。请分析职位要求和候选人简历的匹配度，用JSON格式返回。";
        String userPrompt = String.format("""
                职位要求:
                %s
                
                候选人简历:
                %s
                
                请分析匹配度并返回JSON:
                1. 综合匹配度（0-100%）
                2. 技能匹配度
                3. 经验匹配度
                4. 学历匹配度
                5. 匹配亮点（候选人哪些方面特别适合）
                6. 不足之处（哪些方面需要提升）
                7. 推荐理由（30字以内）
                
                返回格式: {"overall": 85, "skillMatch": 90, "experienceMatch": 80, "educationMatch": 85, "highlights": ["..."], "gaps": ["..."], "recommendation": "..."}
                """, jobRequirements, resumeText);

        return callApi(systemPrompt, userPrompt);
    }

    @Override
    public String optimizeJD(String draftJD) {
        String systemPrompt = "你是一位资深招聘专家，擅长撰写吸引人的职位描述。请优化职位JD，用JSON格式返回，不要加其他文字。";
        String userPrompt = String.format("""
                请优化以下职位描述草稿，使其更专业、更有吸引力：
                
                %s
                
                返回JSON包含:
                1. optimized: 优化后的完整JD文本
                2. changes: 所做的主要改进（3-5条）
                3. keywords: 补充的关键词（5-10个，用于ATS优化）
                
                返回格式: {"optimized": "...", "changes": ["..."], "keywords": ["..."]}
                """, draftJD);

        return callApi(systemPrompt, userPrompt);
    }

    @Override
    public String summarizeFeedback(String candidateName, String jobTitle, String feedbackList) {
        String systemPrompt = "你是一位专业HR，擅长汇总面试评价。请总结面试反馈，用JSON格式返回。";
        String userPrompt = String.format("""
                候选人: %s
                应聘职位: %s
                
                各位面试官评价:
                %s
                
                请总结并返回JSON:
                1. 综合评分（1-5星）
                2. 总体评价（一句话）
                3. 技术能力评价
                4. 沟通能力评价
                5. 团队匹配度
                6. 录用建议（推荐录用/可考虑/不推荐）
                7. 风险提示（如有）
                8. 下一步建议
                
                返回格式: {"rating": 4, "summary": "...", "techAssessment": "...", "communication": "...", "teamFit": "...", "hireSuggestion": "...", "risks": "...", "nextStep": "..."}
                """, candidateName, jobTitle != null ? jobTitle : "未知职位", feedbackList);

        return callApi(systemPrompt, userPrompt);
    }

    private String callApi(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("DeepSeek API Key 未配置");
            return "{\"error\": \"AI 服务未配置，请设置 DEEPSEEK_API_KEY\"}";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey.trim());

            Map<String, Object> body = new HashMap<>();
            body.put("model", MODEL);
            body.put("temperature", 0.7);
            body.put("max_tokens", 3000);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
            ));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }

            log.error("DeepSeek API 返回异常: {}", response.getBody());
            return "{\"error\": \"AI 服务返回异常\"}";
        } catch (Exception e) {
            log.error("调用 DeepSeek API 失败", e);
            return "{\"error\": \"AI 服务调用失败: " + e.getMessage() + "\"}";
        }
    }
}
