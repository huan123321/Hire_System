package Hiresys.service.impl;

import Hiresys.entity.UserFavoriteJob;
import Hiresys.exception.BusinessException;
import Hiresys.mapper.UserFavoriteJobMapper;
import Hiresys.service.IUserFavoriteJobService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserFavoriteJobServiceImpl extends ServiceImpl<UserFavoriteJobMapper, UserFavoriteJob> implements IUserFavoriteJobService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean favorite(Integer userId, Long jobId) {
        if (isFavorited(userId, jobId)) {
            return false;
        }
        UserFavoriteJob f = new UserFavoriteJob();
        f.setUserId(userId);
        f.setJobId(jobId);
        f.setCreateTime(LocalDateTime.now());
        return this.save(f);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unfavorite(Integer userId, Long jobId) {
        return this.lambdaUpdate()
                .eq(UserFavoriteJob::getUserId, userId)
                .eq(UserFavoriteJob::getJobId, jobId)
                .remove();
    }

    @Override
    public boolean isFavorited(Integer userId, Long jobId) {
        return this.lambdaQuery()
                .eq(UserFavoriteJob::getUserId, userId)
                .eq(UserFavoriteJob::getJobId, jobId)
                .count() > 0;
    }

    @Override
    public List<Long> getFavoriteJobIds(Integer userId) {
        return this.lambdaQuery()
                .eq(UserFavoriteJob::getUserId, userId)
                .list()
                .stream()
                .map(UserFavoriteJob::getJobId)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserFavoriteJob> getFavorites(Integer userId) {
        return this.lambdaQuery()
                .eq(UserFavoriteJob::getUserId, userId)
                .orderByDesc(UserFavoriteJob::getCreateTime)
                .list();
    }
}
