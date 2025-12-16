USE student_db;

-- ==================== Course Schedule Data ====================
INSERT INTO course_schedules (course_id, day_of_week, start_time, end_time, location, created_at) VALUES
(21, 'Monday', '09:00', '11:00', 'Computer Building A101', NOW()),
(21, 'Wednesday', '14:00', '16:00', 'Computer Building A101', NOW()),
(21, 'Friday', '10:00', '12:00', 'Computer Building A101', NOW()),
(22, 'Monday', '14:00', '16:00', 'Computer Building B202', NOW()),
(22, 'Thursday', '09:00', '11:00', 'Computer Building B202', NOW()),
(22, 'Saturday', '10:00', '12:00', 'Computer Building B202', NOW()),
(23, 'Tuesday', '09:00', '11:00', 'Computer Building C303', NOW()),
(23, 'Thursday', '14:00', '16:00', 'Computer Building C303', NOW()),
(24, 'Monday', '13:00', '15:00', 'Classroom A201', NOW()),
(24, 'Wednesday', '09:00', '11:00', 'Classroom A201', NOW()),
(24, 'Friday', '13:00', '15:00', 'Classroom A201', NOW()),
(25, 'Tuesday', '10:00', '12:00', 'Classroom B305', NOW()),
(25, 'Friday', '14:00', '16:00', 'Classroom B305', NOW());
