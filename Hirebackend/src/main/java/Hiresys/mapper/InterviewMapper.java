package Hiresys.mapper;

import Hiresys.entity.Interview;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 面试安排 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-04-08
 */
@Mapper
public interface InterviewMapper extends BaseMapper<Interview> {

    /**
     * 查询用户的面试列表（包含职位和公司信息）
     * @param userId 用户ID
     * @return 面试列表
     */
    List<Interview> selectInterviewsWithDetails(@Param("userId") Integer userId);

    /**
     * 查询公司的面试列表（HR视角）
     * @param companyId 公司ID
     * @return 面试列表
     */
    List<Interview> selectCompanyInterviews(@Param("companyId") Long companyId);

    /**
     * 查询职位的面试列表
     * @param jobId 职位ID
     * @return 面试列表
     */
    List<Interview> selectJobInterviews(@Param("jobId") Integer jobId);
}
