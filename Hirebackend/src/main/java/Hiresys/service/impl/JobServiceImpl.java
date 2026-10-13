package Hiresys.service.impl;

import Hiresys.entity.Job;
import Hiresys.entity.JobApplication;
import Hiresys.entity.Company;
import Hiresys.entity.User;
import Hiresys.exception.BusinessException;
import Hiresys.mapper.JobMapper;
import Hiresys.mapper.CompanyMapper;
import Hiresys.service.IJobApplicationService;
import Hiresys.service.IJobService;
import Hiresys.service.IUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    private CompanyMapper companyMapper;

    /**
     * 按公司名批量填充职位对应的公司头像。
     * <p>
     * 优化前：循环内逐个 {@code selectOne} —— 典型的 N+1（一页 10 条职位 = 10 次查询）；
     * 优化后：先去重收集公司名，再用一次 {@code IN} 批量查询 + Map 映射回填，
     * 查询次数固定为 1，与职位条数无关。
     */
    private void fillCompanyLogo(List<Job> jobs) {
        if (jobs == null || jobs.isEmpty()) return;

        // 1. 收集去重后的公司名（跳过空值）
        Set<String> companyNames = jobs.stream()
                .map(Job::getCompanyName)
                .filter(name -> name != null && !name.isEmpty())
                .collect(Collectors.toSet());
        if (companyNames.isEmpty()) return;

        // 2. 一次批量查询（替代原先的 N 次 selectOne）
        List<Company> companies = companyMapper.selectList(
                new QueryWrapper<Company>().in("name", companyNames));
        if (companies == null || companies.isEmpty()) return;

        // 3. 建立 公司名 -> logo 映射
        Map<String, String> logoMap = companies.stream()
                .filter(c -> c.getName() != null && c.getLogo() != null && !c.getLogo().isEmpty())
                .collect(Collectors.toMap(Company::getName, Company::getLogo, (a, b) -> a));

        // 4. 回填到职位
        for (Job job : jobs) {
            String logo = logoMap.get(job.getCompanyName());
            if (logo != null) {
                job.setCompanyLogo(logo);
            }
        }
    }
    
    /**
     * 分页查询职位（带 Redis 缓存）
     * <p>
     * 缓存设计（A/B 对比压测用）：
     * <ul>
     *   <li>key 含全部查询参数，避免不同条件互相污染</li>
     *   <li>condition 只缓存前 3 页（每页 10 条）：分页组合多，全量缓存会导致 key 爆炸；
     *       而真实流量集中在首页/前几页，缓存热点页收益最高</li>
     *   <li>unless 空结果不缓存，防缓存穿透</li>
     *   <li>职位增删改（含上下架）时通过 @CacheEvict allEntries 整体失效，保证一致</li>
     * </ul>
     */
    @Override
    @Cacheable(value = "jobList",
            key = "#page + ':' + #size + ':' + (#keyword ?: '') + ':' + (#city ?: '')",
            condition = "#page <= 3 && #size == 10",
            unless = "#result == null")
    public IPage<Job> getJobsByPage(Integer page, Integer size, String keyword, String city) {
        // 创建分页对象
        Page<Job> jobPage = new Page<>(page, size);
        
        // 创建查询条件
        QueryWrapper<Job> queryWrapper = new QueryWrapper<>();
        
        // 求职者端只展示招聘中的职位
        queryWrapper.eq("status", "active");
        
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
        IPage<Job> result = this.baseMapper.selectPage(jobPage, queryWrapper);
        fillCompanyLogo(result.getRecords());
        return result;
    }
    
    @Override
    public List<Job> filterJobs(String salary, String experience, String education) {
        // 创建查询条件
        QueryWrapper<Job> queryWrapper = new QueryWrapper<>();
        
        // 求职者端只展示招聘中的职位
        queryWrapper.eq("status", "active");
        
        if (salary != null && !salary.isEmpty()) {
            queryWrapper.like("salary", salary);
        }
        
        if (experience != null && !experience.isEmpty()) {
            queryWrapper.eq("experience", experience);
        }
        
        if (education != null && !education.isEmpty()) {
            queryWrapper.eq("education", education);
        }
        
        List<Job> result = this.list(queryWrapper);
        fillCompanyLogo(result);
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "jobList", allEntries = true)
    public Job createJob(Job job) {
        // 验证必填字段
        if (job.getTitle() == null || job.getTitle().isEmpty()) {
            throw new BusinessException("职位名称不能为空");
        }
        
        // 新发布的职位默认招聘中
        if (job.getStatus() == null || job.getStatus().isEmpty()) {
            job.setStatus("active");
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
    @CacheEvict(value = "jobList", allEntries = true)
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
        
        List<Job> result = this.list(queryWrapper);
        fillCompanyLogo(result);
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "jobList", allEntries = true)
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
        Job job = this.getById(id);
        if (job != null) {
            List<Job> one = new ArrayList<>();
            one.add(job);
            fillCompanyLogo(one);
        }
        return job;
    }
}
