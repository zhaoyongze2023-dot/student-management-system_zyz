USE student_db;

-- ==================== Student Enrollment Data ====================
INSERT INTO student_courses (student_id, course_id, enroll_date, status, created_at, updated_at) VALUES
(1, 21, '2024-12-10 10:30:00', 'active', NOW(), NOW()),
(1, 24, '2024-12-10 11:15:00', 'active', NOW(), NOW()),
(1, 27, '2024-12-10 14:00:00', 'active', NOW(), NOW()),
(2, 22, '2024-12-11 09:00:00', 'active', NOW(), NOW()),
(2, 23, '2024-12-11 10:30:00', 'active', NOW(), NOW()),
(2, 25, '2024-12-11 14:00:00', 'active', NOW(), NOW()),
(3, 21, '2024-12-12 08:00:00', 'active', NOW(), NOW()),
(3, 28, '2024-12-12 11:00:00', 'active', NOW(), NOW()),
(3, 29, '2024-12-12 15:00:00', 'active', NOW(), NOW()),
(4, 22, '2024-12-13 09:30:00', 'active', NOW(), NOW()),
(4, 30, '2024-12-13 13:00:00', 'active', NOW(), NOW()),
(5, 23, '2024-12-14 10:00:00', 'active', NOW(), NOW()),
(5, 26, '2024-12-14 11:30:00', 'active', NOW(), NOW());
