package Hiresys.service.impl;

import Hiresys.entity.Job;
import Hiresys.entity.JobApplication;
import Hiresys.entity.Resume;
import Hiresys.entity.Company;
import Hiresys.entity.ChatMessage;
import Hiresys.entity.User;
import Hiresys.exception.BusinessException;
import Hiresys.mapper.JobApplicationMapper;
import Hiresys.mapper.JobMapper;
import Hiresys.mapper.CompanyMapper;
import Hiresys.mapper.UserMapper;
import Hiresys.service.IChatService;
import Hiresys.service.IHotJobService;
import Hiresys.service.IJobApplicationService;
import Hiresys.service.IResumeService;
import Hiresys.websocket.ChatWebSocketHandler;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 职位申请记录表 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-09-27
 */
@Slf4j
@Service
public class JobApplicationServiceImpl extends ServiceImpl<JobApplicationMapper, JobApplication> implements IJobApplicationService {
    private static final Set<String> VALID_STATUSES = Set.of("new", "viewed", "interview", "hired", "rejected");
    
    @Autowired
    private IResumeService resumeService;
    
    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;
    
    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IChatService chatService;

    @Autowired
    private IHotJobService hotJobService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer applyForPosition(Integer jobId, Integer userId, Integer resumeId) {
        // 检查是否已经申请过该职位
        QueryWrapper<JobApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("job_id", jobId)
                   .eq("user_id", userId);
        long count = this.count(queryWrapper);
        
        if (count > 0) {
            throw new BusinessException("您已经申请过该职位，请勿重复申请");
        }
        
        Integer finalResumeId = resumeId;
        if (finalResumeId == null) {
            Resume defaultResume = resumeService.getDefaultResume(userId);
            if (defaultResume == null || defaultResume.getId() == null) {
                throw new BusinessException("请先创建简历后再申请职位");
            }
            finalResumeId = defaultResume.getId().intValue();
        } else {
            Resume selectedResume = resumeService.getById(finalResumeId);
            if (selectedResume == null || !userId.equals(selectedResume.getUserId())) {
                throw new BusinessException("所选简历无效，请重新选择");
            }
        }

        // 创建申请记录
        JobApplication application = new JobApplication();
        application.setJobId(jobId);
        application.setUserId(userId);
        application.setResumeId(finalResumeId);
        application.setStatus("new"); // 默认状态：新申请
        application.setFeedback("");
        application.setApplyTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());

        // 保存到数据库
        boolean success = this.save(application);
        if (!success) {
            throw new BusinessException("职位申请失败，请稍后重试");
        }

        hotJobService.recordApply(Long.valueOf(jobId));

        // 申请成功后，自动把简历发送给该公司的 HR（聊天消息）
        sendResumeToHr(application, finalResumeId);

