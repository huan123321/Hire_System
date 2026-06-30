package Hiresys.service.impl;

import Hiresys.entity.Company;
import Hiresys.entity.UserFollowCompany;
import Hiresys.mapper.UserFollowCompanyMapper;
import Hiresys.service.ICompanyService;
import Hiresys.service.IUserFollowCompanyService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户关注公司关系表 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-09-27
 */
@Service
public class UserFollowCompanyServiceImpl extends ServiceImpl<UserFollowCompanyMapper, UserFollowCompany> implements IUserFollowCompanyService {

    @Autowired
    private ICompanyService companyService;

    @Override
    public boolean followCompany(Integer userId, Long companyId) {
        // 检查是否已经关注
        if (isFollowing(userId, companyId)) {
            return false;
        }
        
        // 创建关注关系
        UserFollowCompany followRelation = new UserFollowCompany();
        followRelation.setUserId(userId);
        followRelation.setCompanyId(companyId);
        
        return this.save(followRelation);
    }

    @Override
    public boolean unfollowCompany(Integer userId, Long companyId) {
        // 查询关注关系
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserFollowCompany> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("company_id", companyId);
        
        return this.remove(queryWrapper);
    }

    @Override
    public boolean isFollowing(Integer userId, Long companyId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserFollowCompany> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("company_id", companyId);
        
        return this.count(queryWrapper) > 0;
    }

    @Override
    public List<Company> getFollowedCompanies(Integer userId) {
        // 查询用户关注的所有公司 ID
        QueryWrapper<UserFollowCompany> followQuery = new QueryWrapper<>();
        followQuery.eq("user_id", userId)
                  .select("company_id");
        
        List<UserFollowCompany> followRelations = this.list(followQuery);
        if (followRelations == null || followRelations.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 提取公司 ID 列表
        List<Long> companyIds = followRelations.stream()
            .map(UserFollowCompany::getCompanyId)
            .collect(Collectors.toList());
        
        // 查询公司信息
        QueryWrapper<Company> companyQuery = new QueryWrapper<>();
        companyQuery.in("id", companyIds);
        
        return companyService.list(companyQuery);
    }
}