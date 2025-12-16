package com.example.student.controller;

import com.example.student.dto.*;
import com.example.student.service.AuthService;
import com.example.student.utils.CaptchaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private CaptchaUtil captchaUtil;
    
    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public ResponseEntity<ApiResponse<Map<String, String>>> getCaptcha() {
        try {
            Map<String, String> captcha = captchaUtil.generateCaptcha();
            return ResponseEntity.ok(ApiResponse.success(captcha));
        } catch (Exception e) {
            log.error("获取验证码失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, "获取验证码失败: " + e.getMessage()));
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        try {
            // 验证验证码 (跳过验证码检查如果在开发环境或两个参数都为空)
            boolean skipCaptchaCheck = (request.getCaptchaKey() == null || request.getCaptchaKey().isEmpty()) &&
                    (request.getCaptcha() == null || request.getCaptcha().isEmpty());
            
            if (!skipCaptchaCheck) {
                if (request.getCaptchaKey() == null || request.getCaptchaKey().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.error(400, "captcha key is required"));
                }
                
                if (request.getCaptcha() == null || request.getCaptcha().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ApiResponse.error(400, "captcha is required"));
                }
                
                if (!captchaUtil.validateCaptcha(request.getCaptchaKey(), request.getCaptcha())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(ApiResponse.error(401, "invalid captcha"));
                }
            }
            
            // 调用认证服务
            Map<String, Object> loginData = authService.login(request.getUsername(), request.getPassword());
            
            return ResponseEntity.ok(ApiResponse.success("login success", loginData));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("locked")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(ApiResponse.error(429, e.getMessage()));
            }
            
            log.error("登录失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, e.getMessage()));
        } catch (Exception e) {
            log.error("登录异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "login failed: " + e.getMessage()));
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody RegisterRequest request) {
        try {
            Map<String, Object> registerData = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success("register success", registerData));
        } catch (RuntimeException e) {
            log.error("注册失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            log.error("注册异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "register failed: " + e.getMessage()));
        }
    }
    
    /**
     * 刷新 Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            Map<String, Object> tokenData = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(ApiResponse.success("token refreshed", tokenData));
        } catch (RuntimeException e) {
            log.error("刷新Token失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, e.getMessage()));
        } catch (Exception e) {
            log.error("刷新Token异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "refresh failed: " + e.getMessage()));
        }
    }
    
    /**
     * 登出
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout() {
        try {
            // 清除 SecurityContext
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(ApiResponse.success("logout success", null));
        } catch (Exception e) {
            log.error("登出异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "logout failed"));
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "未授权"));
            }
            
            String username = authentication.getName();
            UserDTO user = authService.getCurrentUser(username);
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (RuntimeException e) {
            log.error("获取用户信息失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            log.error("获取用户信息异常: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取用户信息失败"));
        }
    }
}
