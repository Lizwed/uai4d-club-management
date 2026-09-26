package com.mycompany.uai4dclub;

import java.sql.*;
import java.util.UUID;

public class PasswordResetDAO {
    private DatabaseConnection db;
    
    public PasswordResetDAO() {
        this.db = DatabaseConnection.getInstance();
    }
    
    public String createResetToken(int userId, int expiryMinutes) {
        String token = UUID.randomUUID().toString().replace("-", "");
        String sql = "INSERT INTO password_reset_tokens (user_id, token, expiry_date) VALUES (?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE))";
        
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, token);
            pstmt.setInt(3, expiryMinutes);
            
            if (pstmt.executeUpdate() > 0) {
                return token;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public int validateToken(String token) {
        String sql = "SELECT user_id FROM password_reset_tokens WHERE token = ? AND expiry_date > NOW() AND used = FALSE";
        
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public boolean markTokenUsed(String token) {
        String sql = "UPDATE password_reset_tokens SET used = TRUE WHERE token = ?";
        
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, token);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
        }
        return false;
    }
}