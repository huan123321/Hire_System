package Hiresys.service.impl;

import Hiresys.service.IHotJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HotJobServiceImpl implements IHotJobService {

    private static final String KEY = "hot:jobs";
    private static final double VIEW_SCORE = 1;
    private static final double APPLY_SCORE = 10;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    private final Map<Long, Double> memoryFallback = new LinkedHashMap<>();
    private volatile boolean redisAvailable = true;

    @Override
    public void recordView(Long jobId) {
        incrementScore(jobId, VIEW_SCORE);
    }

    @Override
    public void recordApply(Long jobId) {
        incrementScore(jobId, APPLY_SCORE);
    }

    private void incrementScore(Long jobId, double delta) {
        if (redisAvailable && redisTemplate != null) {
            try {
                redisTemplate.opsForZSet().incrementScore(KEY, jobId.toString(), delta);
            } catch (Exception e) {
                log.warn("Redis 不可用，降级使用内存: {}", e.getMessage());
                redisAvailable = false;
                memoryFallback(jobId, delta);
            }
        } else {
            memoryFallback(jobId, delta);
        }
    }

    private synchronized void memoryFallback(Long jobId, double delta) {
        memoryFallback.merge(jobId, delta, Double::sum);
    }

    @Override
    public List<Long> getHotJobIds(int topN) {
        if (redisAvailable && redisTemplate != null) {
            try {
                Set<String> ids = redisTemplate.opsForZSet().reverseRange(KEY, 0, topN - 1);
                if (ids != null) {
                    return ids
                            .stream()
                            .map(Long::parseLong)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("Redis 读取热门职位失败: {}", e.getMessage());
                redisAvailable = false;
            }
        }
        return memoryFallback.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getHotJobs(int topN) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (redisAvailable && redisTemplate != null) {
            try {
                Set<ZSetOperations.TypedTuple<String>> set = redisTemplate.opsForZSet()
                        .reverseRangeWithScores(KEY, 0, topN - 1);
                if (set != null) {
                    for (ZSetOperations.TypedTuple<String> t : set) {
                        Map<String, Object> m = new HashMap<>();
                        m.put("jobId", Long.parseLong(t.getValue()));
                        m.put("score", t.getScore().intValue());
                        result.add(m);
                    }
                }
            } catch (Exception e) {
                log.warn("Redis 读取热门职位详情失败: {}", e.getMessage());
                redisAvailable = false;
            }
        }
        if (result.isEmpty()) {
            memoryFallback.entrySet().stream()
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(topN)
                    .forEach(e -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("jobId", e.getKey());
                        m.put("score", e.getValue().intValue());
                        result.add(m);
                    });
        }
        return result;
    }

    @Override
    public Double getScore(Long jobId) {
        if (redisAvailable && redisTemplate != null) {
            try {
                return redisTemplate.opsForZSet().score(KEY, jobId.toString());
            } catch (Exception e) {
                redisAvailable = false;
            }
        }
        return memoryFallback.getOrDefault(jobId, 0.0);
    }

    @Override
    public Map<Long, Double> getScoreMap(List<Long> jobIds) {
        Map<Long, Double> result = new HashMap<>();
        if (redisAvailable && redisTemplate != null) {
            try {
                for (Long jobId : jobIds) {
                    Double s = redisTemplate.opsForZSet().score(KEY, jobId.toString());
                    if (s != null) result.put(jobId, s);
                }
            } catch (Exception e) {
                redisAvailable = false;
            }
        }
        if (result.isEmpty()) {
            for (Long jobId : jobIds) {
                result.put(jobId, memoryFallback.getOrDefault(jobId, 0.0));
            }
        }
        return result;
    }
}
