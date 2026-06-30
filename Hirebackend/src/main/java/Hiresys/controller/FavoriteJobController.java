package Hiresys.controller;

import Hiresys.dto.Result;
import Hiresys.entity.Job;
import Hiresys.entity.UserFavoriteJob;
import Hiresys.service.IJobService;
import Hiresys.service.IUserFavoriteJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "职位收藏", description = "求职者收藏/取消收藏职位、查看收藏列表")
@RestController
@RequestMapping("/favorite")
public class FavoriteJobController {

    @Autowired
    private IUserFavoriteJobService favoriteJobService;

    @Autowired
    private IJobService jobService;

    @Operation(summary = "收藏职位")
    @PostMapping("/add")
    public Result<Void> add(@RequestBody Map<String, Object> req) {
        Integer userId = ((Number) req.get("userId")).intValue();
        Long jobId = ((Number) req.get("jobId")).longValue();
        boolean ok = favoriteJobService.favorite(userId, jobId);
        return ok ? Result.ok("收藏成功", null) : Result.fail("已收藏");
    }

    @Operation(summary = "取消收藏")
    @PostMapping("/remove")
    public Result<Void> remove(@RequestBody Map<String, Object> req) {
        Integer userId = ((Number) req.get("userId")).intValue();
        Long jobId = ((Number) req.get("jobId")).longValue();
        favoriteJobService.unfavorite(userId, jobId);
        return Result.ok("已取消收藏", null);
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam Integer userId, @RequestParam Long jobId) {
        return Result.ok(favoriteJobService.isFavorited(userId, jobId));
    }

    @Operation(summary = "获取用户收藏的职位ID列表")
    @GetMapping("/ids/{userId}")
    public Result<List<Long>> getFavoriteIds(@PathVariable Integer userId) {
        return Result.ok(favoriteJobService.getFavoriteJobIds(userId));
    }

    @Operation(summary = "获取用户收藏列表（含职位详情）")
    @GetMapping("/list/{userId}")
    public Result<List<Map<String, Object>>> getFavorites(@PathVariable Integer userId) {
        List<UserFavoriteJob> favs = favoriteJobService.getFavorites(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserFavoriteJob fav : favs) {
            Job job = jobService.getById(fav.getJobId());
            if (job != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", fav.getId());
                item.put("jobId", fav.getJobId());
                item.put("jobTitle", job.getTitle());
                item.put("companyName", job.getCompanyName());
                item.put("salary", job.getSalary());
                item.put("createTime", fav.getCreateTime().toString());
                result.add(item);
            }
        }
        return Result.ok(result);
    }
}
