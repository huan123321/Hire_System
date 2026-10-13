package Hiresys.service.impl;

import Hiresys.entity.Job;
import Hiresys.entity.JobApplication;
import Hiresys.entity.User;
import Hiresys.exception.BusinessException;
import Hiresys.mapper.JobMapper;
import Hiresys.service.IJobApplicationService;
import Hiresys.service.IUserService;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobMapper jobMapper;

    @Mock
    private IJobApplicationService jobApplicationService;

    @Mock
    private IUserService userService;

    @InjectMocks
    private JobServiceImpl jobService;

    @BeforeEach
    void setUp() throws Exception {
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(jobService, jobMapper);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Job.class);
    }

    private Job buildJob() {
        Job j = new Job();
        j.setTitle("Java开发工程师");
        j.setCity("北京");
        return j;
    }

    @Test
    void getJobsByPage_调用selectPage并返回分页结果() {
        Page<Job> page = new Page<>(1, 10);
        page.setRecords(List.of(buildJob()));
        page.setTotal(1);
        when(jobMapper.selectPage(any(Page.class), any())).thenReturn(page);

        IPage<Job> result = jobService.getJobsByPage(1, 10, "Java", "北京");

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        verify(jobMapper).selectPage(any(Page.class), any());
    }

    @Test
    void getJobsByPage_无条件查询() {
        Page<Job> page = new Page<>(1, 10);
        page.setRecords(List.of(buildJob()));
        when(jobMapper.selectPage(any(Page.class), any())).thenReturn(page);

        jobService.getJobsByPage(1, 10, null, null);

        verify(jobMapper).selectPage(any(Page.class), any());
    }

    @Test
    void createJob_标题为空抛业务异常() {
        Job j = new Job();
        BusinessException ex = assertThrows(BusinessException.class, () -> jobService.createJob(j));
        assertEquals("职位名称不能为空", ex.getMessage());
        verify(jobMapper, never()).insert(any());
    }

    @Test
    void createJob_成功发布() {
        Job j = buildJob();
        when(jobMapper.insert(any(Job.class))).thenReturn(1);

        Job saved = jobService.createJob(j);

        assertNotNull(saved.getCreateTime());
        verify(jobMapper).insert(j);
    }

    @Test
    void updateJob_职位不存在抛异常() {
        when(jobMapper.selectById(1L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> jobService.updateJob(1L, buildJob()));
        assertEquals("职位不存在", ex.getMessage());
    }

    @Test
    void updateJob_更新失败抛异常() {
        Job existing = buildJob();
        existing.setId(1L);
        when(jobMapper.selectById(1L)).thenReturn(existing);
        when(jobMapper.updateById(any(Job.class))).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> jobService.updateJob(1L, buildJob()));
        assertEquals("职位更新失败", ex.getMessage());
    }

    @Test
    void deleteJob_职位不存在抛异常() {
        when(jobMapper.selectById(1L)).thenReturn(null);
        BusinessException ex = assertThrows(BusinessException.class, () -> jobService.deleteJob(1L));
        assertEquals("职位不存在", ex.getMessage());
    }

    @Test
    void deleteJob_成功删除() {
        Job existing = buildJob();
        existing.setId(1L);
        when(jobMapper.selectById(1L)).thenReturn(existing);
        when(jobMapper.deleteById(1L)).thenReturn(1);

        assertTrue(jobService.deleteJob(1L));
    }

    @Test
    void getJobCandidatesWithDetails_组装申请人与用户信息() {
        JobApplication app = new JobApplication();
        app.setId(10);
        app.setJobId(1);
        app.setUserId(5);
        User candidate = new User();
        candidate.setId(5);
        candidate.setPassword("secret-hash");

        when(jobApplicationService.list(any())).thenReturn(List.of(app));
        when(userService.getById(5)).thenReturn(candidate);

        List<Map<String, Object>> candidates = jobService.getJobCandidatesWithDetails(1L);

        assertEquals(1, candidates.size());
        assertEquals(app, candidates.get(0).get("application"));
        User returnedUser = (User) candidates.get(0).get("user");
        assertNotNull(returnedUser);
        assertNull(returnedUser.getPassword(), "返回给前端的用户信息必须清除密码");
    }
}
