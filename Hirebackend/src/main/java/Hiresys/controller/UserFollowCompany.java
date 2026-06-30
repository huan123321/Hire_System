package Hiresys.controller;

import Hiresys.dto.Result;
import Hiresys.entity.Company;
import Hiresys.service.ICompanyService;
import Hiresys.service.IUserFollowCompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "公司关注", description = "求职者关注/取消关注公司、查看关注列表")
@RestController
@RequestMapping("/user-follow-company")
public class UserFollowCompany {

    @Autowired
    private IUserFollowCompanyService userFollowCompanyService;

    @Autowired
    private ICompanyService companyService;

    @Operation(summary = "关注公司")
    @PostMapping("/follow")
    public Result<Void> followCompany(@RequestParam Integer userId,
                                       @RequestParam Long companyId) {
        boolean success = userFollowCompanyService.followCompany(userId, companyId);
        if (success) {
            return Result.ok("关注成功", null);
        }
        return Result.fail("您已关注该公司");
    }

    @Operation(summary = "取消关注公司")
    @PostMapping("/unfollow")
    public Result<Void> unfollowCompany(@RequestParam Integer userId,
                                         @RequestParam Long companyId) {
        boolean success = userFollowCompanyService.unfollowCompany(userId, companyId);
        if (success) {
            return Result.ok("已取消关注", null);
        }
        return Result.fail("取消关注失败");
    }

    @Operation(summary = "检查是否关注")
    @GetMapping("/is-following")
    public Result<Boolean> isFollowing(@RequestParam Integer userId,
                                        @RequestParam Long companyId) {
        boolean isFollowing = userFollowCompanyService.isFollowing(userId, companyId);
        return Result.ok(isFollowing);
    }

    @Operation(summary = "获取关注的公司列表")
    @GetMapping("/companies/{userId}")
    public Result<List<Company>> getFollowedCompanies(@PathVariable Integer userId) {
        List<Company> companies = userFollowCompanyService.getFollowedCompanies(userId);
        return Result.ok(companies);
    }
}
