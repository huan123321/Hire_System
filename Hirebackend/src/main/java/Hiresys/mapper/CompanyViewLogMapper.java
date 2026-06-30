package Hiresys.mapper;

import Hiresys.entity.CompanyViewLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 公司浏览记录表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2025-04-08
 */
@Mapper
public interface CompanyViewLogMapper extends BaseMapper<CompanyViewLog> {

    /**
     * 统计指定公司的浏览量
     * @param companyId 公司ID
     * @return 浏览量
     */
    @Select("SELECT COUNT(*) FROM company_view_log WHERE company_id = #{companyId}")
    Long countByCompanyId(@Param("companyId") Long companyId);

    /**
     * 检查用户是否已浏览过该公司（用于防刷）
     * @param companyId 公司ID
     * @param userId 用户ID
     * @return 浏览记录
     */
    @Select("SELECT * FROM company_view_log WHERE company_id = #{companyId} AND user_id = #{userId} ORDER BY view_time DESC LIMIT 1")
    CompanyViewLog findLatestByUserAndCompany(@Param("companyId") Long companyId, @Param("userId") Integer userId);
}
