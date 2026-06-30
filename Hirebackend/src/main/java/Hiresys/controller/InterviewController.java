package Hiresys.controller;

import Hiresys.dto.Result;
import Hiresys.entity.Interview;
import Hiresys.service.IInterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "面试管理", description = "面试安排、编辑、确认/拒绝、状态更新与取消")
@RestController
@RequestMapping("/interview")
public class InterviewController {

    @Autowired
    private IInterviewService interviewService;

    @Operation(summary = "HR安排面试")
    @PostMapping("/schedule")
    public Result<?> scheduleInterview(@RequestBody Map<String, Object> interviewData) {
        return wrapServiceResult(interviewService.scheduleInterview(interviewData));
    }

    @Operation(summary = "编辑面试信息")
    @PutMapping("/{interviewId}")
    public Result<?> updateInterview(@PathVariable Long interviewId,
                                     @RequestBody Map<String, Object> interviewData) {
        return wrapServiceResult(interviewService.updateInterview(interviewId, interviewData));
    }

    @Operation(summary = "求职者确认或拒绝面试")
    @PostMapping("/confirm/{interviewId}")
    public Result<?> confirmInterview(@PathVariable Long interviewId,
                                      @RequestParam Integer userId,
                                      @RequestParam Boolean confirmed) {
        return wrapServiceResult(interviewService.confirmInterview(interviewId, userId, confirmed));
    }

    @Operation(summary = "HR更新面试状态")
    @PutMapping("/status/{interviewId}")
    public Result<?> updateInterviewStatus(@PathVariable Long interviewId,
                                            @RequestParam String status,
                                            @RequestParam(required = false) String feedback,
                                            @RequestParam(required = false) Integer rating) {
        return wrapServiceResult(interviewService.updateInterviewStatus(interviewId, status, feedback, rating));
    }

    @Operation(summary = "获取用户的面试列表")
    @GetMapping("/user/{userId}")
    public Result<List<Interview>> getUserInterviews(@PathVariable Integer userId) {
        List<Interview> interviews = interviewService.getUserInterviews(userId);
        return Result.ok(interviews);
    }

    @Operation(summary = "获取公司的面试列表（HR视角）")
    @GetMapping("/company/{companyId}")
    public Result<List<Interview>> getCompanyInterviews(@PathVariable Long companyId) {
        List<Interview> interviews = interviewService.getCompanyInterviews(companyId);
        return Result.ok(interviews);
    }

    @Operation(summary = "取消面试")
    @PostMapping("/cancel/{interviewId}")
    public Result<?> cancelInterview(@PathVariable Long interviewId,
                                      @RequestParam Integer userId,
                                      @RequestParam(required = false) String reason) {
        return wrapServiceResult(interviewService.cancelInterview(interviewId, userId, reason));
    }

    @Operation(summary = "获取面试详情")
    @GetMapping("/{interviewId}")
    public Result<Interview> getInterviewDetail(@PathVariable Long interviewId) {
        Interview interview = interviewService.getById(interviewId);
        if (interview != null) {
            return Result.ok(interview);
        }
        return Result.fail(404, "面试记录不存在");
    }

    private Result<?> wrapServiceResult(Map<String, Object> serviceResult) {
        Boolean success = (Boolean) serviceResult.get("success");
        String message = (String) serviceResult.get("message");
        if (Boolean.TRUE.equals(success)) {
            return Result.ok(message, serviceResult);
        }
        return Result.fail(message != null ? message : "操作失败");
    }
}
