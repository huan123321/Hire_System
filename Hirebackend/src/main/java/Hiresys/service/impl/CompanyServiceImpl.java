package Hiresys.service.impl;

import Hiresys.entity.Company;
import Hiresys.entity.User;
import Hiresys.exception.BusinessException;
import Hiresys.mapper.CompanyMapper;
import Hiresys.mapper.UserMapper;
import Hiresys.service.ICompanyService;
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
import java.util.List;

/**
 * <p>
 * 公司表 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-09-19
 */
@Service
public class CompanyServiceImpl extends ServiceImpl<CompanyMapper, Company> implements ICompanyService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Cacheable(value = "company", key = "#id", unless = "#result == null")
    public Company getById(java.io.Serializable id) {
        return super.getById(id);
    }

    @Override
    public IPage<Company> getCompaniesByPage(Integer page, Integer size, String keyword, String industry) {
        // 创建分页对象
        Page<Company> companyPage = new Page<>(page, size);
        
        // 创建查询条件
        QueryWrapper<Company> queryWrapper = new QueryWrapper<>();
        
        // 添加搜索条件
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like("name", keyword);
        }
        
        if (industry != null && !industry.isEmpty()) {
            queryWrapper.eq("industry", industry);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc("create_time");
        
        // 执行分页查询
        return this.page(companyPage, queryWrapper);
    }
    
    @Override
    public List<Company> filterCompanies(String scale, String industry, String location) {
        // 创建查询条件
        QueryWrapper<Company> queryWrapper = new QueryWrapper<>();
        
        if (scale != null && !scale.isEmpty()) {
            queryWrapper.eq("scale", scale);
        }
        
        if (industry != null && !industry.isEmpty()) {
            queryWrapper.eq("industry", industry);
        }

        if (location != null && !location.isEmpty()) {
            queryWrapper.like("address", location);
        }
        
        return this.list(queryWrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "company", key = "#result.id", beforeInvocation = false)
    public Company addCompany(Company company) {
        // 验证必填字段
        if (company.getName() == null || company.getName().isEmpty()) {
            throw new BusinessException("公司名称不能为空");
        }
        
        // 设置创建时间和更新时间
        company.setCreateTime(LocalDateTime.now());
        company.setUpdateTime(LocalDateTime.now());
        
        // 保存公司
        boolean success = this.save(company);
        
        if (!success) {
            throw new BusinessException("添加公司失败");
        }
        
        return company;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "company", key = "#id", beforeInvocation = false)
    public Company updateCompany(Long id, Company company) {
        // 检查公司是否存在
        Company existingCompany = this.getById(id);
        if (existingCompany == null) {
            throw new BusinessException("公司不存在");
        }
        
        // 设置 ID 和更新时间
        company.setId(id);
        company.setUpdateTime(LocalDateTime.now());
        
        // 更新公司
        boolean success = this.updateById(company);
        
        if (!success) {
            throw new BusinessException("更新公司失败");
        }
        
        return company;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "company", key = "#id")
    public boolean deleteCompany(Long id) {
        // 检查公司是否存在
        Company existingCompany = this.getById(id);
        if (existingCompany == null) {
            throw new BusinessException("公司不存在");
        }
        
        return this.removeById(id);
    }

    @Override
    public User getCompanyHr(Long companyId) {
        User hr = userMapper.selectOne(new QueryWrapper<User>()
                .eq("role", 1)
                .eq("company_id", companyId)
                .eq("is_deleted", 0)
                .last("LIMIT 1"));
        if (hr == null) {
            return null;
        }
        // 补充公司名称（companyName 为非存储字段）
        Company company = this.getById(companyId);
        if (company != null) {
            hr.setCompanyName(company.getName());
        }
        return hr;
    }
}