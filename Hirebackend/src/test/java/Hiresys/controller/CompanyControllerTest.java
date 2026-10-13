package Hiresys.controller;

import Hiresys.auth.TokenService;
import Hiresys.entity.Company;
import Hiresys.service.ICompanyService;
import Hiresys.service.ICompanyViewLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ICompanyService companyService;

    @MockBean
    private ICompanyViewLogService companyViewLogService;

    @MockBean
    private TokenService tokenService;

    private Company buildCompany() {
        Company c = new Company();
        c.setId(1L);
        c.setName("腾讯科技");
        return c;
    }

    @Test
    void 公司详情_存在返回200() throws Exception {
        when(companyService.getById(1L)).thenReturn(buildCompany());

        mockMvc.perform(get("/company/detail/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("腾讯科技"));
    }

    @Test
    void 公司详情_不存在返回404业务码() throws Exception {
        when(companyService.getById(99L)).thenReturn(null);

        mockMvc.perform(get("/company/detail/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("公司不存在"));
    }

    @Test
    void 记录浏览_成功返回200() throws Exception {
        when(companyViewLogService.recordView(any(), any(), any())).thenReturn(true);

        mockMvc.perform(post("/company/1/view").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void 记录浏览_失败返回400() throws Exception {
        when(companyViewLogService.recordView(any(), any(), any())).thenReturn(false);

        mockMvc.perform(post("/company/1/view"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("记录失败"));
    }
}
