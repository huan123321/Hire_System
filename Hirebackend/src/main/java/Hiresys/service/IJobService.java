package Hiresys.service;

import Hiresys.entity.Job;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 职位表 服务类
 * </p>
 *
 * @author author
 * @since 2025-09-19
 */
public interface IJobService extends IService<Job> {
    
    /**
     * 分页查询职位信息
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param city 城市
     * @return 分页结果
     */
    IPage<Job> getJobsByPage(Integer page, Integer size, String keyword, String city);
    
    /**
     * 根据条件筛选职位
     * @param salary 薪资范围
     * @param experience 经验要求
     * @param education 学历要求
     * @return 符合条件的职位列表
     */
    List<Job> filterJobs(String salary, String experience, String education);
    
    /**
     * 创建新职位
     * @param job 职位信息
     * @return 创建后的职位信息
     */
    Job createJob(Job job);
    
    /**
     * 更新职位信息
     * @param id 职位 ID
     * @param job 更新的职位信息
     * @return 更新后的职位信息
     */
    Job updateJob(Long id, Job job);
    
    /**
     * 获取 HR 发布的职位列表
     * @param companyName 公司名称
     * @return 该公司发布的职位列表
     */
    List<Job> getJobsByCompany(String companyName);
    
    /**
     * 删除职位
     * @param id 职位 ID
     * @return 是否删除成功
     */
    boolean deleteJob(Long id);
    
    /**
     * 获取职位的申请者列表（包含详细信息）
     * @param jobId 职位 ID
     * @return 候选人列表，每个候选人包含 application 和 user 信息
     */
    List<Map<String, Object>> getJobCandidatesWithDetails(Long jobId);
    
    /**
     * 根据 ID 获取职位详情
     * @param id 职位 ID
     * @return 职位详情
     */
    Job getJobById(Long id);
}