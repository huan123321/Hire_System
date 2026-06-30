package Hiresys.service.impl;

import Hiresys.entity.Resume;
import Hiresys.exception.BusinessException;
import Hiresys.mapper.ResumeMapper;
import Hiresys.service.IResumeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 用户简历表 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-09-27
 */
@Service
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, Resume> implements IResumeService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Resume saveResume(Resume resume) {
        try {
            if (resume.getId() != null && resume.getId() > 0) {
                // 更新现有简历
                resume.setUpdateTime(LocalDateTime.now());
                boolean success = this.updateById(resume);
                if (!success) {
                    throw new BusinessException("简历更新失败");
                }
            } else {
                // 创建新简历
                // 如果这是用户的第一份简历，设为默认简历
                QueryWrapper<Resume> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("user_id", resume.getUserId());
                long count = this.count(queryWrapper);
                if (count == 0) {
                    resume.setIsDefault(true);
                }
                resume.setCreateTime(LocalDateTime.now());
                resume.setUpdateTime(LocalDateTime.now());
                boolean success = this.save(resume);
                if (!success) {
                    throw new BusinessException("简历保存失败");
                }
            }
            return resume;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("简历保存失败：" + e.getMessage());
        }
    }
    
    @Override
    public List<Resume> getResumesByUserId(Integer userId) {
        // 创建查询条件
        QueryWrapper<Resume> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("is_default"); // 默认简历排在前面
        queryWrapper.orderByDesc("update_time"); // 按更新时间排序
        return this.list(queryWrapper);
    }
    
    @Override
    public Resume getDefaultResume(Integer userId) {
        // 先尝试获取默认简历
        QueryWrapper<Resume> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("is_default", true);
        Resume resume = this.getOne(queryWrapper);
        
        // 如果没有默认简历，获取最新创建的简历
        if (resume == null) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.orderByDesc("create_time");
            queryWrapper.last("LIMIT 1");
            resume = this.getOne(queryWrapper);
        }
        
        return resume;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultResume(Long resumeId, Integer userId) {
        try {
            // 先将用户的所有简历设为非默认
            QueryWrapper<Resume> updateWrapper = new QueryWrapper<>();
            updateWrapper.eq("user_id", userId);
            Resume resetResume = new Resume();
            resetResume.setIsDefault(false);
            this.update(resetResume, updateWrapper);
            
            // 再将指定简历设为默认
            Resume resume = new Resume();
            resume.setId(resumeId);
            resume.setIsDefault(true);
            boolean success = this.updateById(resume);
            
            return success;
        } catch (Exception e) {
            throw new BusinessException("默认简历设置失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteResume(Long resumeId) {
        // 检查简历是否存在
        Resume existingResume = this.getById(resumeId);
        if (existingResume == null) {
            throw new BusinessException("简历不存在");
        }
        
        return this.removeById(resumeId);
    }
}