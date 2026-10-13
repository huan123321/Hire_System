package Hiresys.controller;

import Hiresys.dto.Result;
import Hiresys.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传接口（目前仅头像）
 * <p>
 * 上传的文件保存到本地磁盘（app.upload.dir），通过 WebConfig 的
 * /uploads/** 静态资源映射对外提供访问，接口返回相对路径。
 */
@Tag(name = "文件上传", description = "头像等文件上传")
@RestController
@RequestMapping("/upload")
public class FileUploadController {

    /** 允许的图片类型 */
    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    /** 最大 10MB */
    private static final long MAX_SIZE = 10L * 1024 * 1024;

    /** 头像子目录 */
    private static final String AVATAR_SUB_DIR = "avatars";

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Operation(summary = "上传头像，返回可访问的相对路径")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException("仅支持 JPG/PNG/WebP/GIF 格式的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("图片大小不能超过 10MB");
        }

        String ext = resolveExtension(file.getOriginalFilename(), file.getContentType());
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;

        try {
            Path avatarDir = Paths.get(uploadDir, AVATAR_SUB_DIR).toAbsolutePath().normalize();
            Files.createDirectories(avatarDir);
            file.transferTo(avatarDir.resolve(filename));
        } catch (IOException e) {
            throw new BusinessException("图片保存失败，请稍后重试");
        }

        return Result.ok("上传成功", "/uploads/" + AVATAR_SUB_DIR + "/" + filename);
    }

    private String resolveExtension(String originalFilename, String contentType) {
        if (originalFilename != null) {
            int idx = originalFilename.lastIndexOf('.');
            if (idx >= 0 && idx < originalFilename.length() - 1) {
                String ext = originalFilename.substring(idx + 1).toLowerCase();
                if (ext.matches("(jpg|jpeg|png|webp|gif)")) {
                    return "jpeg".equals(ext) ? "jpg" : ext;
                }
            }
        }
        // 兜底：按 MIME 类型映射
        return switch (contentType == null ? "" : contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            default -> "jpg";
        };
    }
}