        return application.getId();
    }

    /**
     * 申请职位成功后，自动将求职者简历作为聊天消息发送给职位所属公司的 HR。
     * 若该公司无 HR 或简历缺失，仅记录日志，不影响申请流程。
     */
    private void sendResumeToHr(JobApplication application, Integer resumeId) {
        try {
            // 1. 获取职位与公司
            Job job = jobMapper.selectById(application.getJobId());
            if (job == null || job.getCompanyName() == null || job.getCompanyName().isEmpty()) {
                log.info("申请后自动发简历跳过：职位 {} 无公司信息", application.getJobId());
                return;
            }
            Company company = companyMapper.selectOne(new QueryWrapper<Company>()
                    .eq("name", job.getCompanyName())
                    .last("LIMIT 1"));
            if (company == null) {
                log.info("申请后自动发简历跳过：找不到公司 {}", job.getCompanyName());
                return;
            }

            // 2. 找到该公司的 HR 用户
            User hr = userMapper.selectOne(new QueryWrapper<User>()
                    .eq("role", 1)
                    .eq("company_id", company.getId())
                    .eq("is_deleted", 0)
                    .last("LIMIT 1"));
            if (hr == null) {
                log.info("申请后自动发简历跳过：公司 {} 暂无 HR", company.getName());
                return;
            }

            // 3. 组装简历内容
            String resumeText = buildResumeText(application.getUserId(), resumeId);

            // 4. 保存聊天消息（求职者 → HR）
            ChatMessage message = new ChatMessage();
            message.setSenderId(application.getUserId());
            message.setReceiverId(hr.getId().longValue());
            message.setReceiverType("user");
            message.setContent(resumeText);
            message.setMessageType("text");
            message = chatService.saveMessage(message);

            // 5. WebSocket 实时推送新消息给 HR（若在线）
            Map<String, Object> data = new HashMap<>();
            data.put("id", message.getId());
            data.put("senderId", message.getSenderId());
            data.put("receiverId", message.getReceiverId());
            data.put("receiverType", message.getReceiverType());
            data.put("content", message.getContent());
            data.put("messageType", message.getMessageType());
            data.put("sendTime", message.getSendTime() != null ? message.getSendTime().toString() : null);

            Map<String, Object> push = new HashMap<>();
            push.put("type", "chat");
            push.put("message", "收到新消息");
            push.put("data", data);
            chatWebSocketHandler.sendMessageToUser("user", String.valueOf(hr.getId()), push);

            log.info("已自动将简历发送给公司 {} 的 HR {}（消息 ID: {}）", company.getName(), hr.getUsername(), message.getId());
        } catch (Exception e) {
            log.error("申请后自动发送简历失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 根据简历组装简历文本（basicInfo/education/experience 均为 JSON 字符串）
     */
    private String buildResumeText(Integer userId, Integer resumeId) {
        ObjectMapper mapper = new ObjectMapper();
        String name = "";
        String phone = "";
        String email = "";
        String location = "";
        String education = "";
        String experience = "";

        try {
            // 兜底：从用户信息取名称/昵称
            User applicant = userMapper.selectById(userId);
            if (applicant != null) {
                name = applicant.getRealName() != null && !applicant.getRealName().isEmpty()
                        ? applicant.getRealName()
                        : (applicant.getNickname() != null && !applicant.getNickname().isEmpty()
                                ? applicant.getNickname() : applicant.getUsername());
            }

            Resume resume = resumeService.getById(resumeId);
            if (resume != null) {
                if (resume.getName() != null && !resume.getName().isEmpty()) {
                    name = resume.getName();
                }
                if (resume.getBasicInfo() != null && !resume.getBasicInfo().isEmpty()) {
                    JsonNode basic = mapper.readTree(resume.getBasicInfo());
                    if (basic.hasNonNull("name")) name = basic.get("name").asText();
                    if (basic.hasNonNull("phone")) phone = basic.get("phone").asText();
                    if (basic.hasNonNull("email")) email = basic.get("email").asText();
                    if (basic.hasNonNull("location")) {
                        JsonNode loc = basic.get("location");
                        if (loc.isArray()) {
                            List<String> parts = new java.util.ArrayList<>();
                            loc.forEach(n -> parts.add(n.asText()));
                            // 去掉旧的「中国」顶层
                            if (!parts.isEmpty() && "中国".equals(parts.get(0))) {
                                parts.remove(0);
                            }
                            location = String.join(" ", parts);
                        } else {
                            location = loc.asText();
                        }
                    }
                }
                if (resume.getEducation() != null && !resume.getEducation().isEmpty()) {
                    JsonNode eduArr = mapper.readTree(resume.getEducation());
                    if (eduArr.isArray() && !eduArr.isEmpty()) {
                        JsonNode edu = eduArr.get(0);
                        StringBuilder sb = new StringBuilder();
                        if (edu.hasNonNull("school")) sb.append(edu.get("school").asText());
                        if (edu.hasNonNull("degree")) {
                            if (sb.length() > 0) sb.append(" · ");
                            sb.append(edu.get("degree").asText());
                        }
                        if (edu.hasNonNull("major")) {
                            if (sb.length() > 0) sb.append(" · ");
                            sb.append(edu.get("major").asText());
                        }
                        education = sb.toString();
                    }
                }
                if (resume.getExperience() != null && !resume.getExperience().isEmpty()) {
                    JsonNode expArr = mapper.readTree(resume.getExperience());
                    if (expArr.isArray() && !expArr.isEmpty()) {
                        JsonNode exp = expArr.get(0);
                        StringBuilder sb = new StringBuilder();
                        if (exp.hasNonNull("company")) sb.append(exp.get("company").asText());
                        if (exp.hasNonNull("position")) {
                            if (sb.length() > 0) sb.append(" · ");
                            sb.append(exp.get("position").asText());
                        }
                        if (exp.hasNonNull("duration")) {
                            if (sb.length() > 0) sb.append("（");
                            sb.append(exp.get("duration").asText());
                            if (sb.charAt(sb.length() - 1) != '）') sb.append("）");
                        }
                        experience = sb.toString();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析简历信息失败，使用默认内容: {}", e.getMessage());
        }

        StringBuilder sb = new StringBuilder();
        if (name == null || name.isEmpty()) name = "求职者";
        sb.append("📄 简历投递：").append(name).append("\n");
        sb.append("应聘职位相关简历已送达，请查收：\n");
        if (phone != null && !phone.isEmpty()) sb.append("电话：").append(phone).append("\n");
        if (email != null && !email.isEmpty()) sb.append("邮箱：").append(email).append("\n");
        if (location != null && !location.isEmpty()) sb.append("所在地：").append(location).append("\n");
        if (education != null && !education.isEmpty()) sb.append("教育：").append(education).append("\n");
        if (experience != null && !experience.isEmpty()) sb.append("经历：").append(experience).append("\n");
        return sb.toString().trim();
    }

    @Override
    public List<Map<String, Object>> getUserApplicationsWithDetails(Integer userId) {
        return this.baseMapper.getUserApplicationsWithDetails(userId);
    }

    @Override
    public List<Map<String, Object>> getJobApplicantsWithDetails(Integer jobId) {
        return this.baseMapper.getJobApplicantsWithDetails(jobId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateApplicationStatus(Integer applicationId, String status, String feedback) {
        JobApplication application = this.getById(applicationId);
        if (application == null) {
            throw new BusinessException("申请记录不存在");
        }
        if (status == null || !VALID_STATUSES.contains(status)) {
            throw new BusinessException("无效的申请状态: " + status);
        }

        String currentStatus = application.getStatus();
        if (!isAllowedTransition(currentStatus, status)) {
            throw new BusinessException("状态流转不合法: " + currentStatus + " -> " + status);
        }

        application.setStatus(status);
        application.setFeedback(feedback);
        application.setUpdateTime(LocalDateTime.now());

        boolean success = this.updateById(application);
        
        // 状态更新成功后，给求职者发送通知
        if (success) {
            sendNotificationToApplicant(application, status);
        }
        
        return success;
    }

    @Override
    public boolean hasApplied(Integer jobId, Integer userId) {
        QueryWrapper<JobApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("job_id", jobId)
                   .eq("user_id", userId);
        return this.count(queryWrapper) > 0;
    }

    private boolean isAllowedTransition(String currentStatus, String nextStatus) {
        if (currentStatus == null || nextStatus == null) {
            return false;
        }
        if (currentStatus.equals(nextStatus)) {
            return true;
        }
        return switch (currentStatus) {
            case "new" -> "viewed".equals(nextStatus) || "interview".equals(nextStatus) || "rejected".equals(nextStatus);
            case "viewed" -> "interview".equals(nextStatus) || "rejected".equals(nextStatus);
            case "interview" -> "hired".equals(nextStatus) || "rejected".equals(nextStatus);
            case "hired", "rejected" -> false;
            default -> false;
        };
    }
    
    /**
     * 给求职者发送状态变更通知
     */
    private void sendNotificationToApplicant(JobApplication application, String newStatus) {
        try {
            // 获取职位信息
            Job job = jobMapper.selectById(application.getJobId());
            String jobTitle = job != null ? job.getTitle() : "未知职位";
            
            // 根据状态构建通知内容
            String notificationContent = buildNotificationContent(newStatus, jobTitle, application.getFeedback());
            
            // 构建通知消息
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "notification");
            notification.put("message", notificationContent);
            
            Map<String, Object> data = new HashMap<>();
            data.put("applicationId", application.getId());
            data.put("jobId", application.getJobId());
            data.put("jobTitle", jobTitle);
            data.put("newStatus", newStatus);
            data.put("statusText", getStatusText(newStatus));
            data.put("feedback", application.getFeedback());
            data.put("sendTime", LocalDateTime.now().toString());
            notification.put("data", data);
            
            // 通过 WebSocket 发送给求职者
            String userIdStr = String.valueOf(application.getUserId());
            chatWebSocketHandler.sendMessageToUser("user", userIdStr, notification);
            
            log.info("已发送通知给求职者 {}: {}", application.getUserId(), notificationContent);
        } catch (Exception e) {
            log.error("发送通知失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 构建通知内容
     */
    private String buildNotificationContent(String status, String jobTitle, String feedback) {
        switch (status) {
            case "viewed":
                return "您的「" + jobTitle + "」职位申请已被查看，请耐心等待后续通知。";
            case "interview":
                String interviewMsg = "恭喜！您已通过「" + jobTitle + "」的简历筛选，请留意面试安排通知。";
                if (feedback != null && !feedback.isEmpty()) {
                    interviewMsg += " HR备注：" + feedback;
                }
                return interviewMsg;
            case "hired":
                String hiredMsg = "🎉 恭喜您！您已被「" + jobTitle + "」正式录用！";
                if (feedback != null && !feedback.isEmpty()) {
                    hiredMsg += " HR备注：" + feedback;
                }
                return hiredMsg;
            case "rejected":
                String rejectMsg = "很遗憾，您未通过「" + jobTitle + "」的筛选。";
                if (feedback != null && !feedback.isEmpty()) {
                    rejectMsg += " 原因：" + feedback;
                } else {
                    rejectMsg += " 感谢您的参与，祝您早日找到心仪的工作！";
                }
                return rejectMsg;
            default:
                return "您的「" + jobTitle + "」申请状态已更新为：" + getStatusText(status);
        }
    }
    
    /**
     * 获取状态中文描述
     */
    private String getStatusText(String status) {
        switch (status) {
            case "new": return "新申请";
            case "viewed": return "已查看";
            case "interview": return "面试中";
            case "hired": return "已录用";
            case "rejected": return "已拒绝";
            default: return status;
        }
    }
}
