package com.mycompany.uai4dclub;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class VerifySecurityCodeServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();
        
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("user") == null) {
            result.put("success", false);
            result.put("message", "Not logged in");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        User currentUser = (User) session.getAttribute("user");
        
        if (!"ADMIN".equals(currentUser.getRole())) {
            result.put("success", false);
            result.put("message", "Access denied");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        String code = request.getParameter("code");
        
        if (code == null || code.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Security code is required");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            // Check if code exists and is valid
            String sql = "SELECT code_id, code, expires_at FROM security_codes WHERE code = ? AND is_active = TRUE";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, code.trim());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Check if expired
                java.sql.Timestamp expiresAt = rs.getTimestamp("expires_at");
                if (expiresAt != null && expiresAt.before(new java.util.Date())) {
                    result.put("success", false);
                    result.put("message", "Security code has expired");
                } else {
                    // Mark code as used
                    String updateSql = "UPDATE security_codes SET used_by = ?, used_at = NOW() WHERE code = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setInt(1, currentUser.getUserId());
                    updateStmt.setString(2, code.trim());
                    updateStmt.executeUpdate();
                    updateStmt.close();
                    
                    result.put("success", true);
                    result.put("message", "Security code verified successfully");
                }
            } else {
                result.put("success", false);
                result.put("message", "Invalid security code");
            }
            
            rs.close();
            pstmt.close();
            db.closeConnection();
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}