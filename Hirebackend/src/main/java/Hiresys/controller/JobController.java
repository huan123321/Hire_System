package Hiresys.controller;

import Hiresys.dto.PageResult;
import Hiresys.dto.Result;
import Hiresys.entity.Job;
import Hiresys.service.IHotJobService;
import Hiresys.service.IJobService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "职位管理", description = "职位发布、搜索、筛选、删除与候选人管理")
@RestController
@RequestMapping("/job")
public class JobController {

    @Autowired
    private IJobService jobService;

    @Autowired
    private IHotJobService hotJobService;

    @Operation(summary = "分页搜索职位列表（支持 sort=hot 按热度排序）")
    @GetMapping("/list")
    public Result<PageResult<Job>> list(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String city,
                                        @RequestParam(required = false) String sort) {
        if ("hot".equals(sort)) {
            List<Long> hotIds = hotJobService.getHotJobIds(100);
            if (!hotIds.isEmpty()) {
                // 批量查询避免 N+1（原实现逐个 getById 导致 100 次 SQL）
                Map<Long, Job> jobMap = jobService.listByIds(hotIds).stream()
                        .collect(Collectors.toMap(Job::getId, j -> j));
                List<Job> hotJobs = hotIds.stream()
                        .map(jobMap::get)
                        .filter(Objects::nonNull)
                        .skip((long) (page - 1) * size)
                        .limit(size)
                        .collect(Collectors.toList());
                return Result.ok(new PageResult<>(hotJobs, hotIds.size(), page, size));
            }
        }
        IPage<Job> result = jobService.getJobsByPage(page, size, keyword, city);
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "按条件筛选职位")
    @GetMapping("/filter")
    public Result<List<Job>> filter(@RequestParam(required = false) String salary,
                                    @RequestParam(required = false) String experience,
                                    @RequestParam(required = false) String education) {
        List<Job> jobs = jobService.filterJobs(salary, experience, education);
        return Result.ok(jobs);
    }

    @Operation(summary = "发布新职位")
    @PostMapping("")
    public Result<Job> createJob(@RequestBody Job job) {
        Job savedJob = jobService.createJob(job);
        return Result.ok("职位发布成功", savedJob);
    }

    @Operation(summary = "更新职位信息")
    @PutMapping("/{id}")
    public Result<Job> updateJob(@PathVariable Long id, @RequestBody Job job) {
        Job updatedJob = jobService.updateJob(id, job);
        return Result.ok("职位更新成功", updatedJob);
    }

    @Operation(summary = "按公司名称获取职位")
    @GetMapping("/company/{companyName}")
    public Result<List<Job>> getJobsByCompany(@PathVariable String companyName) {
        List<Job> jobs = jobService.getJobsByCompany(companyName);
        return Result.ok(jobs);
    }

    @Operation(summary = "获取职位候选人列表")
    @GetMapping("/candidates/{jobId}")
    public Result<List<Map<String, Object>>> getJobCandidates(@PathVariable Long jobId) {
        List<Map<String, Object>> candidates = jobService.getJobCandidatesWithDetails(jobId);
        return Result.ok(candidates);
    }

    @Operation(summary = "删除职位")
    @DeleteMapping("/{id}")
    public Result<Void> deleteJob(@PathVariable Long id) {
        boolean success = jobService.deleteJob(id);
        if (success) {
            return Result.ok("职位删除成功", null);
        }
        return Result.fail("职位删除失败");
    }

    @Operation(summary = "热门职位 Top N")
    @GetMapping("/hot")
    public Result<List<Job>> getHotJobs(@RequestParam(defaultValue = "10") int limit) {
        List<Long> ids = hotJobService.getHotJobIds(limit);
        List<Job> jobs = ids.stream()
                .map(jobService::getById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return Result.ok(jobs);
    }

    @Operation(summary = "获取职位热度分数")
    @GetMapping("/{id}/score")
    public Result<Double> getJobScore(@PathVariable Long id) {
        return Result.ok(hotJobService.getScore(id));
    }

    @Operation(summary = "批量获取职位热度分数")
    @PostMapping("/scores")
    public Result<Map<Long, Double>> getJobScores(@RequestBody List<Long> ids) {
        return Result.ok(hotJobService.getScoreMap(ids));
    }

    @Operation(summary = "获取职位详情")
    @GetMapping("/{id}")
    public Result<Job> getJobDetail(@PathVariable Long id) {
        Job job = jobService.getJobById(id);
        if (job != null) {
            hotJobService.recordView(id);
            return Result.ok(job);
        }
        return Result.fail(404, "职位不存在");
    }
}
