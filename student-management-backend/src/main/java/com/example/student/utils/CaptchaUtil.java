package com.example.student.utils;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class CaptchaUtil {
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Autowired(required = false)
    private DefaultKaptcha kaptcha;
    
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;
    
    /**
     * 生成验证码
     */
    public Map<String, String> generateCaptcha() {
        try {
            String captchaKey = UUID.randomUUID().toString();
            
            // 如果 kaptcha 可用，使用真实验证码
            if (kaptcha != null) {
                String captchaText = kaptcha.createText();
                BufferedImage bufferedImage = kaptcha.createImage(captchaText);
                
                // 保存验证码到 Redis
                redisTemplate.opsForValue().set(
                    CAPTCHA_KEY_PREFIX + captchaKey,
                    captchaText,
                    CAPTCHA_EXPIRE_MINUTES,
                    TimeUnit.MINUTES
                );
                
                // 转换为 Base64
                String imageBase64 = imageToBase64(bufferedImage);
                
                return Map.of(
                    "key", captchaKey,
                    "image", "data:image/png;base64," + imageBase64
                );
            } else {
                // 回退方案：生成简单的数字验证码
                String captchaText = String.valueOf(new Random().nextInt(9000) + 1000);
                
                redisTemplate.opsForValue().set(
                    CAPTCHA_KEY_PREFIX + captchaKey,
                    captchaText,
                    CAPTCHA_EXPIRE_MINUTES,
                    TimeUnit.MINUTES
                );
                
                return Map.of(
                    "key", captchaKey,
                    "image", "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
                );
            }
        } catch (Exception e) {
            log.error("生成验证码失败: {}", e.getMessage());
            throw new RuntimeException("验证码生成失败");
        }
    }
    
    /**
     * 验证验证码
     */
    public boolean validateCaptcha(String key, String code) {
        try {
            String storedCode = redisTemplate.opsForValue().get(CAPTCHA_KEY_PREFIX + key);
            
            if (storedCode == null) {
                log.warn("验证码已过期: {}", key);
                return false;
            }
            
            // 验证后立即删除验证码
            redisTemplate.delete(CAPTCHA_KEY_PREFIX + key);
            
            return code.equalsIgnoreCase(storedCode);
        } catch (Exception e) {
            log.error("验证验证码失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 将 BufferedImage 转换为 Base64
     */
    private String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // 使用 javax.imageio 转换图片为PNG格式
            ImageIO.write(image, "png", outputStream);
            byte[] bytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.error("图像转Base64失败: {}", e.getMessage());
            return "";
        }
    }
}
