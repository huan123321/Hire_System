package Hiresys.service.impl;

import Hiresys.entity.Job;
import Hiresys.entity.JobApplication;
import Hiresys.entity.User;
import Hiresys.exception.BusinessException;
import Hiresys.mapper.JobMapper;
import Hiresys.service.IJobApplicationService;
import Hiresys.service.IJobService;
import Hiresys.service.IUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 职位表 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-09-19
 */
@Service
public class JobServiceImpl extends ServiceImpl<JobMapper, Job> implements IJobService {
    
    @Autowired
    private IJobApplicationService jobApplicationService;
    
    @Autowired
    private IUserService userService;
    
    @Override
    public IPage<Job> getJobsByPage(Integer page, Integer size, String keyword, String city) {
        // 创建分页对象
        Page<Job> jobPage = new Page<>(page, size);
        
        // 创建查询条件
        QueryWrapper<Job> queryWrapper = new QueryWrapper<>();
        
        // 添加搜索条件
        if (keyword != null && !keyword.isEmpty()) {
            //select * from job where title like '%keyword%'
            queryWrapper.like("title", keyword);
        }
        
        if (city != null && !city.isEmpty()) {
            queryWrapper.eq("city", city);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc("create_time");
        
        // 执行分页查询
        return this.baseMapper.selectPage(jobPage, queryWrapper);
    }
    
    @Override
    public List<Job> filterJobs(String salary, String experience, String education) {
        // 创建查询条件
        QueryWrapper<Job> queryWrapper = new QueryWrapper<>();
        
        if (salary != null && !salary.isEmpty()) {
            queryWrapper.like("salary", salary);
        }
        
        if (experience != null && !experience.isEmpty()) {
            queryWrapper.eq("experience", experience);
        }
        
        if (education != null && !education.isEmpty()) {
            queryWrapper.eq("education", education);
        }
        
        return this.list(queryWrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Job createJob(Job job) {
        // 验证必填字段
        if (job.getTitle() == null || job.getTitle().isEmpty()) {
            throw new BusinessException("职位名称不能为空");
        }
        
        // 设置创建时间和更新时间
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());
        
        // 保存职位
        boolean success = this.save(job);
        
        if (!success) {
            throw new BusinessException("职位发布失败");
        }
        
        return job;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Job updateJob(Long id, Job job) {
        // 检查职位是否存在
        Job existingJob = this.getById(id);
        if (existingJob == null) {
            throw new BusinessException("职位不存在");
        }
        
        // 设置 ID 和更新时间
        job.setId(id);
        job.setUpdateTime(LocalDateTime.now());
        
        // 更新职位
        boolean success = this.updateById(job);
        
        if (!success) {
            throw new BusinessException("职位更新失败");
        }
        
        return job;
    }
    
    @Override
    public List<Job> getJobsByCompany(String companyName) {
        // 创建查询条件
        QueryWrapper<Job> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("company_name", companyName);
        
        return this.list(queryWrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteJob(Long id) {
        // 检查职位是否存在
        Job existingJob = this.getById(id);
        if (existingJob == null) {
            throw new BusinessException("职位不存在");
        }
        
        return this.removeById(id);
    }
    
    @Override
    public List<Map<String, Object>> getJobCandidatesWithDetails(Long jobId) {
        // 1. 查询该职位的所有申请记录
        QueryWrapper<JobApplication> applicationQuery = new QueryWrapper<>();
        applicationQuery.eq("job_id", jobId)
                       .orderByDesc("apply_time");
        
        List<JobApplication> applications = jobApplicationService.list(applicationQuery);
        
        // 2. 构建返回结果
        List<Map<String, Object>> candidates = new ArrayList<>();
        
        for (JobApplication application : applications) {
            Map<String, Object> candidate = new HashMap<>();
            
            // 获取申请者信息
            User user = userService.getById(application.getUserId());
            if (user != null) {
                user.setPassword(null);
            }
            
            // 组装数据
            candidate.put("application", application);
            candidate.put("user", user);
            
            candidates.add(candidate);
        }
        
        return candidates;
    }
    
    @Override
    public Job getJobById(Long id) {
        // 根据 ID 获取职位
        return this.getById(id);
    }
}
