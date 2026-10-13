# Docker 部署踩坑记录

> 记录 Hire System 从本地开发迁移到 Docker Compose 部署过程中踩到的三个典型问题、排查思路与解决方案。
>
> 这三个问题的共同点：**都不是"代码写错了"，而是对容器机制理解不足导致的** —— 记录下来避免重复踩坑。

## 背景

项目使用 Docker Compose 编排四个服务：

| 服务 | 镜像/构建 | 端口 |
|---|---|---|
| MySQL | `mysql:8.0` | 3307 → 3306 |
| Redis | `redis:7-alpine` | 6379 |
| 后端 | `./Hirebackend/Dockerfile`（多阶段构建 → app.jar） | 8094 |
| 前端 | `./Hirefrontend/Dockerfile`（npm build → Nginx） | 8080 → 80 |

编排中的两个关键设计：

```yaml
backend:
  depends_on:
    mysql: { condition: service_healthy }   # 等 MySQL 健康检查通过再启动
    redis: { condition: service_healthy }
  healthcheck:
    test: ["CMD-SHELL", "curl -f http://localhost:8094/actuator/health || exit 1"]
    start_period: 60s
```

---

## 问题一：验证码加载失败 —— Spring Boot 3 的 Servlet API 包名不兼容

### 现象

前端登录页持续提示：

```
❌ 验证码加载失败，正在重试...
❌ 验证码加载失败，请检查后端服务是否启动 (端口 8094)
```

但检查发现后端进程**已经启动**，问题出在别处。

### 根本原因

**Spring Boot 3.x 从 Java EE 迁移到了 Jakarta EE**，Servlet API 的包名发生了破坏性变更：

```java
// ❌ 错误：Spring Boot 2.x 及更早版本的包名
import javax.servlet.http.HttpServletResponse;

// ✅ 正确：Spring Boot 3.x（Jakarta EE）的包名
import jakarta.servlet.http.HttpServletResponse;
```

同时 `pom.xml` 里还混入了**与新版本冲突的旧依赖**：

```xml
<!-- ❌ 不应添加：与 Spring Boot 3.x 冲突 -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>3.1.0</version>
</dependency>
```

**为什么冲突**：Spring Boot 3.x 的 `spring-boot-starter-web` **已经内置了 `jakarta.servlet-api`**，额外引入 `javax.servlet-api` 会导致类路径上两套 Servlet API 并存，编译或运行期行为异常。

### 解决

1. 全局替换 `javax.servlet.*` → `jakarta.servlet.*`（所有使用 Servlet API 的地方都要改）
2. 从 `pom.xml` 中**移除** `javax.servlet-api` 依赖，让 `spring-boot-starter-web` 统一管理版本

### 经验点

> 从 Spring Boot 2 升级到 3 时，`javax.*` → `jakarta.*` 是**必须处理的破坏性变更**，涉及 Servlet、Validation、Persistence 等多个 API。升级前先全局搜索 `import javax.` 定位影响范围。

---

## 问题二：改了代码但容器行为不变 —— 镜像层缓存

### 现象

本地修改了后端（或前端）代码，`docker-compose restart` 之后容器里运行的**仍然是旧版本**。

### 根本原因

Docker 镜像构建时：

- **后端**：执行 `mvn clean package` 打成 `app.jar`，**jar 包被封存在镜像的只读层中**
- **前端**：执行 `npm run build` 生成静态文件并复制进 Nginx 镜像

容器启动时**不会重新构建**，只是基于已有镜像启动 —— 所以代码改动不会生效。`restart` 重启的是容器，不是镜像。

### 解决

```bash
# 代码改动后：重新构建镜像再启动
docker-compose build backend frontend
docker-compose up -d

# 或者一步到位（构建 + 启动）
docker-compose up -d --build
```

### 经验点

> 分清三个层次：**镜像（代码产物）→ 容器（镜像的运行实例）→ 数据卷（持久化数据）**。
> `restart` 只重启容器，**不会更新镜像**；要更新代码必须 `build`。这也是 CI/CD 中"构建"与"部署"必须分开的原因。

---

## 问题三：改了 schema.sql 但表结构没更新 —— MySQL 数据卷

### 现象

修改了 `schema.sql` 里的表结构，重启容器后发现**表还是旧的**。

### 根本原因

`docker-compose.yml` 中配置了数据卷：

```yaml
volumes:
  - mysql_data:/var/lib/mysql
```

MySQL 官方镜像的启动逻辑：

```
启动 MySQL → 检查 /var/lib/mysql 是否为空
                ├─ 空   → 执行 /docker-entrypoint-initdb.d/*.sql（首次初始化）
                └─ 非空 → 认为数据库已初始化，直接跳过初始化脚本
```

所以**只要数据卷存在（非首次启动），Docker 就会跳过 `schema.sql`** —— 修改脚本对已有数据卷无效。

### 解决

```bash
# 1. 停止所有服务并删除数据卷（关键：-v 会删除 mysql_data 卷）
docker-compose down -v

# 2. 重新构建并启动
docker-compose up -d --build
```

> ⚠️ `-v` 会**物理删除数据卷**，数据库数据全部丢失。生产环境务必先备份：
> ```bash
> docker exec hire_mysql mysqldump -uroot -p hire_system > backup.sql
> ```

### 经验点

> **初始化脚本只在数据卷为空时执行一次**。后续的表结构变更应该用**迁移脚本（Flyway / Liquibase）**管理，而不是改 `schema.sql` 后删卷重建 —— 后者在有任何真实数据时都是不可接受的。

---

## 排查清单（可复用）

| 现象 | 先查什么 | 常见原因 |
|---|---|---|
| 接口 500 / 启动失败，报 `ClassNotFoundException: javax.servlet...` | 依赖树 + import 语句 | Spring Boot 3 仍用 `javax.*` 包名或残留旧依赖 |
| 改了代码，容器行为没变 | 镜像构建时间 `docker images` | 没重新 `build`，镜像层里还是旧产物 |
| 建表 / 字段不生效 | 数据卷是否存在 `docker volume ls` | MySQL 仅在空数据卷时执行初始化脚本 |
| 后端连不上数据库 | `docker-compose logs mysql` | 未配 `depends_on: condition: service_healthy`，后端先于 DB 就绪启动 |
| 前端请求 404 | Nginx 配置 + `VUE_APP_API_BASE_URL` 构建参数 | 前端构建时注入的 API 地址不正确 |

### 常用排查命令

```bash
docker-compose ps                    # 各服务状态与健康检查结果
docker-compose logs -f backend       # 跟踪后端日志
docker exec -it hire_mysql bash      # 进容器排查
docker inspect hire_backend | grep -i health   # 查看健康检查详情
docker volume ls                     # 确认数据卷
docker system df                     # 磁盘占用（镜像/容器/卷）
```

---

## 小结

这三个问题的排查过程让我理解了容器化部署的一条核心原则：

> **容器是"不可变基础设施"** —— 代码变更要通过**重新构建镜像**生效，数据变更要通过**迁移脚本**管理，而不是去改运行中的容器或删卷重建。
