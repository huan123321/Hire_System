package Hiresys.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 轻量 Token 服务：HMAC-SHA256 签名 + 过期时间
 * <p>
 * Token 结构：Base64Url(userId:expireAt) + "." + Base64Url(HMAC-SHA256(payload))
 * 无状态、不依赖 Redis，重启后仍有效（过期时间内的 token 继续可用）。
 */
@Component
public class TokenService {

    /** Token 有效期：7 天（毫秒） */
    private static final long TOKEN_TTL_MS = 7L * 24 * 60 * 60 * 1000;

    private final SecretKeySpec secretKey;

    public TokenService(@Value("${app.auth.secret:hire-system-dev-secret-change-me}") String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /** 为用户生成 Token */
    public String generateToken(Integer userId) {
        long expireAt = System.currentTimeMillis() + TOKEN_TTL_MS;
        String payload = userId + ":" + expireAt;
        String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return payloadBase64 + "." + hmac(payloadBase64);
    }

    /**
     * 解析并校验 Token
     *
     * @return 有效时返回 userId，无效/过期/被篡改时返回 null
     */
    public Integer parseToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            return null;
        }
        String payloadBase64 = parts[0];
        String signature = parts[1];
        // 签名校验（防篡改）
        if (!hmac(payloadBase64).equals(signature)) {
            return null;
        }
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(payloadBase64), StandardCharsets.UTF_8);
            int idx = decoded.indexOf(':');
            if (idx <= 0) {
                return null;
            }
            long expireAt = Long.parseLong(decoded.substring(idx + 1));
            if (System.currentTimeMillis() > expireAt) {
                return null; // 已过期
            }
            return Integer.parseInt(decoded.substring(0, idx));
        } catch (Exception e) {
            return null;
        }
    }

    private String hmac(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC 签名计算失败", e);
        }
    }
}
