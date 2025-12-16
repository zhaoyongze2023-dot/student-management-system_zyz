package com.example.student.service;

import com.example.student.dto.MenuDTO;
import com.example.student.entity.Menu;
import com.example.student.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {
    private final MenuRepository menuRepository;
    private final PermissionService permissionService;
    
    /**
     * 获取用户的菜单树（根据权限过滤）
     */
    public List<MenuDTO> getUserMenus(Long userId) {
        List<Menu> allMenus = menuRepository.findAllVisibleMenus();
        List<String> userPermissions = permissionService.getUserPermissions(userId);
        
        // 过滤有权限的菜单
        List<Menu> accessibleMenus = allMenus.stream()
                .filter(menu -> {
                    if (menu.getPermission() == null || menu.getPermission().isEmpty()) {
                        return true;
                    }
                    return userPermissions.contains(menu.getPermission());
                })
                .collect(Collectors.toList());
        
        // 构建树形结构
        return buildMenuTree(accessibleMenus, null);
    }
    
    /**
     * 获取所有菜单树（管理员）
     */
    public List<MenuDTO> getAllMenus() {
        List<Menu> allMenus = menuRepository.findAllVisibleMenus();
        return buildMenuTree(allMenus, null);
    }
    
    /**
     * 创建菜单
     */
    public MenuDTO createMenu(MenuDTO menuDTO) {
        Menu menu = new Menu();
        menu.setName(menuDTO.getName());
        menu.setParentId(menuDTO.getParentId());
        menu.setPath(menuDTO.getPath());
        menu.setIcon(menuDTO.getIcon());
        menu.setPermission(menuDTO.getPermission());
        menu.setSort(menuDTO.getSort() != null ? menuDTO.getSort() : 0);
        menu.setStatus(menuDTO.getStatus() != null ? menuDTO.getStatus() : "visible");
        
        Menu saved = menuRepository.save(menu);
        return convertToDTO(saved);
    }
    
    /**
     * 更新菜单
     */
    public MenuDTO updateMenu(Long id, MenuDTO menuDTO) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在"));
        
        menu.setName(menuDTO.getName());
        menu.setParentId(menuDTO.getParentId());
        menu.setPath(menuDTO.getPath());
        menu.setIcon(menuDTO.getIcon());
        menu.setPermission(menuDTO.getPermission());
        menu.setSort(menuDTO.getSort());
        menu.setStatus(menuDTO.getStatus());
        
        Menu updated = menuRepository.save(menu);
        return convertToDTO(updated);
    }
    
    /**
     * 删除菜单
     */
    public void deleteMenu(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在"));
        menuRepository.delete(menu);
    }
    
    /**
     * 获取菜单详情
     */
    public MenuDTO getMenuDetail(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("菜单不存在"));
        return convertToDTO(menu);
    }
    
    /**
     * 为角色分配菜单权限（通过权限码）
     */
    public void assignMenusToRole(Long roleId, List<String> permissionCodes) {
        // 通过权限码获取对应的权限
        List<String> validPermissions = new ArrayList<>();
        for (String code : permissionCodes) {
            permissionService.getPermissionByCode(code).ifPresent(permission -> 
                validPermissions.add(code)
            );
        }
        
        // 这里调用PermissionService的方法来关联角色和权限
        if (!validPermissions.isEmpty()) {
            permissionService.assignPermissionsToRole(roleId, validPermissions);
        }
    }
    
    /**
     * 构建菜单树形结构
     */
    private List<MenuDTO> buildMenuTree(List<Menu> menus, Long parentId) {
        List<MenuDTO> result = new ArrayList<>();
        
        for (Menu menu : menus) {
            if ((parentId == null && menu.getParentId() == null) || 
                (parentId != null && parentId.equals(menu.getParentId()))) {
                
                MenuDTO dto = convertToDTO(menu);
                
                // 递归获取子菜单
                List<MenuDTO> children = buildMenuTree(menus, menu.getId());
                if (!children.isEmpty()) {
                    dto.setChildren(children);
                }
                
                result.add(dto);
            }
        }
        
        // 按sort排序
        result.sort((a, b) -> {
            int aSort = a.getSort() != null ? a.getSort() : 0;
            int bSort = b.getSort() != null ? b.getSort() : 0;
            return Integer.compare(aSort, bSort);
        });
        
        return result;
    }
    
    /**
     * DTO转换
     */
    private MenuDTO convertToDTO(Menu menu) {
        MenuDTO dto = new MenuDTO();
        dto.setId(menu.getId());
        dto.setName(menu.getName());
        dto.setParentId(menu.getParentId());
        dto.setPath(menu.getPath());
        dto.setIcon(menu.getIcon());
        dto.setPermission(menu.getPermission());
        dto.setSort(menu.getSort());
        dto.setStatus(menu.getStatus());
        dto.setCreatedAt(menu.getCreatedAt());
        dto.setUpdatedAt(menu.getUpdatedAt());
        return dto;
    }
}
