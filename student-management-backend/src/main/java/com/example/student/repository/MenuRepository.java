package com.example.student.repository;

import com.example.student.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByParentIdAndStatus(Long parentId, String status);
    
    List<Menu> findByParentIdOrderBySort(Long parentId);
    
    List<Menu> findByStatusOrderBySort(String status);
    
    Optional<Menu> findByPermission(String permission);
    
    @Query("SELECT m FROM Menu m WHERE m.status = 'visible' ORDER BY m.sort ASC")
    List<Menu> findAllVisibleMenus();
    
    @Query("SELECT m FROM Menu m WHERE m.parentId = :parentId AND m.status = 'visible' ORDER BY m.sort ASC")
    List<Menu> findVisibleMenusByParent(@Param("parentId") Long parentId);
}
