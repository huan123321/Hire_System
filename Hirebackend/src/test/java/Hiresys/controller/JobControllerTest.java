package Hiresys.controller;

import Hiresys.auth.TokenService;
import Hiresys.entity.Job;
import Hiresys.service.IHotJobService;
import Hiresys.service.IJobService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IJobService jobService;

    @MockBean
    private IHotJobService hotJobService;

    @MockBean
    private TokenService tokenService;

    private Job buildJob(Long id, String title) {
        Job job = new Job();
        job.setId(id);
        job.setTitle(title);
        return job;
    }

    @Test
    void sort为hot且ZSet有数据_走listByIds批量查询_不发生N1() throws Exception {
        List<Long> hotIds = List.of(3L, 1L, 2L);
        when(hotJobService.getHotJobIds(100)).thenReturn(hotIds);
        // 注意：listByIds 是 IService 方法，这里 mock 接口方法
        when(jobService.listByIds(hotIds)).thenReturn(List.of(
                buildJob(1L, "职位1"), buildJob(2L, "职位2"), buildJob(3L, "职位3")));

        mockMvc.perform(get("/job/list").param("sort", "hot").param("page", "1").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].id").value(3));

        // 回归断言：批量查询只调用一次，且绝不出现逐个 getById（N+1 修复）
        verify(jobService).listByIds(hotIds);
        verify(jobService, never()).getById(anyLong());
        verify(hotJobService).getHotJobIds(100);
    }

    @Test
    void sort为hot但ZSet为空_回退MySQL分页查询() throws Exception {
        when(hotJobService.getHotJobIds(100)).thenReturn(List.of());
        IPage<Job> page = new Page<>(1, 10);
        page.setRecords(List.of(buildJob(1L, "回退职位")));
        page.setTotal(1);
        when(jobService.getJobsByPage(eq(1), eq(10), any(), any())).thenReturn(page);

        mockMvc.perform(get("/job/list").param("sort", "hot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].title").value("回退职位"));

        verify(jobService).getJobsByPage(eq(1), eq(10), any(), any());
        verify(jobService, never()).listByIds(any());
    }

    @Test
    void 普通列表_直接走MySQL分页() throws Exception {
        IPage<Job> page = new Page<>(1, 10);
        page.setRecords(List.of(buildJob(1L, "普通职位")));
        page.setTotal(1);
        when(jobService.getJobsByPage(eq(1), eq(10), any(), any())).thenReturn(page);

        mockMvc.perform(get("/job/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].title").value("普通职位"));

        verify(jobService).getJobsByPage(eq(1), eq(10), any(), any());
    }

    @Test
    void 普通列表_带关键字和城市条件() throws Exception {
        IPage<Job> page = new Page<>(1, 10);
        page.setRecords(List.of(buildJob(1L, "Java开发")));
        page.setTotal(1);
        when(jobService.getJobsByPage(eq(1), eq(10), eq("Java"), eq("北京"))).thenReturn(page);

        mockMvc.perform(get("/job/list").param("keyword", "Java").param("city", "北京"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));

        verify(jobService).getJobsByPage(eq(1), eq(10), eq("Java"), eq("北京"));
    }

    @Test
    void 职位详情_成功记录浏览热度() throws Exception {
        Job job = buildJob(1L, "Java开发");
        when(jobService.getJobById(1L)).thenReturn(job);

        mockMvc.perform(get("/job/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(hotJobService).recordView(1L);
    }

    @Test
    void 职位详情_不存在返回404() throws Exception {
        when(jobService.getJobById(99L)).thenReturn(null);

        mockMvc.perform(get("/job/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));

        // 不存在的职位不应记录热度
        verify(hotJobService, never()).recordView(anyLong());
    }
}
