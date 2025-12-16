-- 清空现有数据
DELETE FROM role_permissions;
DELETE FROM permissions;
DELETE FROM roles;

-- 插入角色数据
INSERT INTO roles (name, code, description) VALUES
('管理员', 'admin', '系统管理员'),
('教师', 'teacher', '教师'),
('学生', 'student', '学生');

-- 插入权限数据
INSERT INTO permissions (name, code, description) VALUES
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
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.code = 'admin';

-- 为教师角色添加权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code = 'teacher' AND p.code IN ('course:view', 'course:create', 'course:edit', 'student:view', 'grade:view', 'grade:edit', 'data:export');

-- 为学生角色添加权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.code = 'student' AND p.code IN ('course:view', 'enrollment:view', 'grade:view');
