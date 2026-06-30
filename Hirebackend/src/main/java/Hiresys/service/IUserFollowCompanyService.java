package Hiresys.service;

import Hiresys.entity.UserFollowCompany;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户关注公司关系表 服务类
 * </p>
 *
 * @author author
 * @since 2025-09-27
 */
public interface IUserFollowCompanyService extends IService<UserFollowCompany> {

    /**
     * 用户关注公司
     * @param userId 用户 ID
     * @param companyId 公司 ID
     * @return 是否关注成功
     */
    boolean followCompany(Integer userId, Long companyId);

    /**
     * 用户取消关注公司
     * @param userId 用户 ID
     * @param companyId 公司 ID
     * @return 是否取消成功
     */
    boolean unfollowCompany(Integer userId, Long companyId);

    /**
     * 检查用户是否关注了某公司
     * @param userId 用户 ID
     * @param companyId 公司 ID
     * @return true-已关注，false-未关注
     */
    boolean isFollowing(Integer userId, Long companyId);

    /**
     * 获取用户关注的所有公司
     * @param userId 用户 ID
     * @return 公司列表
     */
    java.util.List<Hiresys.entity.Company> getFollowedCompanies(Integer userId);
}