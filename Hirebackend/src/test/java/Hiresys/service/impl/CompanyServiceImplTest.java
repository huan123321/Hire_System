package Hiresys.service.impl;

import Hiresys.entity.Company;
import Hiresys.exception.BusinessException;
import Hiresys.mapper.CompanyMapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyServiceImpl companyService;

    @BeforeEach
    void setUp() throws Exception {
        // baseMapper 是 ServiceImpl 的泛型字段（擦除为 BaseMapper），@InjectMocks 匹配不上，显式注入
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(companyService, companyMapper);
        // removeById 内部依赖 TableInfoHelper，需初始化实体表信息
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Company.class);
    }

    private Company buildCompany() {
        Company c = new Company();
        c.setName("腾讯科技");
        c.setIndustry("互联网");
        return c;
    }

    @Test
    void getById_命中mapper并返回公司() {
        Company expected = buildCompany();
        expected.setId(1L);
        when(companyMapper.selectById(1L)).thenReturn(expected);

        Company actual = companyService.getById(1L);

        assertNotNull(actual);
        assertEquals(1L, actual.getId());
        assertEquals("腾讯科技", actual.getName());
    }

    @Test
    void getById_不存在返回null() {
        when(companyMapper.selectById(99L)).thenReturn(null);
        assertNull(companyService.getById(99L));
    }

    @Test
    void addCompany_名称为空抛业务异常() {
        Company c = new Company();
        BusinessException ex = assertThrows(BusinessException.class, () -> companyService.addCompany(c));
        assertEquals("公司名称不能为空", ex.getMessage());
        verify(companyMapper, never()).insert(any());
    }

    @Test
    void addCompany_成功保存并返回() {
        Company c = buildCompany();
        when(companyMapper.insert(any(Company.class))).thenReturn(1);

        Company saved = companyService.addCompany(c);

        assertNotNull(saved);
        assertNotNull(saved.getCreateTime());
        assertNotNull(saved.getUpdateTime());
        verify(companyMapper).insert(c);
    }

    @Test
    void addCompany_保存失败抛业务异常() {
        Company c = buildCompany();
        when(companyMapper.insert(any(Company.class))).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class, () -> companyService.addCompany(c));
        assertEquals("添加公司失败", ex.getMessage());
    }

    @Test
    void updateCompany_公司不存在抛异常() {
        when(companyMapper.selectById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> companyService.updateCompany(1L, buildCompany()));
        assertEquals("公司不存在", ex.getMessage());
        verify(companyMapper, never()).updateById(any());
    }

    @Test
    void updateCompany_成功更新并返回() {
        Company existing = buildCompany();
        existing.setId(1L);
        when(companyMapper.selectById(1L)).thenReturn(existing);
        when(companyMapper.updateById(any(Company.class))).thenReturn(1);

        Company updated = companyService.updateCompany(1L, buildCompany());

        assertEquals(1L, updated.getId());
        assertNotNull(updated.getUpdateTime());
        verify(companyMapper).updateById(any(Company.class));
    }

    @Test
    void deleteCompany_公司不存在抛异常() {
        when(companyMapper.selectById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> companyService.deleteCompany(1L));
        assertEquals("公司不存在", ex.getMessage());
    }

    @Test
    void deleteCompany_成功删除返回true() {
        Company existing = buildCompany();
        existing.setId(1L);
        when(companyMapper.selectById(1L)).thenReturn(existing);
        when(companyMapper.deleteById(1L)).thenReturn(1);

        assertTrue(companyService.deleteCompany(1L));
    }
}
