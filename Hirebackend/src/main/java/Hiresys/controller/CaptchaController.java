package Hiresys.controller;

import Hiresys.dto.Result;
import Hiresys.service.CaptchaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@Tag(name = "验证码", description = "生成验证码图片及验证校验")
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    @Operation(summary = "获取验证码图片")
    @GetMapping("/image")
    public void getCaptchaImage(@RequestParam("sessionId") String sessionId,
                                HttpServletResponse response) throws IOException {
        response.setContentType("image/jpeg");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        String captchaCode = captchaService.generateCaptchaCode();
        captchaService.storeCaptcha(sessionId, captchaCode);

        BufferedImage image = createCaptchaImage(captchaCode);
        ImageIO.write(image, "JPEG", response.getOutputStream());
    }

    @Operation(summary = "校验验证码")
    @PostMapping("/verify")
    public Result<Void> verifyCaptcha(@RequestParam String sessionId,
                                       @RequestParam String code) {
        captchaService.verifyCaptcha(sessionId, code);
        return Result.ok("验证码正确", null);
    }

    private BufferedImage createCaptchaImage(String captchaCode) {
        int width = 100;
        int height = 40;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        try {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawRect(0, 0, width - 1, height - 1);

            Random random = new Random();
            for (int i = 0; i < 3; i++) {
                g2d.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
                int x1 = random.nextInt(width);
                int y1 = random.nextInt(height);
                int x2 = random.nextInt(width);
                int y2 = random.nextInt(height);
                g2d.drawLine(x1, y1, x2, y2);
            }

            g2d.setFont(new Font("Arial", Font.BOLD, 28));
            for (int i = 0; i < captchaCode.length(); i++) {
                g2d.setColor(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));
                String ch = String.valueOf(captchaCode.charAt(i));
                int x = (i + 1) * 20;
                int y = 28;
                g2d.drawString(ch, x, y);
            }

            for (int i = 0; i < 20; i++) {
                g2d.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
                int x = random.nextInt(width);
                int y = random.nextInt(height);
                g2d.fillRect(x, y, 2, 2);
            }
        } catch (Exception e) {
            log.error("验证码图片生成失败", e);
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, width, height);
        } finally {
            g2d.dispose();
        }

        return image;
    }
}
