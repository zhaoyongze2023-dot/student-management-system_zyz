-- 清空并重新插入中文数据
USE student_db;

-- 禁用外键约束（便于清空数据）
SET FOREIGN_KEY_CHECKS = 0;

-- 清空表数据
TRUNCATE TABLE students;
TRUNCATE TABLE classes;
TRUNCATE TABLE users;
TRUNCATE TABLE dict_data;

-- 启用外键约束
SET FOREIGN_KEY_CHECKS = 1;

-- 重新插入班级数据
INSERT INTO classes (name, grade, student_count, created_at) VALUES
('计算机一班', '2024级', 30, NOW()),
('计算机二班', '2024级', 28, NOW()),
('软件工程一班', '2024级', 32, NOW()),
('软件工程二班', '2024级', 29, NOW()),
('电子信息一班', '2024级', 35, NOW()),
('电子信息二班', '2024级', 33, NOW()),
('网络工程一班', '2024级', 26, NOW()),
('信息安全一班', '2024级', 27, NOW()),
('物联网工程一班', '2023级', 31, NOW()),
('大数据技术一班', '2023级', 34, NOW()),
('人工智能一班', '2023级', 28, NOW()),
('云计算技术一班', '2023级', 25, NOW());

-- 重新插入用户数据 (密码都使用BCrypt加密，原密码为：Test@123456)
INSERT INTO users (username, password, email, phone, created_at, updated_at) VALUES
('admin', '$2a$10$slYQmyNdGzin7olVVCb1Be7DlH.PKZbv5H8KnzzVgXXbVxzy2QIDM', 'admin@example.com', '13800138000', NOW(), NOW()),
('testuser', '$2a$10$slYQmyNdGzin7olVVCb1Be7DlH.PKZbv5H8KnzzVgXXbVxzy2QIDM', 'test@example.com', '13800138001', NOW(), NOW());

-- 重新插入学生数据
INSERT INTO students (student_id, name, class_id, gender, age, phone, email, major, admission_year, status, created_at, updated_at) VALUES
('2024001', '张三', 1, 'M', 20, '13800138000', 'zhangsan@example.com', '计算机科学与技术', 2024, 'active', NOW(), NOW()),
('2024002', '李四', 1, 'F', 20, '13800138001', 'lisi@example.com', '计算机科学与技术', 2024, 'active', NOW(), NOW()),
('2024003', '王五', 2, 'M', 20, '13800138002', 'wangwu@example.com', '计算机科学与技术', 2024, 'active', NOW(), NOW()),
('2024004', '赵六', 2, 'F', 20, '13800138003', 'zhaoliu@example.com', '计算机科学与技术', 2024, 'inactive', NOW(), NOW()),
('2024005', '孙七', 3, 'M', 20, '13800138004', 'sunqi@example.com', '软件工程', 2024, 'active', NOW(), NOW()),
('2024006', '周八', 3, 'F', 21, '13800138005', 'zhouba@example.com', '软件工程', 2024, 'active', NOW(), NOW()),
('2024007', '吴九', 4, 'M', 21, '13800138006', 'wujiu@example.com', '软件工程', 2024, 'active', NOW(), NOW()),
('2024008', '郑十', 4, 'F', 21, '13800138007', 'zhengshi@example.com', '软件工程', 2024, 'graduated', NOW(), NOW()),
('2024009', '刘十一', 5, 'M', 20, '13800138008', 'liushiyi@example.com', '电子信息工程', 2024, 'active', NOW(), NOW()),
('2024010', '陈十二', 5, 'F', 20, '13800138009', 'chenshier@example.com', '电子信息工程', 2024, 'active', NOW(), NOW()),
('2024011', '杨十三', 6, 'M', 20, '13800138010', 'yangshisan@example.com', '电子信息工程', 2024, 'active', NOW(), NOW()),
('2024012', '何十四', 6, 'F', 20, '13800138011', 'heshisi@example.com', '电子信息工程', 2024, 'inactive', NOW(), NOW()),
('2024013', '曾十五', 7, 'M', 21, '13800138012', 'zengshiwu@example.com', '网络工程', 2024, 'active', NOW(), NOW()),
('2024014', '徐十六', 7, 'F', 21, '13800138013', 'xushiliu@example.com', '网络工程', 2024, 'active', NOW(), NOW()),
('2024015', '萧十七', 8, 'M', 20, '13800138014', 'xiaoshiqi@example.com', '信息安全', 2024, 'active', NOW(), NOW()),
('2024016', '朱十八', 8, 'F', 20, '13800138015', 'zhushiba@example.com', '信息安全', 2024, 'active', NOW(), NOW()),
('2023001', '韦十九', 9, 'M', 21, '13800138016', 'weishijiu@example.com', '物联网工程', 2023, 'active', NOW(), NOW()),
('2023002', '唐二十', 9, 'F', 21, '13800138017', 'tangershi@example.com', '物联网工程', 2023, 'active', NOW(), NOW()),
('2023003', '许二十一', 10, 'M', 21, '13800138018', 'xuersiyishi@example.com', '大数据技术', 2023, 'active', NOW(), NOW()),
('2023004', '韩二十二', 10, 'F', 21, '13800138019', 'hanersiyier@example.com', '大数据技术', 2023, 'active', NOW(), NOW());

-- 重新插入数据字典数据
INSERT INTO dict_data (label, value, dict_type, sort, created_at) VALUES
('在读', 'active', 'student_status', 1, NOW()),
('休学', 'inactive', 'student_status', 2, NOW()),
('已毕业', 'graduated', 'student_status', 3, NOW()),
('男', 'M', 'gender', 1, NOW()),
('女', 'F', 'gender', 2, NOW());

-- 验证数据
SELECT '班级数据' as '数据表';
SELECT id, name, grade, student_count FROM classes;

SELECT '学生数据' as '数据表';
SELECT id, student_id, name, gender, age FROM students LIMIT 10;

SELECT '用户数据' as '数据表';
SELECT id, username, email FROM users;

SELECT '数据字典' as '数据表';
SELECT id, label, value, dict_type FROM dict_data;
