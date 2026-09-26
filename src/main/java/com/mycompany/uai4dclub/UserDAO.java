package com.mycompany.uai4dclub;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private DatabaseConnection db;
    
    public UserDAO() {
        this.db = DatabaseConnection.getInstance();
    }
    
    // ===== CREATE =====
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, email, password_hash, full_name, role, course, year_of_study, skills, bio, university, ai_experience, interests) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getRole() != null ? user.getRole() : "MEMBER");
            pstmt.setString(6, user.getCourse() != null ? user.getCourse() : "");
            pstmt.setInt(7, user.getYearOfStudy());
            pstmt.setString(8, user.getSkills() != null ? user.getSkills() : "");
            pstmt.setString(9, user.getBio() != null ? user.getBio() : "");
            pstmt.setString(10, user.getUniversity() != null ? user.getUniversity() : "");
            pstmt.setString(11, user.getAiExperience() != null ? user.getAiExperience() : "");
            pstmt.setString(12, user.getInterests() != null ? user.getInterests() : "");
            
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // ===== READ =====
    public User findUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public User findUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public User findUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public User findUserByGoogleId(String googleId) {
        String sql = "SELECT * FROM users WHERE google_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, googleId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY user_id DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    // ===== UPDATE =====
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET full_name = ?, course = ?, year_of_study = ?, skills = ?, bio = ?, university = ?, ai_experience = ?, interests = ? WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getCourse());
            pstmt.setInt(3, user.getYearOfStudy());
            pstmt.setString(4, user.getSkills());
            pstmt.setString(5, user.getBio());
            pstmt.setString(6, user.getUniversity() != null ? user.getUniversity() : "");
            pstmt.setString(7, user.getAiExperience() != null ? user.getAiExperience() : "");
            pstmt.setString(8, user.getInterests() != null ? user.getInterests() : "");
            pstmt.setInt(9, user.getUserId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updatePassword(int userId, String newHashedPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newHashedPassword);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updateLastLogin(String username) {
        String sql = "UPDATE users SET last_login = NOW() WHERE username = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updateOAuth(User user) {
        String sql = "UPDATE users SET google_id = ?, oauth_provider = ?, oauth_id = ?, email_verified = TRUE WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getGoogleId());
            pstmt.setString(2, user.getOauthProvider());
            pstmt.setString(3, user.getOauthId());
            pstmt.setInt(4, user.getUserId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // ===== DELETE =====
    public boolean deactivateUser(int userId) {
        String sql = "UPDATE users SET is_active = FALSE WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // ===== UTILITY =====
    public int countUsers() {
        String sql = "SELECT COUNT(*) as total FROM users";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public boolean usernameExists(String username) {
        return findUserByUsername(username) != null;
    }
    
    public boolean emailExists(String email) {
        return findUserByEmail(email) != null;
    }
    
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setProfilePic(rs.getString("profile_pic"));
        user.setCourse(rs.getString("course") != null ? rs.getString("course") : "");
        user.setYearOfStudy(rs.getInt("year_of_study"));
        user.setSkills(rs.getString("skills") != null ? rs.getString("skills") : "");
        user.setBio(rs.getString("bio") != null ? rs.getString("bio") : "");
        user.setActive(rs.getBoolean("is_active"));
        user.setEmailVerified(rs.getBoolean("email_verified"));
        user.setGoogleId(rs.getString("google_id"));
        user.setOauthProvider(rs.getString("oauth_provider"));
        user.setOauthId(rs.getString("oauth_id"));
        user.setLastLogin(rs.getTimestamp("last_login"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        // ===== NEW FIELDS =====
        user.setUniversity(rs.getString("university") != null ? rs.getString("university") : "");
        user.setAiExperience(rs.getString("ai_experience") != null ? rs.getString("ai_experience") : "");
        user.setInterests(rs.getString("interests") != null ? rs.getString("interests") : "");
        
        // Event and Project counts (if available from joins)
        try {
            user.setEventCount(rs.getInt("event_count"));
        } catch (SQLException e) {
            user.setEventCount(0);
        }
        try {
            user.setProjectCount(rs.getInt("project_count"));
        } catch (SQLException e) {
            user.setProjectCount(0);
        }
        
        return user;
    }
}