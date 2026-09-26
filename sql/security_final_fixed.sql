-- =============================================
-- UAI4D Club Security Tables
-- Database: uai4d_club
-- Simple version - works on all MySQL versions
-- =============================================

USE uai4d_club;

-- Drop tables if they exist
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS user_sessions;
DROP TABLE IF EXISTS login_attempts;
SET FOREIGN_KEY_CHECKS = 1;

-- Create password_reset_tokens
CREATE TABLE password_reset_tokens (
    token_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_token (token),
    INDEX idx_expiry (expiry_date)
);

ALTER TABLE password_reset_tokens 
ADD CONSTRAINT fk_password_reset_user 
FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

-- Create user_sessions
CREATE TABLE user_sessions (
    session_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    session_token VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    device_type VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    INDEX idx_token (session_token),
    INDEX idx_user_active (user_id, is_active)
);

ALTER TABLE user_sessions 
ADD CONSTRAINT fk_session_user 
FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

-- Create login_attempts
CREATE TABLE login_attempts (
    attempt_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45),
    attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN DEFAULT FALSE,
    INDEX idx_username_time (username, attempt_time)
);

-- Add columns to users table (ignore duplicate errors)
-- These will show warnings but continue
ALTER TABLE users ADD COLUMN google_id VARCHAR(255) NULL;
ALTER TABLE users ADD COLUMN oauth_provider VARCHAR(50) NULL;
ALTER TABLE users ADD COLUMN oauth_id VARCHAR(255) NULL;
ALTER TABLE users ADD COLUMN email_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN two_factor_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN two_factor_secret VARCHAR(255) NULL;

-- Update audit_logs
ALTER TABLE audit_logs ADD COLUMN session_id VARCHAR(255) NULL;
ALTER TABLE audit_logs ADD COLUMN location VARCHAR(100) NULL;

-- Show success
SELECT '✅ Security tables created successfully!' AS Status;
SHOW TABLES;