package Hiresys.controller;

import Hiresys.dto.Result;
import Hiresys.service.IJobApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "职位申请", description = "求职者投递职位、HR查看申请人、状态更新、申请检查")
@RestController
@RequestMapping("/job-application")
public class JobApplicationController {

    @Autowired
    private IJobApplicationService jobApplicationService;

    @Operation(summary = "申请职位")
    @PostMapping("/apply")
    public Result<Integer> applyForPosition(@RequestParam Integer jobId,
                                            @RequestParam Integer userId,
                                            @RequestParam(required = false) Integer resumeId) {
        Integer applicationId = jobApplicationService.applyForPosition(jobId, userId, resumeId);
        return Result.ok("职位申请成功", applicationId);
    }

    @Operation(summary = "获取用户的申请记录")
    @GetMapping("/user/{userId}")
    public Result<List<Map<String, Object>>> getUserApplications(@PathVariable Integer userId) {
        List<Map<String, Object>> applications = jobApplicationService.getUserApplicationsWithDetails(userId);
        return Result.ok(applications);
    }

    @Operation(summary = "获取职位的申请者列表")
    @GetMapping("/job/{jobId}/applicants")
    public Result<List<Map<String, Object>>> getJobApplicants(@PathVariable Integer jobId) {
        List<Map<String, Object>> applicants = jobApplicationService.getJobApplicantsWithDetails(jobId);
        return Result.ok(applicants);
    }

    @Operation(summary = "更新申请状态")
    @PutMapping("/{applicationId}/status")
    public Result<Void> updateApplicationStatus(@PathVariable Integer applicationId,
                                                @RequestParam String status,
                                                @RequestParam(required = false) String feedback) {
        boolean success = jobApplicationService.updateApplicationStatus(applicationId, status, feedback);
        if (success) {
            return Result.ok("状态更新成功", null);
        }
        return Result.fail("状态更新失败");
    }

    @Operation(summary = "检查是否已申请")
    @GetMapping("/check")
    public Result<Boolean> checkApplicationStatus(@RequestParam Integer jobId,
                                                   @RequestParam Integer userId) {
        boolean hasApplied = jobApplicationService.hasApplied(jobId, userId);
        return Result.ok(hasApplied);
    }

    @Operation(summary = "获取职位申请人数")
    @GetMapping("/job/{jobId}/count")
    public Result<Long> getJobApplicationCount(@PathVariable Integer jobId) {
        long count = jobApplicationService.count(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Hiresys.entity.JobApplication>()
                        .eq("job_id", jobId)
        );
        return Result.ok(count);
    }
}
