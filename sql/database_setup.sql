-- =============================================
-- UAI4D Club Management System Database
-- For Jakarta EE 11 + JDK 24
-- =============================================

-- Drop database if exists
DROP DATABASE IF EXISTS uai4d_club;

-- Create database
CREATE DATABASE uai4d_club 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE uai4d_club;

-- =============================================
-- 1. USERS TABLE
-- =============================================
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'MEMBER', 'GUEST') DEFAULT 'MEMBER',
    profile_pic VARCHAR(255) DEFAULT 'default_profile.png',
    course VARCHAR(100),
    year_of_study INT CHECK (year_of_study BETWEEN 1 AND 6),
    skills TEXT,
    bio TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    last_login DATETIME,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =============================================
-- 2. EVENTS TABLE
-- =============================================
CREATE TABLE events (
    event_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    event_date DATETIME NOT NULL,
    location VARCHAR(255),
    max_attendees INT DEFAULT 50,
    current_attendees INT DEFAULT 0,
    status ENUM('UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED') DEFAULT 'UPCOMING',
    event_type ENUM('WORKSHOP', 'TUTORIAL', 'MEETUP', 'HACKATHON', 'GUEST_SPEAKER', 'SOCIAL') DEFAULT 'MEETUP',
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- =============================================
-- 3. EVENT REGISTRATIONS
-- =============================================
CREATE TABLE event_registrations (
    registration_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    event_id INT NOT NULL,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('REGISTERED', 'ATTENDED', 'CANCELLED') DEFAULT 'REGISTERED',
    feedback TEXT,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    UNIQUE KEY unique_registration (user_id, event_id)
);

-- =============================================
-- 4. RESOURCES TABLE
-- =============================================
CREATE TABLE resources (
    resource_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    resource_url VARCHAR(500),
    resource_type ENUM('TUTORIAL', 'ARTICLE', 'VIDEO', 'PDF', 'GITHUB', 'EXTERNAL_LINK') DEFAULT 'TUTORIAL',
    category VARCHAR(100),
    is_premium BOOLEAN DEFAULT FALSE,
    uploaded_by INT,
    view_count INT DEFAULT 0,
    download_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (uploaded_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- =============================================
-- 5. PROJECTS TABLE
-- =============================================
CREATE TABLE projects (
    project_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    status ENUM('IDEA', 'IN_PROGRESS', 'COMPLETED', 'PAUSED', 'ABANDONED') DEFAULT 'IDEA',
    team_lead_id INT,
    github_repo VARCHAR(255),
    tech_stack VARCHAR(255),
    objectives TEXT,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (team_lead_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- =============================================
-- 6. PROJECT MEMBERS
-- =============================================
CREATE TABLE project_members (
    project_member_id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT NOT NULL,
    user_id INT NOT NULL,
    role ENUM('LEAD', 'MEMBER', 'CONTRIBUTOR', 'OBSERVER') DEFAULT 'MEMBER',
    joined_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_project_member (project_id, user_id)
);

-- =============================================
-- 7. ANNOUNCEMENTS
-- =============================================
CREATE TABLE announcements (
    announcement_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') DEFAULT 'MEDIUM',
    is_pinned BOOLEAN DEFAULT FALSE,
    expires_at DATETIME,
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- =============================================
-- 8. AUDIT LOGS
-- =============================================
CREATE TABLE audit_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action VARCHAR(100) NOT NULL,
    description TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    action_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- =============================================
-- 9. CLUB SETTINGS
-- =============================================
CREATE TABLE club_settings (
    setting_id INT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(50) UNIQUE NOT NULL,
    setting_value TEXT,
    setting_description VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =============================================
-- INSERT INITIAL DATA
-- =============================================

-- Admin user (password: Admin@123)
INSERT INTO users (username, email, password_hash, full_name, role, course, year_of_study, bio) 
VALUES (
    'admin_uai4d', 
    'admin@uai4d.com', 
    '$2a$12$N9qB8YpZxVwR5sT7uL.mueXxQyZzWwVvUuTtSsRrQqPpOoIiUuYyZz', 
    'Club Administrator', 
    'ADMIN', 
    'Computer Science', 
    4, 
    'UAI4D Club Administrator - Building the future of AI in Tanzania'
);

-- Sample members
INSERT INTO users (username, email, password_hash, full_name, role, course, year_of_study, skills, bio) 
VALUES 
('john_doe', 'john@uai4d.com', '$2a$12$N9qB8YpZxVwR5sT7uL.mueXxQyZzWwVvUuTtSsRrQqPpOoIiUuYyZz', 'John Doe', 'MEMBER', 'Computer Science', 3, 'Python, Java, AI, ML', 'Passionate about AI for social good'),
('jane_smith', 'jane@uai4d.com', '$2a$12$N9qB8YpZxVwR5sT7uL.mueXxQyZzWwVvUuTtSsRrQqPpOoIiUuYyZz', 'Jane Smith', 'MEMBER', 'Data Science', 2, 'Python, R, SQL', 'Data enthusiast');

-- Sample events
INSERT INTO events (title, description, event_date, location, max_attendees, event_type, status, created_by) 
VALUES 
('Python for AI Workshop', 'Introduction to Python programming for AI applications.', DATE_ADD(NOW(), INTERVAL 7 DAY), 'Computer Lab 301', 30, 'WORKSHOP', 'UPCOMING', 1),
('AI in Agriculture Webinar', 'How AI is transforming agriculture in Tanzania', DATE_ADD(NOW(), INTERVAL 14 DAY), 'Online (Zoom)', 100, 'GUEST_SPEAKER', 'UPCOMING', 1);

-- Club settings
INSERT INTO club_settings (setting_key, setting_value, setting_description) 
VALUES 
('club_name', 'UAI4D Club | UDOM', 'Official club name'),
('club_mission', 'Empowering UDOM students with Python and AI skills to solve development challenges in Tanzania and beyond.', 'Club mission statement');

-- =============================================
-- CREATE VIEWS
-- =============================================

CREATE VIEW vw_upcoming_events AS
SELECT 
    e.event_id,
    e.title,
    e.description,
    e.event_date,
    e.location,
    e.max_attendees,
    e.current_attendees,
    e.event_type,
    u.full_name AS organizer_name
FROM events e
LEFT JOIN users u ON e.created_by = u.user_id
WHERE e.status IN ('UPCOMING', 'ONGOING')
ORDER BY e.event_date ASC;

-- =============================================
-- STORED PROCEDURE
-- =============================================

DELIMITER //
CREATE PROCEDURE register_for_event(
    IN p_user_id INT,
    IN p_event_id INT
)
BEGIN
    DECLARE current_count INT;
    DECLARE max_capacity INT;
    
    SELECT current_attendees, max_attendees INTO current_count, max_capacity
    FROM events WHERE event_id = p_event_id;
    
    IF current_count >= max_capacity THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Event is full';
    ELSE
        INSERT INTO event_registrations (user_id, event_id) 
        VALUES (p_user_id, p_event_id);
        
        UPDATE events SET current_attendees = current_attendees + 1
        WHERE event_id = p_event_id;
    END IF;
END//
DELIMITER ;

SELECT '✅ Database setup complete!' AS Status;