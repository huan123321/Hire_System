package Hiresys.service.impl;

import Hiresys.entity.CompanyViewLog;
import Hiresys.mapper.CompanyViewLogMapper;
import Hiresys.service.ICompanyViewLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 * 公司浏览记录表 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-04-08
 */
@Slf4j
@Service
public class CompanyViewLogServiceImpl extends ServiceImpl<CompanyViewLogMapper, CompanyViewLog> implements ICompanyViewLogService {

    /**
     * 记录公司浏览
     * @param companyId 公司ID
     * @param userId 用户ID（可为null）
     * @param ipAddress IP地址
     * @return 是否记录成功
     */
    @Override
    public boolean recordView(Long companyId, Integer userId, String ipAddress) {
        try {
            // 防刷机制：同一用户在5分钟内只记录一次浏览
            if (userId != null && hasViewedRecently(companyId, userId, 5)) {
                return true; // 5分钟内已浏览过，不重复记录
            }

            // 创建浏览记录
            CompanyViewLog viewLog = new CompanyViewLog();
            viewLog.setCompanyId(companyId);
            viewLog.setUserId(userId);
            viewLog.setIpAddress(ipAddress);
            viewLog.setViewTime(LocalDateTime.now());

            // 保存到数据库
            return this.save(viewLog);
        } catch (Exception e) {
            log.error("记录公司浏览失败: companyId={}", companyId, e);
            return false;
        }
    }

    @Override
    public Long getViewCount(Long companyId) {
        try {
            Long count = baseMapper.countByCompanyId(companyId);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("获取浏览量失败: companyId={}", companyId, e);
            return 0L;
        }
    }

    /**
     * 检查用户是否已浏览过该公司
     * @param companyId 公司ID
     * @param userId 用户ID
     * @return true-已浏览过，false-未浏览过
     */
    @Override
    public boolean hasViewed(Long companyId, Integer userId) {
        if (userId == null) {
            return false; // 未登录用户无法判断
        }
        
        CompanyViewLog latestView = baseMapper.findLatestByUserAndCompany(companyId, userId);
        return latestView != null;
    }

    /**
     * 检查用户是否在指定时间内浏览过该公司（防刷）
     * @param companyId 公司ID
     * @param userId 用户ID
     * @param minutes 时间范围（分钟）
     * @return true-在指定时间内浏览过，false-未浏览过
     */
    private boolean hasViewedRecently(Long companyId, Integer userId, int minutes) {
        if (userId == null) {
            return false;
        }
        
        CompanyViewLog latestView = baseMapper.findLatestByUserAndCompany(companyId, userId);
        if (latestView == null) {
            return false;
        }
        
        // 计算时间差
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime viewTime = latestView.getViewTime();
        long minutesDiff = java.time.Duration.between(viewTime, now).toMinutes();
        
        return minutesDiff < minutes; // 如果在指定时间内浏览过，返回true
    }
}
