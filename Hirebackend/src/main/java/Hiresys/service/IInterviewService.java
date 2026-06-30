package Hiresys.service;

import Hiresys.entity.Interview;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 面试安排服务接口
 * </p>
 *
 * @author author
 * @since 2026-04-08
 */
public interface IInterviewService extends IService<Interview> {

    /**
     * HR安排面试
     * @param interviewData 面试数据（包含applicationId, interviewTime, interviewType等）
     * @return 安排结果
     */
    Map<String, Object> scheduleInterview(Map<String, Object> interviewData);

    /**
     * 更新面试信息（编辑）
     * @param interviewId 面试ID
     * @param interviewData 更新的面试数据
     * @return 更新结果
     */
    Map<String, Object> updateInterview(Long interviewId, Map<String, Object> interviewData);

    /**
     * 求职者确认/拒绝面试
     * @param interviewId 面试ID
     * @param userId 用户ID
     * @param confirmed 是否确认
     * @return 操作结果
     */
    Map<String, Object> confirmInterview(Long interviewId, Integer userId, Boolean confirmed);

    /**
     * HR更新面试状态
     * @param interviewId 面试ID
     * @param status 新状态
     * @param feedback 反馈信息
     * @param rating 评分
     * @return 更新结果
     */
    Map<String, Object> updateInterviewStatus(Long interviewId, String status, String feedback, Integer rating);

    /**
     * 获取用户的面试列表
     * @param userId 用户ID
     * @return 面试列表
     */
    List<Interview> getUserInterviews(Integer userId);

    /**
     * 获取公司的面试列表（HR视角）
     * @param companyId 公司ID
     * @return 面试列表
     */
    List<Interview> getCompanyInterviews(Long companyId);

    /**
     * 取消面试
     * @param interviewId 面试ID
     * @param userId 用户ID
     * @param reason 取消原因
     * @return 操作结果
     */
    Map<String, Object> cancelInterview(Long interviewId, Integer userId, String reason);
}
