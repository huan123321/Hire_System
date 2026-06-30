package Hiresys.service;

import Hiresys.entity.JobApplication;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 职位申请记录表 服务类
 * </p>
 *
 * @author author
 * @since 2025-09-27
 */
public interface IJobApplicationService extends IService<JobApplication> {

    /**
     * 申请职位
     * @param jobId 职位 ID
     * @param userId 用户 ID
     * @param resumeId 简历 ID（可选）
     * @return 申请记录 ID
     * @throws IllegalArgumentException 如果已经申请过该职位
     */
    Integer applyForPosition(Integer jobId, Integer userId, Integer resumeId);

    /**
     * 获取用户申请的职位列表（包含职位详情）
     * @param userId 用户 ID
     * @return 申请记录列表，每条记录包含 application 和 job 信息
     */
    List<Map<String, Object>> getUserApplicationsWithDetails(Integer userId);

    /**
     * 获取职位的申请者列表（包含申请者详细信息）
     * @param jobId 职位 ID
     * @return 申请者列表，包含用户和简历信息
     */
    List<Map<String, Object>> getJobApplicantsWithDetails(Integer jobId);

    /**
     * 更新申请状态
     * @param applicationId 申请记录 ID
     * @param status 新状态（new/viewed/interview/hired/rejected）
     * @param feedback 反馈信息
     * @return 是否更新成功
     */
    boolean updateApplicationStatus(Integer applicationId, String status, String feedback);

    /**
     * 检查用户是否已经申请过某个职位
     * @param jobId 职位 ID
     * @param userId 用户 ID
     * @return true-已申请，false-未申请
     */
    boolean hasApplied(Integer jobId, Integer userId);
}
