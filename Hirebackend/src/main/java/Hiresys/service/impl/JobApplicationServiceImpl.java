package Hiresys.service.impl;

import Hiresys.entity.Job;
import Hiresys.entity.JobApplication;
import Hiresys.entity.Resume;
import Hiresys.exception.BusinessException;
import Hiresys.mapper.JobApplicationMapper;
import Hiresys.mapper.JobMapper;
import Hiresys.service.IHotJobService;
import Hiresys.service.IJobApplicationService;
import Hiresys.service.IResumeService;
import Hiresys.websocket.ChatWebSocketHandler;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

        return application.getId();
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
