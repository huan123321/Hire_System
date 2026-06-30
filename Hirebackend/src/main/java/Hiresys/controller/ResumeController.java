package Hiresys.controller;

import Hiresys.dto.Result;
import Hiresys.entity.Resume;
import Hiresys.service.IResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "简历管理", description = "简历保存、查询、设为默认及删除")
@RestController
@RequestMapping("/resume")
public class ResumeController {

    @Autowired
    private IResumeService resumeService;

    @Operation(summary = "保存或更新简历")
    @PostMapping("/save")
    public Result<Resume> saveResume(@RequestBody Resume resume) {
        Resume savedResume = resumeService.saveResume(resume);
        return Result.ok("简历保存成功", savedResume);
    }

    @Operation(summary = "获取用户的所有简历")
    @GetMapping("/list/{userId}")
    public Result<List<Resume>> getResumes(@PathVariable Integer userId) {
        List<Resume> resumes = resumeService.getResumesByUserId(userId);
        return Result.ok(resumes);
    }

    @Operation(summary = "获取用户默认简历")
    @GetMapping("/default/{userId}")
    public Result<Resume> getDefaultResume(@PathVariable Integer userId) {
        Resume resume = resumeService.getDefaultResume(userId);
        if (resume != null) {
            return Result.ok(resume);
        }
        return Result.fail(404, "未找到简历");
    }

    @Operation(summary = "设置默认简历")
    @PostMapping("/set-default")
    public Result<Void> setDefaultResume(@RequestParam Long resumeId, @RequestParam Integer userId) {
        boolean success = resumeService.setDefaultResume(resumeId, userId);
        if (success) {
            return Result.ok("默认简历设置成功", null);
        }
        return Result.fail("默认简历设置失败");
    }

    @Operation(summary = "删除简历")
    @DeleteMapping("/{resumeId}")
    public Result<Void> deleteResume(@PathVariable Long resumeId) {
        boolean success = resumeService.deleteResume(resumeId);
        if (success) {
            return Result.ok("简历删除成功", null);
        }
        return Result.fail("简历删除失败");
    }
}
