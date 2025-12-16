USE student_db;

-- ==================== Message/Notification Data ====================
INSERT INTO messages (receiver_id, recipient_id, type, title, content, status, created_at, updated_at) VALUES
(1, 1, 'notification', 'Course System Ready', 'The course management system is ready for use', 'read', NOW(), NOW()),
(2, 2, 'notification', 'Enrollment Confirmation', 'Your course enrollments have been confirmed', 'unread', NOW(), NOW()),
(3, 3, 'notification', 'New Courses Available', 'New courses are available for enrollment', 'unread', NOW(), NOW());
