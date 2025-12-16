-- Test Data for Student Management System
-- Uses existing users and students from the database

USE student_db;

-- ==================== Course Data ====================
INSERT INTO courses (name, code, description, teacher_id, category, capacity, enrolled, status, start_date, end_date, credits, location, syllabus, requirements, created_at, updated_at) VALUES
('Java Basics Programming', 'JAVA001', 'Learn Java language fundamentals including OOP, Collections, Exception handling', 1, 'Programming Language', 50, 3, 'open', '2025-01-10', '2025-06-30', 4, 'Computer Building A101', 'Comprehensive Java programming course covering syntax, OOP, and advanced features', 'Basic computer knowledge', NOW(), NOW()),
('Python Data Science', 'PYTHON001', 'Master Python programming and data analysis with NumPy and Pandas', 1, 'Programming Language', 45, 3, 'open', '2025-02-15', '2025-07-31', 4, 'Computer Building B202', 'Learn data analysis, visualization and machine learning with Python', 'Programming basics required', NOW(), NOW()),
('Web Frontend Development', 'WEB001', 'Learn HTML, CSS, JavaScript and frontend frameworks for responsive websites', 1, 'Web Development', 40, 2, 'open', '2025-01-15', '2025-06-30', 4, 'Computer Building C303', 'Modern web development with HTML5, CSS3, JavaScript and Vue.js/React', 'HTML basics required', NOW(), NOW()),
('Database Design and SQL', 'DB001', 'Learn relational database design principles and SQL optimization techniques', 2, 'Database', 50, 2, 'open', '2025-01-20', '2025-07-15', 3, 'Classroom A201', 'Database fundamentals, SQL language, performance optimization and transactions', 'Data structures knowledge', NOW(), NOW()),
('Computer Networks Fundamentals', 'NET001', 'Study computer network architecture and TCP/IP protocol family', 2, 'Network Technology', 55, 1, 'open', '2025-02-01', '2025-07-31', 3, 'Classroom B305', 'OSI model, TCP/IP protocols, network security and Internet fundamentals', 'Basic computer knowledge', NOW(), NOW()),
('Operating System Principles', 'OS001', 'Study OS design and implementation covering process and memory management', 2, 'System Fundamentals', 50, 1, 'open', '2025-01-25', '2025-07-20', 3, 'Classroom C401', 'Operating system resource management and Linux system administration', 'Computer architecture knowledge', NOW(), NOW()),
('Algorithms and Data Structures', 'ALGO001', 'Learn common algorithms and data structures for programming problem-solving', 2, 'Algorithm Design', 45, 1, 'open', '2025-03-01', '2025-08-31', 4, 'Classroom D201', 'Sorting, searching, graph theory, dynamic programming and data structure implementation', 'Programming fundamentals', NOW(), NOW()),
('Software Engineering Practice', 'SE001', 'Study software development lifecycle, design patterns and team collaboration', 1, 'Software Engineering', 40, 1, 'open', '2025-02-10', '2025-08-20', 3, 'Classroom E102', 'Requirements analysis, system design, testing and deployment methodologies', 'Programming fundamentals', NOW(), NOW()),
('Mobile Application Development', 'MOBILE001', 'Learn Android application development with frameworks and best practices', 1, 'Mobile Development', 35, 1, 'open', '2025-03-10', '2025-08-30', 4, 'Classroom F303', 'Android Studio, mobile UI design, and complete mobile app development', 'Java fundamentals', NOW(), NOW()),
('Cloud Computing and Docker', 'CLOUD001', 'Learn cloud computing concepts, Docker containerization and Kubernetes orchestration', 2, 'Cloud Computing', 50, 1, 'open', '2025-04-01', '2025-09-30', 3, 'Classroom G401', 'Cloud platforms, Docker containers and Kubernetes for microservices deployment', 'Linux basics required', NOW(), NOW());

-- ==================== Course Schedule Data ====================
INSERT INTO course_schedules (course_id, day_of_week, start_time, end_time, location, created_at) VALUES
(1, 'Monday', '09:00', '11:00', 'Computer Building A101', NOW()),
(1, 'Wednesday', '14:00', '16:00', 'Computer Building A101', NOW()),
(1, 'Friday', '10:00', '12:00', 'Computer Building A101', NOW()),
(2, 'Monday', '14:00', '16:00', 'Computer Building B202', NOW()),
(2, 'Thursday', '09:00', '11:00', 'Computer Building B202', NOW()),
(2, 'Saturday', '10:00', '12:00', 'Computer Building B202', NOW()),
(3, 'Tuesday', '09:00', '11:00', 'Computer Building C303', NOW()),
(3, 'Thursday', '14:00', '16:00', 'Computer Building C303', NOW()),
(4, 'Monday', '13:00', '15:00', 'Classroom A201', NOW()),
(4, 'Wednesday', '09:00', '11:00', 'Classroom A201', NOW()),
(4, 'Friday', '13:00', '15:00', 'Classroom A201', NOW()),
(5, 'Tuesday', '10:00', '12:00', 'Classroom B305', NOW()),
(5, 'Friday', '14:00', '16:00', 'Classroom B305', NOW());

-- ==================== Student Enrollment Data ====================
-- Students 1-5 enrolling in various courses
INSERT INTO student_courses (student_id, course_id, enroll_date, status, created_at, updated_at) VALUES
(1, 1, '2024-12-10 10:30:00', 'active', NOW(), NOW()),
(1, 4, '2024-12-10 11:15:00', 'active', NOW(), NOW()),
(1, 7, '2024-12-10 14:00:00', 'active', NOW(), NOW()),
(2, 2, '2024-12-11 09:00:00', 'active', NOW(), NOW()),
(2, 3, '2024-12-11 10:30:00', 'active', NOW(), NOW()),
(2, 5, '2024-12-11 14:00:00', 'active', NOW(), NOW()),
(3, 1, '2024-12-12 08:00:00', 'active', NOW(), NOW()),
(3, 8, '2024-12-12 11:00:00', 'active', NOW(), NOW()),
(3, 9, '2024-12-12 15:00:00', 'active', NOW(), NOW()),
(4, 2, '2024-12-13 09:30:00', 'active', NOW(), NOW()),
(4, 10, '2024-12-13 13:00:00', 'active', NOW(), NOW()),
(5, 3, '2024-12-14 10:00:00', 'active', NOW(), NOW()),
(5, 6, '2024-12-14 11:30:00', 'active', NOW(), NOW());

-- ==================== Message/Notification Data ====================
INSERT INTO messages (receiver_id, recipient_id, type, title, content, status, created_at, updated_at) VALUES
(1, 1, 'notification', 'Course System Ready', 'The course management system is ready for use', 'read', NOW(), NOW()),
(2, 2, 'notification', 'Enrollment Confirmation', 'Your course enrollments have been confirmed', 'unread', NOW(), NOW()),
(3, 3, 'notification', 'New Courses Available', 'New courses are available for enrollment', 'unread', NOW(), NOW());
