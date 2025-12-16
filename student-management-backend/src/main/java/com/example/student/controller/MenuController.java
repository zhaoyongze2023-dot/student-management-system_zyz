package com.example.student.controller;

import com.example.student.dto.ApiResponse;
import com.example.student.dto.MenuDTO;
import com.example.student.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;
    
    /**
     * 获取当前用户的菜单（根据权限过滤）
     */
    @GetMapping("/my-menus")
    public ResponseEntity<?> getMyMenus(@RequestParam Long userId) {
        try {
            List<MenuDTO> menus = menuService.getUserMenus(userId);
            return ResponseEntity.ok(ApiResponse.success("获取成功", menus));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取菜单失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有菜单树（管理员）
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllMenus() {
        try {
            List<MenuDTO> menus = menuService.getAllMenus();
            return ResponseEntity.ok(ApiResponse.success("获取成功", menus));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取菜单失败: " + e.getMessage()));
        }
    }
    
    /**
     * 创建菜单
     */
    @PostMapping
    public ResponseEntity<?> createMenu(@RequestBody MenuDTO menuDTO) {
        try {
            MenuDTO created = menuService.createMenu(menuDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("菜单创建成功", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "菜单创建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新菜单
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMenu(@PathVariable Long id, @RequestBody MenuDTO menuDTO) {
        try {
            MenuDTO updated = menuService.updateMenu(id, menuDTO);
            return ResponseEntity.ok(ApiResponse.success("菜单更新成功", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "菜单更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMenu(@PathVariable Long id) {
        try {
            menuService.deleteMenu(id);
            return ResponseEntity.ok(ApiResponse.success("菜单删除成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "菜单删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取菜单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMenuDetail(@PathVariable Long id) {
        try {
            MenuDTO menuDTO = menuService.getMenuDetail(id);
            return ResponseEntity.ok(ApiResponse.success("获取成功", menuDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "获取菜单详情失败: " + e.getMessage()));
        }
    }
    
    /**
     * 为角色分配菜单权限
     */
    @PostMapping("/role/{roleId}/assign-permissions")
    public ResponseEntity<?> assignMenusToRole(
            @PathVariable Long roleId,
            @RequestBody List<String> permissionCodes) {
        try {
            menuService.assignMenusToRole(roleId, permissionCodes);
            return ResponseEntity.ok(ApiResponse.success("权限分配成功", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "权限分配失败: " + e.getMessage()));
        }
    }
}
