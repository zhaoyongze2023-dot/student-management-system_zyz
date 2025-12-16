package com.example.student.service;

import com.example.student.dto.LoginRequest;
import com.example.student.dto.RegisterRequest;
import com.example.student.dto.UserDTO;
import com.example.student.entity.User;
import com.example.student.entity.Role;
import com.example.student.entity.Permission;
import com.example.student.repository.UserRepository;
import com.example.student.repository.RoleRepository;
import com.example.student.utils.JwtUtil;
import com.example.student.utils.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private LoginAttemptService loginAttemptService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 用户登录
     */
    @Transactional
    public Map<String, Object> login(String username, String password) {
        // 检查是否被锁定
        if (loginAttemptService.isBlocked(username)) {
            throw new RuntimeException("login locked, please try again after 15 minutes");
        }
        
        // 查找用户
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            loginAttemptService.loginFailed(username);
            throw new RuntimeException("invalid username or password");
        }
        
        User user = userOpt.get();
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            loginAttemptService.loginFailed(username);
            throw new RuntimeException("invalid username or password");
        }
        
        // 登录成功 - 更新最后登录时间并重置失败计数
        user.setLastLoginAt(LocalDateTime.now());
        user.setLoginFailureCount(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        loginAttemptService.loginSucceeded(username);
        
        // 生成 Token
        String token = jwtUtil.generateToken(username);
        String refreshToken = jwtUtil.generateRefreshToken(username);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("user", convertToDTO(user));
        
        return result;
    }
    
    /**
     * 用户注册
     */
    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("username already exists");
        }
        
        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("email already exists");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setLoginFailureCount(0);
        
        // 为新用户分配学生角色
        Optional<Role> studentRole = roleRepository.findByCode("student");
        if (studentRole.isPresent()) {
            user.getRoles().add(studentRole.get());
        }
        
        user = userRepository.save(user);
        
        log.info("新用户注册: {}", user.getUsername());
        
        // 生成 Token
        String token = jwtUtil.generateToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("refreshToken", refreshToken);
        result.put("user", convertToDTO(user));
        
        return result;
    }
    
    /**
     * 刷新 Token
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        // 验证刷新令牌
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("invalid or expired refresh token");
        }
        
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        
        // 生成新的访问令牌
        String newToken = jwtUtil.generateToken(username);
        String newRefreshToken = jwtUtil.generateRefreshToken(username);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", newToken);
        result.put("refreshToken", newRefreshToken);
        
        return result;
    }
    
    /**
     * 获取当前用户信息
     */
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("user not found");
        }
        
        return convertToDTO(userOpt.get());
    }
    
    /**
     * 将 User 转换为 UserDTO
     */
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .roles(user.getRoles().stream()
                        .map(Role::getCode)
                        .collect(Collectors.toList()))
                .permissions(user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(Permission::getCode)
                        .distinct()
                        .collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}

