package Hiresys.service;

import Hiresys.entity.Resume;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户简历表 服务类
 * </p>
 *
 * @author author
 * @since 2025-09-27
 */
public interface IResumeService extends IService<Resume> {
    
    /**
     * 保存或更新用户简历
     * @param resume 简历信息
     * @return 保存后的简历信息
     */
    Resume saveResume(Resume resume);
    
    /**
     * 获取用户的所有简历
     * @param userId 用户 ID
     * @return 用户简历列表
     */
    List<Resume> getResumesByUserId(Integer userId);
    
    /**
     * 获取用户默认简历
     * @param userId 用户 ID
     * @return 用户默认简历
     */
    Resume getDefaultResume(Integer userId);
    
    /**
     * 设置默认简历
     * @param resumeId 简历 ID
     * @param userId 用户 ID
     * @return 是否设置成功
     */
    boolean setDefaultResume(Long resumeId, Integer userId);
    
    /**
     * 删除简历
     * @param resumeId 简历 ID
     * @return 是否删除成功
     */
    boolean deleteResume(Long resumeId);
}