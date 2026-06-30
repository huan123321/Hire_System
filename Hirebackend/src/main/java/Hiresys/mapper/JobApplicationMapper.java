package Hiresys.mapper;

import Hiresys.entity.JobApplication;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 职位申请记录表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2025-09-27
 */
@Mapper
public interface JobApplicationMapper extends BaseMapper<JobApplication> {

    /**
     * 获取用户申请的职位列表（包含职位详情）
     * @param userId 用户 ID
     * @return 申请记录列表，包含职位信息
     */
    @Select("SELECT " +
            "ja.id AS id, " +
            "ja.job_id AS jobId, " +
            "ja.user_id AS userId, " +
            "ja.resume_id AS resumeId, " +
            "ja.status AS status, " +
            "ja.feedback AS feedback, " +
            "ja.apply_time AS applyTime, " +
            "ja.update_time AS updateTime, " +
            "j.title AS title, " +
            "j.salary AS salary, " +
            "j.city AS city, " +
            "j.experience AS experience, " +
            "j.education AS education, " +
            "j.company_name AS companyName " +
            "FROM job_application ja " +
            "LEFT JOIN job j ON ja.job_id = j.id " +
            "WHERE ja.user_id = #{userId} " +
            "ORDER BY ja.apply_time DESC")
    List<Map<String, Object>> getUserApplicationsWithDetails(@Param("userId") Integer userId);

    /**
     * 获取职位的所有申请者列表（包含申请者信息）
     * @param jobId 职位 ID
     * @return 申请者列表，包含用户信息
     */
    @Select("SELECT " +
            "ja.id AS id, " +
            "ja.job_id AS jobId, " +
            "ja.user_id AS userId, " +
            "ja.resume_id AS resumeId, " +
            "ja.status AS status, " +
            "ja.feedback AS feedback, " +
            "ja.apply_time AS applyTime, " +
            "ja.update_time AS updateTime, " +
            "u.username AS username, " +
            "u.real_name AS realName, " +
            "u.phone AS phone, " +
            "u.email AS email, " +
            "r.name AS resumeName, " +
            "r.education AS resumeEducation, " +
            "r.experience AS resumeExperience " +
            "FROM job_application ja " +
            "LEFT JOIN user u ON ja.user_id = u.id " +
            "LEFT JOIN resume r ON ja.resume_id = r.id " +
            "WHERE ja.job_id = #{jobId} " +
            "ORDER BY ja.apply_time DESC")
    List<Map<String, Object>> getJobApplicantsWithDetails(@Param("jobId") Integer jobId);
}
