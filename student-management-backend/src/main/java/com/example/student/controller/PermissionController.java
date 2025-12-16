package com.example.student.controller;

import com.example.student.dto.ApiResponse;
import com.example.student.dto.MenuDTO;
import com.example.student.entity.Permission;
import com.example.student.entity.Role;
import com.example.student.service.MenuService;
import com.example.student.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/permission")
@Slf4j
public class PermissionController {
    
    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private MenuService menuService;
    
    /**
     * 获取当前用户的菜单列表（前端调用 /permission/menus）
     */
    @GetMapping("/menus")
    public ResponseEntity<?> getUserMenus() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "未授权"));
            }
            
            // 获取所有菜单（简化处理，实际应根据用户角色过滤）
            List<MenuDTO> menus = menuService.getAllMenus();
            return ResponseEntity.ok(ApiResponse.success("获取成功", menus));
        } catch (Exception e) {
            log.error("获取菜单失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取菜单失败"));
        }
    }
    
    /**
     * 获取当前用户的权限列表
     */
    @GetMapping("/my-permissions")
    public ResponseEntity<ApiResponse<List<String>>> getMyPermissions() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "未授权"));
            }
            
            String username = authentication.getName();
            List<String> permissions = permissionService.getUserPermissions(username);
            return ResponseEntity.ok(ApiResponse.success(permissions));
        } catch (Exception e) {
            log.error("获取权限列表失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取权限列表失败"));
        }
    }
    
    /**
     * 获取当前用户的角色列表
     */
    @GetMapping("/my-roles")
    public ResponseEntity<ApiResponse<List<String>>> getMyRoles() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "未授权"));
            }
            
            String username = authentication.getName();
            List<String> roles = permissionService.getUserRoles(username);
            return ResponseEntity.ok(ApiResponse.success(roles));
        } catch (Exception e) {
            log.error("获取角色列表失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取角色列表失败"));
        }
    }
    
    /**
     * 获取所有权限（管理员）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Permission>>> getAllPermissions() {
        try {
            List<Permission> permissions = permissionService.getAllPermissions();
            return ResponseEntity.ok(ApiResponse.success(permissions));
        } catch (Exception e) {
            log.error("获取所有权限失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取所有权限失败"));
        }
    }
    
    /**
     * 获取所有角色（管理员）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        try {
            List<Role> roles = permissionService.getAllRoles();
            return ResponseEntity.ok(ApiResponse.success(roles));
        } catch (Exception e) {
            log.error("获取所有角色失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取所有角色失败"));
        }
    }
    
    /**
     * 给用户添加角色（管理员）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<Object>> addRoleToUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        try {
            permissionService.addRoleToUser(userId, roleId);
            return ResponseEntity.ok(ApiResponse.success("角色添加成功", null));
        } catch (Exception e) {
            log.error("添加角色失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "添加角色失败"));
        }
    }
    
    /**
     * 从用户移除角色（管理员）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<Object>> removeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        try {
            permissionService.removeRoleFromUser(userId, roleId);
            return ResponseEntity.ok(ApiResponse.success("角色移除成功", null));
        } catch (Exception e) {
            log.error("移除角色失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "移除角色失败"));
        }
    }
}
