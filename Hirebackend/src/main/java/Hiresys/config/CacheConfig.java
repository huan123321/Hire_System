package Hiresys.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig {

    /** Redis 连接重试次数（解决 Redis 比应用启动慢导致缓存永久降级的问题） */
    private static final int MAX_RETRY = 15;
    private static final long RETRY_INTERVAL_MS = 2000;

    /** 缓存总开关：false 时使用 NoOpCacheManager，所有 @Cacheable 直接穿透（用于 A/B 对比压测） */
    @Value("${app.cache.enabled:true}")
    private boolean cacheEnabled;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        if (!cacheEnabled) {
            log.warn("缓存已禁用（app.cache.enabled=false），@Cacheable 全部穿透直查数据库");
            return new NoOpCacheManager();
        }
        for (int i = 1; i <= MAX_RETRY; i++) {
            try {
                connectionFactory.getConnection().ping();
                if (i > 1) {
                    log.warn("Redis 连接在第 {} 次重试后成功，使用 Redis 缓存（30分钟 TTL）", i);
                } else {
                    log.info("Redis 缓存已启用（30分钟 TTL）");
                }
                return RedisCacheManager.builder(connectionFactory)
                        .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30))
                                .prefixCacheNameWith("cache:"))
                        .build();
            } catch (Exception e) {
                if (i < MAX_RETRY) {
                    log.warn("Redis 连接失败（第 {}/{} 次），{}ms 后重试: {}",
                            i, MAX_RETRY, RETRY_INTERVAL_MS, e.getMessage());
                    try {
                        Thread.sleep(RETRY_INTERVAL_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error("Redis 连续 {} 次连接失败，降级使用内存缓存。注意：本次运行期间 @Cacheable 将不再写入 Redis！", MAX_RETRY);
                    return new ConcurrentMapCacheManager();
                }
            }
        }
        // 理论不可达（循环内必有 return）
        return new ConcurrentMapCacheManager();
    }
}
