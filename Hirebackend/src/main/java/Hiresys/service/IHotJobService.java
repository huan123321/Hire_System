package Hiresys.service;

import java.util.List;
import java.util.Map;

public interface IHotJobService {

    /** 记录职位被查看 */
    void recordView(Long jobId);

    /** 记录职位被申请 */
    void recordApply(Long jobId);

    /** 获取热门职位 top N，返回 jobId 列表 */
    List<Long> getHotJobIds(int topN);

    /** 获取热门职位及分数，返回 [{jobId, score}] */
    List<Map<String, Object>> getHotJobs(int topN);

    /** 获取单个职位的热度分数 */
    Double getScore(Long jobId);

    /** 获取多个职位的热度分数映射 */
    Map<Long, Double> getScoreMap(List<Long> jobIds);
}
