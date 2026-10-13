package Hiresys.service.impl;

import Hiresys.entity.Interview;
import Hiresys.entity.Job;
import Hiresys.entity.JobApplication;
import Hiresys.entity.User;
import Hiresys.exception.BusinessException;
import Hiresys.mapper.InterviewMapper;
import Hiresys.mapper.UserMapper;
import Hiresys.service.IInterviewService;
import Hiresys.service.IJobApplicationService;
import Hiresys.service.IJobService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 面试安排服务实现类
 * </p>
 *
 * @author author
 * @since 2026-04-08
 */
@Slf4j
@Service
public class InterviewServiceImpl extends ServiceImpl<InterviewMapper, Interview> implements IInterviewService {

    @Autowired
    private InterviewMapper interviewMapper;

    @Autowired
    private IJobApplicationService jobApplicationService;

    @Autowired
    private IJobService jobService;

    @Autowired
    private Hiresys.mapper.CompanyMapper companyMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> scheduleInterview(Map<String, Object> interviewData) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 验证必填字段
            Integer applicationId = (Integer) interviewData.get("applicationId");
            if (applicationId == null) {
                throw new BusinessException("申请记录ID不能为空");
            }

            String interviewTimeStr = (String) interviewData.get("interviewTime");
            if (interviewTimeStr == null || interviewTimeStr.isEmpty()) {
                throw new BusinessException("面试时间不能为空");
            }

            String interviewType = (String) interviewData.get("interviewType");
            if (interviewType == null || interviewType.isEmpty()) {
                interviewType = "phone"; // 默认电话面试
            }

            // 2. 验证申请记录是否存在
            JobApplication application = jobApplicationService.getById(applicationId);
            if (application == null) {
                throw new BusinessException("申请记录不存在");
            }

            // 获取职位信息以得到公司ID
            Job job = jobService.getById(application.getJobId());
            if (job == null) {
                throw new BusinessException("职位不存在");
            }

            // 通过公司名称查询公司ID
            Long companyId = null;
            if (job.getCompanyName() != null && !job.getCompanyName().isEmpty()) {
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Hiresys.entity.Company> wrapper = 
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                wrapper.eq("name", job.getCompanyName());
                Hiresys.entity.Company company = companyMapper.selectOne(wrapper);
                if (company != null) {
                    companyId = company.getId();
                }
            }

            // 3. 检查是否已经安排了面试
            Interview existingInterview = this.query()
                    .eq("application_id", applicationId)
                    .in("status", "scheduled", "confirmed")
                    .one();
            if (existingInterview != null) {
                throw new BusinessException("该申请已安排面试，请先取消或完成现有面试");
            }

            // 4. 创建面试记录
            Interview interview = new Interview();
            interview.setApplicationId(applicationId);
            interview.setJobId(application.getJobId());
            interview.setUserId(application.getUserId());
            interview.setInterviewTime(LocalDateTime.parse(interviewTimeStr.replace(" ", "T")));
            interview.setInterviewType(interviewType);
            interview.setLocation((String) interviewData.get("location"));
            interview.setMeetingUrl((String) interviewData.get("meetingUrl"));
            interview.setInterviewer((String) interviewData.get("interviewer"));
            interview.setInterviewerPhone((String) interviewData.get("interviewerPhone"));
            interview.setStatus("scheduled");
            interview.setRemark((String) interviewData.get("remark"));
            interview.setCompanyId(companyId);
            interview.setCreateTime(LocalDateTime.now());
            interview.setUpdateTime(LocalDateTime.now());

            boolean saved = this.save(interview);
            if (!saved) {
                throw new BusinessException("面试安排失败");
            }

            // 5. 更新申请状态为"面试中"
            jobApplicationService.updateApplicationStatus(applicationId, "interview", "已安排面试");

