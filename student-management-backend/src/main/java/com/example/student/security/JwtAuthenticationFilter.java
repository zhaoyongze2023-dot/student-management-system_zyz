package com.example.student.security;

import com.example.student.entity.Role;
import com.example.student.entity.User;
import com.example.student.repository.UserRepository;
import com.example.student.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserRepository userRepository;
    
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                String username = jwtUtil.getUsernameFromToken(jwt);
                
                if (StringUtils.hasText(username)) {
                    // 从数据库加载用户及其角色权限
                    Optional<User> userOptional = userRepository.findByUsername(username);
                    if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        
                        // 转换角色为 GrantedAuthority
                        List<GrantedAuthority> authorities = convertRolesToAuthorities(user.getRoles());
                        
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        // 记录用户角色信息
                        String roles = user.getRoles().stream()
                            .map(Role::getCode)
                            .collect(Collectors.joining(","));
                        log.debug("设置 JWT 认证用户: {} (角色: {})", username, roles);
                    }
                }
            }
        } catch (Exception e) {
            log.error("JWT 认证过程中出错: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 将用户角色转换为 Spring Security GrantedAuthority
     * 同时支持角色权限
     */
    private List<GrantedAuthority> convertRolesToAuthorities(List<Role> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        if (roles == null || roles.isEmpty()) {
            return authorities;
        }
        
        // 添加角色（ROLE_ 前缀）
        for (Role role : roles) {
            // 添加角色代码
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode().toUpperCase()));
            
            // 添加该角色下的所有权限
            if (role.getPermissions() != null) {
                for (com.example.student.entity.Permission permission : role.getPermissions()) {
                    authorities.add(new SimpleGrantedAuthority(permission.getCode()));
                }
            }
        }
        
        return authorities;
    }
    
    /**
     * 从请求中提取 JWT
     * 优先从 Authorization Header 获取，若无则从 URL 参数获取（WebSocket 握手）
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        // 1. 尝试从 Authorization Header 中提取 token
        String bearerToken = request.getHeader(HEADER_AUTHORIZATION);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        
        // 2. 尝试从 URL 参数中提取 token（WebSocket 握手时使用）
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        
        return null;
    }
}
