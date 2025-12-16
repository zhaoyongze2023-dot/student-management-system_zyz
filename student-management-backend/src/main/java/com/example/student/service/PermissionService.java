package com.example.student.service;

import com.example.student.dto.UserDTO;
import com.example.student.entity.User;
import com.example.student.entity.Role;
import com.example.student.entity.Permission;
import com.example.student.repository.UserRepository;
import com.example.student.repository.RoleRepository;
import com.example.student.repository.PermissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PermissionService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    /**
     * 获取用户的所有权限代码（按用户名）
     */
    @Transactional(readOnly = true)
    public List<String> getUserPermissions(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        User user = userOpt.get();
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户的所有权限代码（按用户ID）
     */
    @Transactional(readOnly = true)
    public List<String> getUserPermissions(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        User user = userOpt.get();
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * 检查用户是否有某个权限
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(String username, String permissionCode) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(p -> p.getCode().equals(permissionCode));
    }
    
    /**
     * 获取用户角色
     */
    @Transactional(readOnly = true)
    public List<String> getUserRoles(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        User user = userOpt.get();
        return user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toList());
    }
    
    /**
     * 给用户添加角色
     */
    @Transactional
    public void addRoleToUser(Long userId, Long roleId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        
        if (userOpt.isPresent() && roleOpt.isPresent()) {
            User user = userOpt.get();
            Role role = roleOpt.get();
            if (!user.getRoles().contains(role)) {
                user.getRoles().add(role);
                userRepository.save(user);
                log.info("添加角色 {} 给用户 {}", role.getCode(), user.getUsername());
            }
        }
    }
    
    /**
     * 从用户移除角色
     */
    @Transactional
    public void removeRoleFromUser(Long userId, Long roleId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        
        if (userOpt.isPresent() && roleOpt.isPresent()) {
            User user = userOpt.get();
            Role role = roleOpt.get();
            user.getRoles().remove(role);
            userRepository.save(user);
            log.info("移除角色 {} 从用户 {}", role.getCode(), user.getUsername());
        }
    }
    
    /**
     * 获取所有权限
     */
    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }
    
    /**
     * 获取所有角色
     */
    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    /**
     * 根据权限代码获取权限
     */
    @Transactional(readOnly = true)
    public Optional<Permission> getPermissionByCode(String code) {
        return permissionRepository.findByCode(code);
    }
    
    /**
     * 为角色分配权限（通过权限代码列表）
     */
    @Transactional
    public void assignPermissionsToRole(Long roleId, List<String> permissionCodes) {
        Optional<Role> roleOpt = roleRepository.findById(roleId);
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("角色不存在");
        }
        
        Role role = roleOpt.get();
        List<Permission> permissions = new ArrayList<>();
        
        for (String code : permissionCodes) {
            Optional<Permission> permOpt = permissionRepository.findByCode(code);
            permOpt.ifPresent(permissions::add);
        }
        
        role.setPermissions(permissions);
        roleRepository.save(role);
        log.info("为角色 {} 分配了 {} 个权限", role.getCode(), permissions.size());
    }
}
