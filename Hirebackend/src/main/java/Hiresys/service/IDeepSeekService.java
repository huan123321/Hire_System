package Hiresys.service;

public interface IDeepSeekService {

    String chat(String userMessage);

    String generateInterviewQuestions(String jobTitle, String jobDescription, String requirement);

    /**
     * 简历智能分析：评分、优势、改进建议
     */
    String analyzeResume(String resumeText, String targetPosition);

    /**
     * 职位-简历匹配度评分
     */
    String matchScore(String jobRequirements, String resumeText);

    /**
     * 优化职位描述
     */
    String optimizeJD(String draftJD);

    /**
     * 面试反馈总结
     */
    String summarizeFeedback(String candidateName, String jobTitle, String feedbackList);
}
