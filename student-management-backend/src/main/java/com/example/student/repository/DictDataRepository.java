package com.example.student.repository;

import com.example.student.entity.DictData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DictDataRepository extends JpaRepository<DictData, Long> {
    List<DictData> findByDictTypeOrderBySort(String dictType);
    List<DictData> findByDictType(String dictType);
}
