-- 创建数据库
CREATE DATABASE IF NOT EXISTS student_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_db;

-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 班级表
CREATE TABLE classes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE COMMENT '班级名称',
    grade VARCHAR(50) COMMENT '年级',
    student_count INT DEFAULT 0 COMMENT '学生数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_grade (grade)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- 学生表
CREATE TABLE students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(50) UNIQUE NOT NULL COMMENT '学号',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    class_id BIGINT NOT NULL COMMENT '班级ID',
    gender VARCHAR(1) COMMENT 'M:男 F:女',
    age INT COMMENT '年龄',
    phone VARCHAR(20) COMMENT '电话',
    email VARCHAR(100) COMMENT '邮箱',
    major VARCHAR(100) COMMENT '专业',
    admission_year INT COMMENT '入学年份',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active:在读 inactive:休学 graduated:已毕业',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    INDEX idx_class_id (class_id),
    INDEX idx_status (status),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生表';

-- 初始化班级数据
INSERT INTO classes (name, grade) VALUES
('计算机一班', '2024级'),
('计算机二班', '2024级'),
('软件工程一班', '2024级'),
('电子信息一班', '2024级'),
('电子信息二班', '2024级');

-- 初始化用户数据 (密码都是使用BCrypt加密，原密码为：Test@123456)
INSERT INTO users (username, password, email, phone) VALUES
('testuser', '$2a$10$slYQmyNdGzin7olVVCb1Be7DlH.PKZbv5H8KnzzVgXXbVxzy2QIDM', 'test@example.com', '13800138000'),
('admin', '$2a$10$slYQmyNdGzin7olVVCb1Be7DlH.PKZbv5H8KnzzVgXXbVxzy2QIDM', 'admin@example.com', '13800138001');

-- 初始化学生数据
INSERT INTO students (student_id, name, class_id, gender, age, phone, email, major, admission_year, status) VALUES
('2024001', '张三', 1, 'M', 20, '13800138000', 'student1@example.com', '计算机科学与技术', 2024, 'active'),
('2024002', '李四', 1, 'F', 20, '13800138001', 'student2@example.com', '计算机科学与技术', 2024, 'active'),
('2024003', '王五', 2, 'M', 20, '13800138002', 'student3@example.com', '计算机科学与技术', 2024, 'active'),
('2024004', '赵六', 2, 'F', 20, '13800138003', 'student4@example.com', '计算机科学与技术', 2024, 'inactive'),
('2024005', '孙七', 3, 'M', 20, '13800138004', 'student5@example.com', '软件工程', 2024, 'active');

-- 数据字典表
CREATE TABLE dict_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    label VARCHAR(100) NOT NULL COMMENT '标签',
    value VARCHAR(100) NOT NULL COMMENT '值',
    dict_type VARCHAR(50) NOT NULL COMMENT '字典类型',
    sort INT DEFAULT 0 COMMENT '排序',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典表';

-- 初始化数据字典
INSERT INTO dict_data (label, value, dict_type, sort) VALUES
('在读', 'active', 'status', 1),
('休学', 'inactive', 'status', 2),
('已毕业', 'graduated', 'status', 3),
('男', 'M', 'gender', 1),
('女', 'F', 'gender', 2);
