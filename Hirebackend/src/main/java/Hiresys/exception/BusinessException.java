package Hiresys.exception;

/**
 * 统一业务异常
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    // 常用工厂方法
    public static BusinessException badRequest(String message) { return new BusinessException(400, message); }
    public static BusinessException notFound(String message) { return new BusinessException(404, message); }
    public static BusinessException unauthorized(String message) { return new BusinessException(401, message); }
    public static BusinessException forbidden(String message) { return new BusinessException(403, message); }

    public int getCode() { return code; }
}
