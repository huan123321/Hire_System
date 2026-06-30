package Hiresys.service;

import Hiresys.entity.UserFavoriteJob;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface IUserFavoriteJobService extends IService<UserFavoriteJob> {

    boolean favorite(Integer userId, Long jobId);

    boolean unfavorite(Integer userId, Long jobId);

    boolean isFavorited(Integer userId, Long jobId);

    List<Long> getFavoriteJobIds(Integer userId);

    List<UserFavoriteJob> getFavorites(Integer userId);
}
