package Hiresys.service;

import Hiresys.entity.Company;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 公司表 服务类
 * </p>
 *
 * @author author
 * @since 2025-09-19
 */
public interface ICompanyService extends IService<Company> {
    
    /**
     * 分页查询公司信息
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param industry 行业
     * @return 分页结果
     */
    IPage<Company> getCompaniesByPage(Integer page, Integer size, String keyword, String industry);
    
    /**
     * 根据条件筛选公司
     * @param scale 公司规模
     * @param industry 行业
     * @return 符合条件的公司列表
     */
    List<Company> filterCompanies(String scale, String industry);
    
    /**
     * 添加公司
     * @param company 公司信息
     * @return 添加后的公司信息
     */
    Company addCompany(Company company);
    
    /**
     * 更新公司信息
     * @param id 公司 ID
     * @param company 公司信息
     * @return 更新后的公司信息
     */
    Company updateCompany(Long id, Company company);
    
    /**
     * 删除公司
     * @param id 公司 ID
     * @return 是否删除成功
     */
    boolean deleteCompany(Long id);
}