package com.example.student.service;

import com.example.student.entity.Class;
import com.example.student.entity.DictData;
import com.example.student.repository.ClassRepository;
import com.example.student.repository.DictDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DictService {
    
    @Autowired
    private DictDataRepository dictDataRepository;
    
    @Autowired
    private ClassRepository classRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String DICT_CACHE_KEY = "dict:";
    private static final long CACHE_TIMEOUT = 60 * 60; // 1小时
    
    /**
     * 获取数据字典
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDictData(String dictType) {
        // 尝试从缓存获取
        String cacheKey = DICT_CACHE_KEY + dictType;
        List<DictData> dictDataList = (List<DictData>) redisTemplate.opsForValue().get(cacheKey);
        
        if (dictDataList == null) {
            // 从数据库查询
            dictDataList = dictDataRepository.findByDictTypeOrderBySort(dictType);
            
            // 存入缓存
            if (!dictDataList.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, dictDataList, CACHE_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS);
            }
        }
        
        // 转换为 Map
        return dictDataList.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取班级列表
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getClasses() {
        // 直接从数据库查询，不使用 Redis 缓存以避免类型转换问题
        List<Class> classes = classRepository.findAll();
        
        if (classes == null || classes.isEmpty()) {
            log.warn("数据库中没有班级数据");
            return Collections.emptyList();
        }
        
        // 转换为 Map
        return classes.stream()
                .map(this::convertClassToMap)
                .collect(Collectors.toList());
    }
    
    /**
     * 清除字典缓存
     */
    public void clearDictCache(String dictType) {
        String cacheKey = DICT_CACHE_KEY + dictType;
        redisTemplate.delete(cacheKey);
        log.info("清除字典缓存: {}", cacheKey);
    }
    
    /**
     * 清除班级缓存
     */
    public void clearClassCache() {
        String cacheKey = "dict:classes";
        redisTemplate.delete(cacheKey);
        log.info("清除班级缓存");
    }
    
    /**
     * 将 DictData 转换为 Map
     */
    private Map<String, Object> convertToMap(DictData dictData) {
        Map<String, Object> map = new HashMap<>();
        map.put("label", dictData.getLabel());
        map.put("value", dictData.getValue());
        map.put("type", dictData.getDictType());
        return map;
    }
    
    /**
     * 将 Class 转换为 Map
     */
    private Map<String, Object> convertClassToMap(Class clazz) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", clazz.getId());
        map.put("name", clazz.getName());
        map.put("grade", clazz.getGrade());
        map.put("studentCount", clazz.getStudentCount());
        return map;
    }
}
