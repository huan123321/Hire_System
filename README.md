# Hire System · 在线招聘系统

> 前后端分离的 B2C 招聘平台，打通「求职者找工作」与「HR 招人」的完整闭环：**发布职位 → 投递简历 → 实时沟通 → 安排面试 → 状态流转**。

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-2.6-4fc08d)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7-red)](https://redis.io/)

---

## 📌 项目简介

Hire System 是一个面向求职者与 HR 的招聘平台。求职者可以搜索职位、维护简历、投递岗位、与 HR 实时沟通、接收面试通知；HR 可以注册企业、发布与管理职位、筛选候选人、安排面试、与候选人聊天。

项目重点解决了三个工程问题：

1. **统一鉴权体系** —— 自研 HMAC-SHA256 无状态 Token + 拦截器，Controller 从 Token 上下文取身份而非信任前端参数，规避越权；
2. **投递闭环** —— 申请职位成功后自动把简历推送到该企业 HR 的聊天窗口，把「投递」与「沟通」合成一个动作；
3. **性能优化与量化验证** —— Redis 缓存 + 热门职位 ZSet + N+1 修复，并通过 JMeter A/B 对比压测量化收益（见下方[性能数据](#-性能优化与压测)）。

---

## 🛠 技术栈

| 层次 | 技术选型 |
|---|---|
| 后端框架 | Spring Boot 3.1.5（Jakarta EE）、Spring MVC、Spring AOP |
| 持久层 | MyBatis-Plus 3.5.3.1（条件构造器 + 注解 SQL + XML） |
| 数据库 | MySQL 8.0（10 张业务表） |
| 缓存 | Redis 7（验证码、Spring Cache、热门职位 ZSet） |
| 实时通信 | 原生 WebSocket（聊天 + 状态变更通知） |
| 鉴权 | 自研 HMAC-SHA256 无状态 Token + HandlerInterceptor |
| 密码安全 | BCrypt（兼容明文自动升级） |
| 接口文档 | SpringDoc OpenAPI 3（Swagger UI） |
| 监控 | Spring Boot Actuator（health / info） |
| 前端 | Vue 2.6、Vue Router、Element UI、Axios |
| 构建部署 | Maven、Docker、Docker Compose、Nginx |
| 测试 | JUnit 5、Mockito、MockMvc（66 个用例） |
| 压测 | Apache JMeter 5.6.3 |

---

## ✨ 核心功能

### 求职者端
- **账号体系**：图形验证码注册/登录，支持求职者与 HR 双角色
- **职位浏览**：分页搜索、城市/薪资/经验/学历筛选、热度排序（Redis ZSet）
- **简历管理**：多份简历、第一份自动设为默认、JSON 结构化存储
- **职位投递**：防重复投递、默认简历兜底 → **自动把简历推送给 HR**
- **实时沟通**：与 HR 一对一聊天，支持离线消息与未读计数
- **面试通知**：接收面试安排，可确认或拒绝
- **收藏与关注**：收藏职位、关注公司
- **AI 助手**：基于 DeepSeek 的求职问答、面试题生成、简历分析

### HR 端
- **企业注册**：注册时同事务创建企业档案并绑定
- **职位管理**：发布、编辑、上架/下架、删除（下架职位对求职者不可见）
- **候选人管理**：按职位/状态筛选，查看简历
- **状态流转**：新申请 → 已查看 → 面试中 → 已录用/已拒绝（后端状态机校验）
- **面试安排**：填写时间/形式/地点/面试官，重复安排校验
- **实时沟通**：与候选人聊天，接收简历投递消息

---

## 🏗 系统架构

```
┌──────────────────────────────────────────────────────────────┐
│                    浏览器 / Vue 2 SPA                          │
│   Vue Router（角色路由守卫）· Axios（统一附加 Token/处理 401）    │
└───────────────────────────┬──────────────────────────────────┘
                            │ HTTP / WebSocket
┌───────────────────────────▼──────────────────────────────────┐
│                      Spring Boot 后端                          │
│                                                               │
│  AuthInterceptor ──► Controller ──► Service ──► Mapper        │
│   (Token 校验)        (参数/身份)    (事务/缓存)   (MyBatis-Plus) │
│                                                               │
│  ├─ GlobalExceptionHandler  统一异常 → Result<T>               │
│  ├─ CacheConfig             Redis Cache / NoOpCache（A/B 开关） │
│  ├─ ChatWebSocketHandler    ChatMessageHandler（在线映射/转发）  │
│  └─ HotJobServiceImpl       Redis ZSet 热度排行 + 内存降级       │
└───────┬──────────────────────────┬────────────────────────────┘
        │                          │
   ┌────▼─────┐              ┌─────▼─────┐
   │ MySQL 8  │              │  Redis 7  │
   │ 10 张表   │              │ 缓存/验证码 │
   └──────────┘              └───────────┘
```

### 数据模型（10 张表）

| 表 | 说明 |
|---|---|
| `user` | 用户（求职者 / HR / 管理员），HR 通过 `company_id` 关联企业 |
| `company` | 企业信息（含 logo） |
| `job` | 职位（`status`: active 招聘中 / inactive 已下架） |
| `resume` | 简历（基础信息、教育、经历、项目均为 JSON 字段） |
| `job_application` | 投递记录（状态机：new/viewed/interview/hired/rejected） |
| `interview` | 面试安排（scheduled/confirmed/completed/cancelled） |
| `chat_message` | 聊天消息 |
| `user_favorite_job` | 收藏职位 |
| `user_follow_company` | 关注公司 |
| `company_view_log` | 公司浏览记录 |

---

## 🚀 快速开始

### 方式一：Docker Compose（推荐，一键启动）

```bash
# 1. 克隆项目
git clone <your-repo-url>
cd Hire_System

# 2. 启动全部服务（MySQL + Redis + 后端 + 前端）
docker-compose up -d --build

# 3. 查看启动状态
docker-compose ps
```

启动后访问：

| 服务 | 地址 |
|---|---|
| 前端页面 | http://localhost:8080 |
| 后端接口 | http://localhost:8094 |
| 接口文档（Swagger） | http://localhost:8094/swagger-ui.html |
| 健康检查 | http://localhost:8094/actuator/health |

> ⚠️ **首次启动**会自动执行 `schema.sql` 初始化 10 张表。
> 若修改了表结构，需清空数据卷重新初始化：`docker-compose down -v && docker-compose up -d --build`
> （MySQL 只在数据卷为空时执行初始化脚本，这是 Docker 官方镜像的行为）

### 方式二：本地开发

**前置条件**：JDK 17、Maven 3.8+、Node.js 16+、MySQL 8、Redis 7

```bash
# 1. 初始化数据库
mysql -uroot -p -e "CREATE DATABASE hire_system DEFAULT CHARSET utf8mb4;"
mysql -uroot -p hire_system < Hirebackend/src/main/resources/schema.sql

# 2. 配置后端（复制模板并填写数据库密码）
cd Hirebackend/src/main/resources
cp application.yml.example application.yml
# 编辑 application.yml，修改 spring.datasource.password

# 3. 启动后端（默认 8094 端口）
cd ../../..
mvn spring-boot:run

# 4. 启动前端（默认 8080 端口，需另开终端）
cd Hirefrontend
npm install
npm run serve
```

---

## 📖 接口文档

后端集成 SpringDoc，启动后访问 **http://localhost:8094/swagger-ui.html**

主要接口分组：

| 分组 | 路径前缀 | 说明 |
|---|---|---|
| 用户管理 | `/user` | 注册、登录、用户信息、头像上传 |
| 验证码 | `/captcha` | 图形验证码生成与校验 |
| 职位管理 | `/job` | 职位发布、搜索、筛选、热度 |
| 职位申请 | `/job-application` | 投递、候选人列表、状态流转 |
| 公司管理 | `/company` | 公司列表、详情、筛选、HR 查询 |
| 简历管理 | `/resume` | 简历增删改查、默认简历 |
| 面试管理 | `/interview` | 面试安排、确认、取消 |
| 即时聊天 | `/chat` | 聊天记录、会话列表、未读消息 |
| AI 助手 | `/ai` | DeepSeek 智能问答与分析 |

---

## ⚡ 性能优化与压测

使用 **JMeter 5.6.3** 做 A/B 对比压测（同一份代码、仅切换变量、参数完全一致）。

### 1. 职位列表缓存（A/B 对比：`app.cache.enabled` 开关）

同一份代码部署两个实例（无缓存 `NoOpCacheManager` / 有缓存 `RedisCacheManager`），50 并发、Ramp-Up 5s、**持续 60 秒**：

| 指标 | 无缓存 | 有缓存 | 变化 |
|---|---|---|---|
| **TPS** | 705.58 | **6,853.78** | **+871%** |
| 平均响应 | 67.88 ms | **6.90 ms** | **-89.8%** |
| P95 | 129 ms | **11 ms** | -91.5% |
| P99 | 228 ms | 22 ms | -90% |
| 错误率 | 0% | 0% | — |

**收益来源**：无缓存时每次请求执行 1 次分页查询 + 10 次公司查询（N+1），共 11 次 DB 操作；缓存命中后压缩为 1 次 Redis 读取。

**缓存设计**：key 含全部查询参数；`condition = "#page <= 3 && #size == 10"` 只缓存前 3 页热点；`unless` 空结果不缓存防穿透；发布/上下架用 `@CacheEvict(allEntries=true)` 保证一致性。

> 局限说明：该实验命中率为 100%（固定请求首页），9.7 倍为缓存收益上限；生产环境因翻页/搜索命中率下降会打折 —— 这正是只缓存前 3 页的原因。

### 2. 热门职位 N+1 查询修复

ZSet 命中后逐个 `getById()`（Top100 → 100 次 SQL），改为 `listByIds()` 批量查询 + Map 还原顺序：

| 指标 | 修复前 | 修复后 |
|---|---|---|
| 响应时间 | 5,526 ms | **147 ms** |
| P95 | 6,224 ms | **305 ms** |

### 3. 其他优化

| 优化项 | 效果 |
|---|---|
| 公司详情 Redis 缓存 | 144.8 ms → **18 ms** |
| 关闭 MyBatis `StdOutImpl` 控制台 SQL 日志 | 同负载吞吐 110 → **1014 QPS**（约 9 倍） |
| CacheConfig 连接重试（15 次 × 2s） | 修复 Redis 后启动导致 `@Cacheable` 永久降级为 JVM 内存的缺陷 |

---

## 🧪 测试

```bash
cd Hirebackend
mvn test
```

- **66 个单元测试**，覆盖 Service 层核心逻辑与 Controller 层接口
- 纯 Mockito 单测，**不依赖真实 MySQL / Redis**，可离线运行
- 重点用例：
  - N+1 回归锁定（`verify(listByIds)` + `verify(getById, never())`）
  - Redis 降级分支（`thenThrow` 模拟 Redis 故障）
  - 缓存开关、状态机流转、鉴权拦截

> ⚠️ 若项目路径包含中文（Windows），Mockito inline mock maker 无法自动 attach，需指定：
> `mvn test "-DargLine=-javaagent:<path>/byte-buddy-agent-1.14.9.jar"`

---

## 📁 项目结构

```
Hire_System/
├── Hirebackend/                    # Spring Boot 后端
│   ├── src/main/java/Hiresys/
│   │   ├── auth/                   # Token 生成/校验、鉴权拦截器
│   │   ├── config/                 # Cache、Web、MyBatis 等配置
│   │   ├── controller/             # REST 接口层
│   │   ├── service/                # 业务逻辑层（接口 + impl）
│   │   ├── mapper/                 # MyBatis-Plus Mapper
│   │   ├── entity/ dto/ converter/ # 数据模型与转换
│   │   ├── exception/              # 业务异常 + 全局异常处理
│   │   └── websocket/              # WebSocket 聊天处理器
│   └── src/main/resources/
│       ├── schema.sql              # 数据库初始化脚本（10 张表）
│       ├── application.yml.example # 配置模板（复制为 application.yml）
│       └── mapper/                 # MyBatis XML
├── Hirefrontend/                   # Vue 2 前端
│   └── src/
│       ├── components/             # 页面组件（求职者端 / HrPage 端）
│       ├── router/ utils/          # 路由守卫、请求封装、WebSocket
│       └── assets/                 # 静态资源
├── docker-compose.yml              # 一键部署编排
└── docs/                           # 项目文档
```

---

## 🔧 配置说明

关键配置项（见 `application.yml.example`）：

| 配置项 | 环境变量 | 默认值 | 说明 |
|---|---|---|---|
| 服务端口 | `SERVER_PORT` | 8094 | 后端监听端口 |
| 数据库 | `DB_HOST`/`DB_PORT`/`DB_NAME` | localhost/3306/hire_system | MySQL 连接 |
| 数据库账号 | `DB_USERNAME`/`DB_PASSWORD` | root / — | **必改** |
| Redis | `REDIS_HOST`/`REDIS_PORT` | localhost/6379 | 缓存与验证码 |
| 缓存开关 | `APP_CACHE_ENABLED` | true | `false` = 无缓存基线（用于 A/B 压测） |
| Token 密钥 | `APP_AUTH_SECRET` | dev 默认值 | **生产必须覆盖** |
| AI 密钥 | `DEEPSEEK_API_KEY` | 空 | 未配置时 AI 功能优雅降级 |

> 🔒 `application.yml` 已加入 `.gitignore`（含数据库密码），请勿提交真实凭据。

---

## 📝 说明

- **接口统一响应**：`{ "code": 200, "message": "success", "data": {...} }`，异常由 `GlobalExceptionHandler` 统一转换为 `Result`
- **鉴权方式**：请求头 `Authorization: Bearer <token>`，Token 有效期 7 天，无状态可水平扩展
- **WebSocket 地址**：`ws://localhost:8094/ws/chat?userId={id}&userType=user`
- **头像上传**：`POST /upload/avatar`，限制 10MB，返回可访问 URL
