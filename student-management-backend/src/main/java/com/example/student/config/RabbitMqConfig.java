package com.example.student.config;

import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 消息队列配置
 * 
 * 定义交换机、队列和绑定关系
 */
@Configuration
@ConditionalOnProperty(name = "rabbit-mq.enabled", havingValue = "true")
public class RabbitMqConfig {
    
    // ==================== 课程通知相关 ====================
    
    /** 课程通知交换机 */
    public static final String COURSE_NOTIFICATION_EXCHANGE = "course.notification.exchange";
    
    /** 课程通知队列 */
    public static final String COURSE_NOTIFICATION_QUEUE = "course.notification.queue";
    
    /** 课程通知路由键 */
    public static final String COURSE_NOTIFICATION_ROUTING_KEY = "course.notification.*";
    
    
    // ==================== 文件处理相关 ====================
    
    /** 文件上传交换机 */
    public static final String FILE_UPLOAD_EXCHANGE = "file.upload.exchange";
    
    /** 文件上传队列 */
    public static final String FILE_UPLOAD_QUEUE = "file.upload.queue";
    
    /** 文件上传路由键 */
    public static final String FILE_UPLOAD_ROUTING_KEY = "file.upload.*";
    
    
    // ==================== 课程通知配置 ====================
    
    /**
     * 创建课程通知交换机 (Topic 交换机)
     */
    @Bean
    public TopicExchange courseNotificationExchange() {
        return new TopicExchange(COURSE_NOTIFICATION_EXCHANGE, true, false);
    }
    
    /**
     * 创建课程通知队列
     */
    @Bean
    public Queue courseNotificationQueue() {
        return QueueBuilder.durable(COURSE_NOTIFICATION_QUEUE)
                .withArgument("x-message-ttl", 86400000)  // 消息过期时间: 24小时
                .build();
    }
    
    /**
     * 绑定课程通知交换机和队列
     */
    @Bean
    public Binding courseNotificationBinding() {
        return BindingBuilder
                .bind(courseNotificationQueue())
                .to(courseNotificationExchange())
                .with(COURSE_NOTIFICATION_ROUTING_KEY);
    }
    
    
    // ==================== 文件上传配置 ====================
    
    /**
     * 创建文件上传交换机 (Direct 交换机)
     */
    @Bean
    public DirectExchange fileUploadExchange() {
        return new DirectExchange(FILE_UPLOAD_EXCHANGE, true, false);
    }
    
    /**
     * 创建文件上传队列
     */
    @Bean
    public Queue fileUploadQueue() {
        return QueueBuilder.durable(FILE_UPLOAD_QUEUE)
                .withArgument("x-message-ttl", 3600000)  // 消息过期时间: 1小时
                .build();
    }
    
    /**
     * 绑定文件上传交换机和队列
     */
    @Bean
    public Binding fileUploadBinding() {
        return BindingBuilder
                .bind(fileUploadQueue())
                .to(fileUploadExchange())
                .with(FILE_UPLOAD_ROUTING_KEY);
    }
}
