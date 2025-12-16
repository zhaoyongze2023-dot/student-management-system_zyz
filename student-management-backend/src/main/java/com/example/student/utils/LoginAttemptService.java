package com.example.student.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LoginAttemptService {
    
    @Value("${login.max-attempts:5}")
    private int maxAttempts;
    
    @Value("${login.lock-duration-minutes:15}")
    private long lockDurationMinutes;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    private static final String LOGIN_ATTEMPT_PREFIX = "login_attempt:";
    private static final String LOGIN_LOCK_PREFIX = "login_lock:";
    
    /**
     * 记录登录成功
     */
    public void loginSucceeded(String username) {
        try {
            String attemptKey = LOGIN_ATTEMPT_PREFIX + username;
            String lockKey = LOGIN_LOCK_PREFIX + username;
            
            // 删除失败尝试记录
            redisTemplate.delete(attemptKey);
            // 删除锁定记录
            redisTemplate.delete(lockKey);
            
            log.info("用户登录成功，清除失败记录: {}", username);
        } catch (Exception e) {
            log.error("处理登录成功失败: {}", e.getMessage());
        }
    }
    
    /**
     * 记录登录失败
     */
    public void loginFailed(String username) {
        try {
            String attemptKey = LOGIN_ATTEMPT_PREFIX + username;
            Long attempts = redisTemplate.opsForValue().increment(attemptKey);
            
            if (attempts == 1) {
                // 第一次失败时设置过期时间
                redisTemplate.expire(attemptKey, lockDurationMinutes, TimeUnit.MINUTES);
            }
            
            // 达到最大尝试次数时，锁定账户
            if (attempts >= maxAttempts) {
                String lockKey = LOGIN_LOCK_PREFIX + username;
                redisTemplate.opsForValue().set(
                    lockKey,
                    "locked",
                    lockDurationMinutes,
                    TimeUnit.MINUTES
                );
                log.warn("用户账户已被锁定，失败次数: {} ({})", attempts, username);
            } else {
                log.warn("用户登录失败，失败次数: {} ({})", attempts, username);
            }
        } catch (Exception e) {
            log.error("记录登录失败异常: {}", e.getMessage());
        }
    }
    
    /**
     * 检查账户是否被锁定
     */
    public boolean isBlocked(String username) {
        try {
            String lockKey = LOGIN_LOCK_PREFIX + username;
            String lockValue = redisTemplate.opsForValue().get(lockKey);
            return lockValue != null && "locked".equals(lockValue);
        } catch (Exception e) {
            log.error("检查账户锁定状态失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取剩余尝试次数
     */
    public int getRemainingAttempts(String username) {
        try {
            String attemptKey = LOGIN_ATTEMPT_PREFIX + username;
            String attempts = redisTemplate.opsForValue().get(attemptKey);
            
            if (attempts == null) {
                return maxAttempts;
            }
            
            int currentAttempts = Integer.parseInt(attempts);
            return Math.max(0, maxAttempts - currentAttempts);
        } catch (Exception e) {
            log.error("获取剩余尝试次数失败: {}", e.getMessage());
            return maxAttempts;
        }
    }
}
