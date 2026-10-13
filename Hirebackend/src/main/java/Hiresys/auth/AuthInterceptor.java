package Hiresys.auth;

import Hiresys.dto.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录鉴权拦截器
 * <p>
 * 从请求头 Authorization: Bearer &lt;token&gt; 解析当前用户，
 * 校验通过后将 userId 放入 request attribute（key = {@link #ATTR_USER_ID}），
 * 供 Controller 从认证上下文取身份，而不是信任客户端传来的 userId 参数。
 * 未登录/无效 token 返回 HTTP 401 + JSON 错误体。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_USER_ID = "currentUserId";

    @Autowired
    private TokenService tokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 公开接口白名单（无需登录）
        if (isPublicRequest(method, path)) {
            return true;
        }

        // 解析 Authorization 头
        Integer userId = null;
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            userId = tokenService.parseToken(auth.substring(7));
        }

        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(Result.fail(401, "未登录或登录已过期")));
            return false;
        }

        request.setAttribute(ATTR_USER_ID, userId);
        return true;
    }

    /**
     * 公开接口判断（方法 + 路径）
     */
    private boolean isPublicRequest(String method, String path) {
        // 验证码
        if (path.startsWith("/captcha")) {
            return true;
        }
        // 注册 / 登录
        if ("POST".equals(method) && ("/user/login".equals(path) || "/user/".equals(path))) {
            return true;
        }
        // 查询单个用户信息（DTO 不含密码）
        if ("GET".equals(method) && path.matches("/user/\\d+")) {
            return true;
        }
        // 职位：全部读操作 + 热度评分（供职位列表页使用）
        if ("GET".equals(method) && path.startsWith("/job")) {
            return true;
        }
        if ("POST".equals(method) && "/job/scores".equals(path)) {
            return true;
        }
        // 公司：全部读操作 + 浏览记录
        if ("GET".equals(method) && path.startsWith("/company")) {
            return true;
        }
        if ("POST".equals(method) && path.matches("/company/\\d+/view")) {
            return true;
        }
        // WebSocket 握手由独立 handler 处理（不经过 MVC 拦截器），这里显式放行
        if (path.startsWith("/ws")) {
            return true;
        }
        // 上传文件的静态访问（头像图片，<img> 加载不携带 token）
        if (path.startsWith("/uploads")) {
            return true;
        }
        // 开发/运维入口：Swagger、Actuator
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")
                || path.startsWith("/webjars") || path.equals("/doc.html")
                || path.startsWith("/actuator")) {
            return true;
        }
        return false;
    }
}
