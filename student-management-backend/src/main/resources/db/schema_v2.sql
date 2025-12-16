-- 学生信息管理系统数据库表结构
-- 基于文档中的 API 规范

-- 用户表（已存在，添加必要字段）
-- 注意：以下 ALTER TABLE 语句会在列已存在时提示警告，可以忽略
ALTER TABLE users ADD COLUMN avatar VARCHAR(255) DEFAULT NULL;
ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP NULL;
ALTER TABLE users ADD COLUMN login_failure_count INT DEFAULT 0;
ALTER TABLE users ADD COLUMN locked_until TIMESTAMP NULL;

-- 课程表
CREATE TABLE IF NOT EXISTS courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL COMMENT '课程名称',
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '课程代码',
    description TEXT COMMENT '课程描述',
    teacher_id BIGINT COMMENT '教师ID',
    category VARCHAR(50) COMMENT '课程分类',
    capacity INT DEFAULT 50 COMMENT '课程容量',
    enrolled INT DEFAULT 0 COMMENT '已选人数',
    status VARCHAR(20) DEFAULT 'open' COMMENT 'open:开放, closed:关闭, full:满员, archived:已归档',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    credits INT COMMENT '学分',
    location VARCHAR(200) COMMENT '上课地点',
    syllabus TEXT COMMENT '课程大纲',
    requirements TEXT COMMENT '先修要求',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_status (status),
    INDEX idx_code (code),
    FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- 课程日程表
CREATE TABLE IF NOT EXISTS course_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    day_of_week VARCHAR(20) COMMENT '星期几 (Monday, Tuesday, ...)',
    start_time TIME COMMENT '开始时间',
    end_time TIME COMMENT '结束时间',
    location VARCHAR(200) COMMENT '上课地点',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_course_id (course_id),
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程日程表';

-- 课程附件表
CREATE TABLE IF NOT EXISTS course_attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL COMMENT '课程ID',
    name VARCHAR(255) NOT NULL COMMENT '文件名',
    url VARCHAR(500) NOT NULL COMMENT '文件URL',
    size BIGINT COMMENT '文件大小',
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_course_id (course_id),
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程附件表';

-- 学生选课表
CREATE TABLE IF NOT EXISTS student_courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    enroll_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '选课日期',
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active:在读, completed:已完成, dropped:已退课',
    grade VARCHAR(2) COMMENT '成绩',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_course (student_id, course_id),
    INDEX idx_student_id (student_id),
    INDEX idx_course_id (course_id),
    INDEX idx_status (status),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生选课表';

-- 角色表
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '角色名称',
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '角色代码',
    description VARCHAR(255) COMMENT '角色描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '权限名称',
    code VARCHAR(100) UNIQUE NOT NULL COMMENT '权限代码',
    description VARCHAR(255) COMMENT '权限描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 角色权限中间表
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限表';

-- 用户角色中间表
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色表';

-- 消息表
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipient_id BIGINT NOT NULL COMMENT '接收者ID',
    sender_id BIGINT COMMENT '发送者ID',
    type VARCHAR(20) DEFAULT 'notification' COMMENT 'notification:通知, message:消息',
    title VARCHAR(200) COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    status VARCHAR(20) DEFAULT 'unread' COMMENT 'read:已读, unread:未读',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_recipient_id (recipient_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- 初始化角色数据
INSERT IGNORE INTO roles (name, code, description) VALUES
('管理员', 'admin', '系统管理员'),
('教师', 'teacher', '教师'),
('学生', 'student', '学生');

-- 初始化权限数据
INSERT IGNORE INTO permissions (name, code, description) VALUES
('查看课程', 'course:view', '允许查看课程列表和详情'),
('创建课程', 'course:create', '允许创建新课程'),
('编辑课程', 'course:edit', '允许编辑课程信息'),
('删除课程', 'course:delete', '允许删除课程'),
('查看学生', 'student:view', '允许查看学生列表和详情'),
('编辑学生', 'student:edit', '允许编辑学生信息'),
('删除学生', 'student:delete', '允许删除学生'),
('管理权限', 'permission:manage', '允许管理权限和角色'),
('查看选课', 'enrollment:view', '允许查看选课信息'),
('管理选课', 'enrollment:manage', '允许管理学生选课'),
('查看成绩', 'grade:view', '允许查看成绩'),
('编辑成绩', 'grade:edit', '允许编辑成绩'),
('导出数据', 'data:export', '允许导出数据');

-- 为管理员角色添加所有权限
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.code = 'admin';

-- 为教师角色添加权限
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code = 'teacher' AND p.code IN ('course:view', 'course:create', 'course:edit', 'student:view', 'grade:view', 'grade:edit', 'data:export');

-- 为学生角色添加权限
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code = 'student' AND p.code IN ('course:view', 'enrollment:view', 'grade:view');
