package com.mycompany.uai4dclub;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

public class ResetPasswordFromSecurityServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();
        
        String userIdStr = request.getParameter("userId");
        String newPassword = request.getParameter("newPassword");
        
        if (userIdStr == null || userIdStr.isEmpty()) {
            result.put("success", false);
            result.put("message", "User ID is required");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Password is required");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        if (!PasswordUtil.isPasswordStrong(newPassword)) {
            result.put("success", false);
            result.put("message", "Password must be at least 8 characters with uppercase, lowercase, number, and special character");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        try {
            int userId = Integer.parseInt(userIdStr);
            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, hashedPassword);
            pstmt.setInt(2, userId);
            
            int affected = pstmt.executeUpdate();
            pstmt.close();
            db.closeConnection();
            
            if (affected > 0) {
                result.put("success", true);
                result.put("message", "Password reset successfully");
            } else {
                result.put("success", false);
                result.put("message", "Failed to reset password");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}