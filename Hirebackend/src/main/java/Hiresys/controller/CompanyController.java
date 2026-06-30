package Hiresys.controller;

import Hiresys.dto.PageResult;
import Hiresys.dto.Result;
import Hiresys.entity.Company;
import Hiresys.service.ICompanyService;
import Hiresys.service.ICompanyViewLogService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "公司管理", description = "公司信息查询、添加、修改及浏览统计")
@RestController
@RequestMapping("/company")
public class CompanyController {

    @Autowired
    private ICompanyService companyService;

    @Autowired
    private ICompanyViewLogService companyViewLogService;

    @Operation(summary = "分页搜索公司列表")
    @GetMapping("/list")
    public Result<PageResult<Company>> list(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String industry) {
        IPage<Company> result = companyService.getCompaniesByPage(page, size, keyword, industry);
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "按条件筛选公司")
    @GetMapping("/filter")
    public Result<List<Company>> filter(@RequestParam(required = false) String scale,
                                        @RequestParam(required = false) String industry) {
        List<Company> companies = companyService.filterCompanies(scale, industry);
        return Result.ok(companies);
    }

    @Operation(summary = "获取公司详情")
    @GetMapping("/detail/{id}")
    public Result<Company> detail(@PathVariable Long id) {
        Company company = companyService.getById(id);
        if (company != null) {
            return Result.ok(company);
        }
        return Result.fail(404, "公司不存在");
    }

    @Operation(summary = "添加公司")
    @PostMapping("/add")
    public Result<Company> add(@RequestBody Company company) {
        Company savedCompany = companyService.addCompany(company);
        return Result.ok("添加成功", savedCompany);
    }

    @Operation(summary = "更新公司信息")
    @PutMapping("/update/{id}")
    public Result<Company> update(@PathVariable Long id, @RequestBody Company company) {
        Company updatedCompany = companyService.updateCompany(id, company);
        return Result.ok("更新成功", updatedCompany);
    }

    @Operation(summary = "删除公司")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = companyService.deleteCompany(id);
        if (success) {
            return Result.ok("删除成功", null);
        }
        return Result.fail("删除失败");
    }

    @Operation(summary = "获取公司浏览量")
    @GetMapping("/{companyId}/view-count")
    public Result<Long> getViewCount(@PathVariable Long companyId) {
        Long viewCount = companyViewLogService.getViewCount(companyId);
        return Result.ok(viewCount);
    }

    @Operation(summary = "记录公司浏览")
    @PostMapping("/{companyId}/view")
    public Result<Void> recordView(@PathVariable Long companyId,
                                   @RequestParam(required = false) Integer userId,
                                   @RequestParam(required = false) String ipAddress) {
        boolean success = companyViewLogService.recordView(companyId, userId, ipAddress);
        if (success) {
            return Result.ok("记录成功", null);
        }
        return Result.fail("记录失败");
    }
}