            // 6. 构建返回结果
            result.put("success", true);
            result.put("message", "面试安排成功");
            result.put("interview", interview);

        } catch (BusinessException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            log.error("面试安排失败", e);
            result.put("success", false);
            result.put("message", "面试安排失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateInterview(Long interviewId, Map<String, Object> interviewData) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 查询面试记录
            Interview interview = this.getById(interviewId);
            if (interview == null) {
                throw new BusinessException("面试记录不存在");
            }

            // 2. 检查状态是否允许编辑（已安排或已确认状态可以编辑）
            if (!"scheduled".equals(interview.getStatus()) && !"confirmed".equals(interview.getStatus())) {
                throw new BusinessException("当前状态不允许编辑");
            }

            // 3. 更新面试信息
            String interviewTimeStr = (String) interviewData.get("interviewTime");
            if (interviewTimeStr != null && !interviewTimeStr.isEmpty()) {
                interview.setInterviewTime(LocalDateTime.parse(interviewTimeStr.replace(" ", "T")));
            }

            String interviewType = (String) interviewData.get("interviewType");
            if (interviewType != null && !interviewType.isEmpty()) {
                interview.setInterviewType(interviewType);
            }

            String location = (String) interviewData.get("location");
            if (location != null) {
                interview.setLocation(location);
            }

            String meetingUrl = (String) interviewData.get("meetingUrl");
            if (meetingUrl != null) {
                interview.setMeetingUrl(meetingUrl);
            }

            String interviewer = (String) interviewData.get("interviewer");
            if (interviewer != null) {
                interview.setInterviewer(interviewer);
            }

            String interviewerPhone = (String) interviewData.get("interviewerPhone");
            if (interviewerPhone != null) {
                interview.setInterviewerPhone(interviewerPhone);
            }

            String remark = (String) interviewData.get("remark");
            if (remark != null) {
                interview.setRemark(remark);
            }

            interview.setUpdateTime(LocalDateTime.now());

            boolean updated = this.updateById(interview);
            if (!updated) {
                throw new BusinessException("更新失败");
            }

            result.put("success", true);
            result.put("message", "面试信息更新成功");
            result.put("interview", interview);

        } catch (BusinessException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            log.error("更新面试失败: interviewId={}", interviewId, e);
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> confirmInterview(Long interviewId, Integer userId, Boolean confirmed) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 查询面试记录
            Interview interview = this.getById(interviewId);
            if (interview == null) {
                throw new BusinessException("面试记录不存在");
            }

            // 2. 验证权限
            if (!interview.getUserId().equals(userId)) {
                throw new BusinessException("无权操作此面试记录");
            }

            // 3. 验证状态
            if (!"scheduled".equals(interview.getStatus())) {
                throw new BusinessException("当前状态不允许确认");
            }

            // 4. 更新状态
            if (confirmed) {
                interview.setStatus("confirmed");
                result.put("message", "已确认参加面试");
            } else {
                interview.setStatus("cancelled");
                interview.setRemark("求职者拒绝面试");
                result.put("message", "已拒绝面试邀请");
            }

            interview.setUpdateTime(LocalDateTime.now());
            boolean updated = this.updateById(interview);

            if (!updated) {
                throw new BusinessException("操作失败");
            }

            result.put("success", true);
            result.put("interview", interview);

        } catch (BusinessException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            log.error("确认面试失败: interviewId={}", interviewId, e);
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateInterviewStatus(Long interviewId, String status, String feedback, Integer rating) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 查询面试记录
            Interview interview = this.getById(interviewId);
            if (interview == null) {
                throw new BusinessException("面试记录不存在");
            }

            // 2. 验证状态流转
            validateStatusTransition(interview.getStatus(), status);

            // 3. 更新状态
            interview.setStatus(status);
            if (feedback != null && !feedback.isEmpty()) {
                interview.setFeedback(feedback);
            }
            if (rating != null) {
                interview.setRating(rating);
            }
            interview.setUpdateTime(LocalDateTime.now());

            boolean updated = this.updateById(interview);
            if (!updated) {
                throw new BusinessException("状态更新失败");
            }

            // 4. 如果面试完成或拒绝，更新申请状态
            if ("completed".equals(status) || "no_show".equals(status)) {
                // HR可以根据面试结果决定后续流程
            }

            result.put("success", true);
            result.put("message", "状态更新成功");
            result.put("interview", interview);

        } catch (BusinessException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            log.error("更新面试状态失败: interviewId={}", interviewId, e);
            result.put("success", false);
            result.put("message", "状态更新失败：" + e.getMessage());
        }

        return result;
    }

    @Override
    public List<Interview> getUserInterviews(Integer userId) {
        return interviewMapper.selectInterviewsWithDetails(userId);
    }

    @Override
    public List<Interview> getCompanyInterviews(Long companyId) {
        return interviewMapper.selectCompanyInterviews(companyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> cancelInterview(Long interviewId, Integer userId, String reason) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 查询面试记录
            Interview interview = this.getById(interviewId);
            if (interview == null) {
                throw new BusinessException("面试记录不存在");
            }

            // 2. 验证权限（求职者本人或该公司的 HR 可以取消）
            Integer applicantId = interview.getUserId();
            boolean isApplicant = applicantId != null && applicantId.equals(userId);
            boolean isCompanyHr = false;
            if (!isApplicant && interview.getCompanyId() != null) {
                User operator = userMapper.selectById(userId);
                isCompanyHr = operator != null
                        && Integer.valueOf(1).equals(operator.getRole())
                        && interview.getCompanyId().equals(operator.getCompanyId());
            }
            if (!isApplicant && !isCompanyHr) {
                throw new BusinessException("无权取消该面试");
            }

            // 3. 验证状态
            if ("completed".equals(interview.getStatus()) || "cancelled".equals(interview.getStatus())) {
                throw new BusinessException("当前状态不允许取消");
            }

            // 4. 更新状态
            interview.setStatus("cancelled");
            if (reason != null && !reason.isEmpty()) {
                interview.setRemark(reason);
            }
            interview.setUpdateTime(LocalDateTime.now());

            boolean updated = this.updateById(interview);
            if (!updated) {
                throw new BusinessException("取消失败");
            }

            result.put("success", true);
            result.put("message", "面试已取消");
            result.put("interview", interview);

        } catch (BusinessException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            log.error("取消面试失败: interviewId={}", interviewId, e);
            result.put("success", false);
            result.put("message", "取消失败：" + e.getMessage());
        }

        return result;
    }

    /**
     * 验证状态流转是否合法
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        Map<String, List<String>> validTransitions = new HashMap<>();
        validTransitions.put("scheduled", List.of("confirmed", "cancelled"));
        validTransitions.put("confirmed", List.of("completed", "cancelled", "no_show"));
        validTransitions.put("completed", List.of()); // 终态
        validTransitions.put("cancelled", List.of()); // 终态
        validTransitions.put("no_show", List.of()); // 终态

        List<String> allowedStatuses = validTransitions.get(currentStatus);
        if (allowedStatuses == null || !allowedStatuses.contains(newStatus)) {
            throw new BusinessException("不允许从状态 " + currentStatus + " 转换到 " + newStatus);
        }
    }
}
