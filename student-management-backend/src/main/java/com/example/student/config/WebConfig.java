package com.example.student.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * 配置 CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Type")
                .allowCredentials(false)
                .maxAge(3600);
    }
    
    /**
     * 配置静态资源处理
     * 注意：只配置明确的静态资源路由，避免与 REST API 冲突
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 提供上传文件的访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
        
        // 提供静态资源访问
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        
        // 不配置默认的 /** 映射，这样 /search 等路由会正确地被 Controller 处理
    }
}
