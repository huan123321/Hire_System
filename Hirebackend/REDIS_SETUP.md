# Redis 集成指南

## 1. 安装 Redis

### Windows 系统安装：
1. 访问 GitHub 下载页面：https://github.com/tporadowski/redis/releases
2. 下载最新版本的 Redis-x64-x.x.x.zip
3. 解压到任意目录（如：C:\Redis）
4. 双击运行 `redis-server.exe` 启动 Redis 服务
5. 验证安装：打开命令行，输入 `redis-cli ping`，返回 `PONG` 表示成功

## 2. 项目已完成的配置

### 2.1 依赖添加（pom.xml）
已添加以下依赖：
- `spring-boot-starter-data-redis`：Spring Data Redis 核心依赖
- `commons-pool2`：Redis 连接池支持

### 2.2 application.yml 配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    timeout: 10000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

### 2.3 已创建的配置类
- **RedisConfig.java**：Redis 配置类，包含 RedisTemplate 和 CacheManager 的 Bean 定义
- **RedisUtil.java**：Redis 工具类，封装了常用的 Redis 操作
- **RedisTestController.java**：Redis 测试接口

## 3. 使用示例

### 3.1 在 Service 中使用缓存

```java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Autowired
    private RedisUtil redisUtil;
    
    private static final String USER_CACHE_KEY_PREFIX = "user:";
    
    // 查询用户（带缓存）
    public User getUserById(Integer id) {
        // 1. 先从 Redis 缓存中获取
        String cacheKey = USER_CACHE_KEY_PREFIX + id;
        User user = (User) redisUtil.get(cacheKey);
        
        if (user != null) {
            return user; // 缓存命中
        }
        
        // 2. 缓存中没有，从数据库查询
        user = this.getById(id);
        
        // 3. 将查询结果放入 Redis 缓存
        if (user != null) {
            redisUtil.set(cacheKey, user, 30 * 60); // 30 分钟过期
        }
        
        return user;
    }
    
    // 更新用户（清除缓存）
    public boolean saveOrUpdateUser(User user) {
        boolean result;
        if (user.getId() != null) {
            // 更新操作
            result = this.updateById(user);
            // 清除缓存中的旧数据
            if (result) {
                String cacheKey = USER_CACHE_KEY_PREFIX + user.getId();
                redisUtil.del(cacheKey);
            }
        } else {
            // 新增操作
            result = this.save(user);
        }
        return result;
    }
}
```

### 3.2 使用 @Cacheable 注解方式

```java
@Service
public class UserServiceImpl {
    
    // 使用 Spring Cache 注解
    @Cacheable(value = "user", key = "#id", unless = "#result == null")
    public User getUserById(Integer id) {
        return this.getById(id);
    }
    
    @CacheEvict(value = "user", key = "#user.id")
    public boolean updateUser(User user) {
        return this.updateById(user);
    }
}
```

### 3.3 RedisUtil 工具类常用方法

#### String 类型操作：
```java
// 设置缓存
redisUtil.set("key", "value", 300); // 5 分钟过期

// 获取缓存
Object value = redisUtil.get("key");

// 判断 key 是否存在
boolean exists = redisUtil.hasKey("key");

// 删除缓存
redisUtil.del("key");

// 设置过期时间
redisUtil.expire("key", 600);
```

#### Hash 类型操作：
```java
// 设置 hash
redisUtil.hset("user:1", "name", "张三", 300);

// 获取 hash 字段
Object name = redisUtil.hget("user:1", "name");

// 获取整个 hash
Map<Object, Object> map = redisUtil.hmget("user:1");
```

#### List 类型操作：
```java
// 添加到列表
redisUtil.lSet("list:key", "value1");
redisUtil.lSet("list:key", Arrays.asList("value1", "value2"), 300);

// 获取列表
List<Object> list = redisUtil.lGet("list:key");
```

#### Set 类型操作：
```java
// 添加到集合
redisUtil.sSet("set:key", "value1", "value2", "value3");

// 判断是否在集合中
boolean isMember = redisUtil.sHasKey("set:key", "value1");
```

## 4. 测试接口

启动项目后，可以使用以下接口测试 Redis：

### 4.1 测试 Redis 连接
```
GET http://localhost:8094/redis/test
```

### 4.2 设置缓存
```
POST http://localhost:8094/redis/set?key=testkey&value=testvalue&expire=300
```

### 4.3 获取缓存
```
GET http://localhost:8094/redis/get?key=testkey
```

### 4.4 删除缓存
```
DELETE http://localhost:8094/redis/del?key=testkey
```

### 4.5 检查 key 是否存在
```
GET http://localhost:8094/redis/exists?key=testkey
```

### 4.6 测试用户缓存
```
# 第一次查询（从数据库）
GET http://localhost:8094/user/1

# 第二次查询（从缓存）
GET http://localhost:8094/user/1
```

## 5. 常见应用场景

### 5.1 热点数据缓存
- 用户信息
- 职位信息
- 公司信息
- 配置信息

### 5.2 Session 管理
```java
// 存储用户登录 session
String sessionKey = "session:" + userId;
redisUtil.set(sessionKey, userInfo, 3600); // 1 小时过期

// 获取用户 session
Object session = redisUtil.get("session:" + userId);
```

### 5.3 分布式锁
```java
// 尝试获取锁
Boolean lock = redisUtil.setIfAbsent("lock:order:123", "worker1", 10);
if (lock) {
    try {
        // 执行业务逻辑
    } finally {
        // 释放锁
        redisUtil.del("lock:order:123");
    }
}
```

### 5.4 计数器
```java
// 文章阅读量
redisUtil.incr("article:view:123", 1);

// 获取阅读量
Long viewCount = (Long) redisUtil.get("article:view:123");
```

## 6. 注意事项

1. **缓存穿透**：查询不存在的数据，请求直接打到数据库
   - 解决方案：缓存空对象或使用布隆过滤器

2. **缓存击穿**：热点 key 过期，大量请求涌入数据库
   - 解决方案：设置热点数据永不过期或使用互斥锁

3. **缓存雪崩**：大量 key 同时过期
   - 解决方案：设置随机过期时间

4. **数据一致性**：数据库和缓存数据不一致
   - 解决方案：先更新数据库，再删除缓存

5. **内存管理**：合理设置过期时间，避免内存溢出
   - 使用 Redis 的 maxmemory 策略

## 7. 性能优化建议

1. 批量操作使用 pipeline
2. 避免使用 keys 命令
3. 合理使用数据结构
4. 设置合适的过期时间
5. 监控 Redis 内存使用情况
