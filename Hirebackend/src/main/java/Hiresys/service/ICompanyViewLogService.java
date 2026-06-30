package Hiresys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import Hiresys.entity.CompanyViewLog;

/**
 * <p>
 * 公司浏览记录表 服务类
 * </p>
 *
 * @author author
 * @since 2025-04-08
 */
public interface ICompanyViewLogService extends IService<CompanyViewLog> {

    /**
     * 记录公司浏览
     * @param companyId 公司ID
     * @param userId 用户ID（可为null）
     * @param ipAddress IP地址
     * @return 是否记录成功
     */
    boolean recordView(Long companyId, Integer userId, String ipAddress);

    /**
     * 获取公司浏览量
     * @param companyId 公司ID
     * @return 浏览量
     */
    Long getViewCount(Long companyId);

    /**
     * 检查用户是否已浏览过该公司（用于防刷）
     * @param companyId 公司ID
     * @param userId 用户ID
     * @return true-已浏览过，false-未浏览过
     */
    boolean hasViewed(Long companyId, Integer userId);
}
