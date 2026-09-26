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
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class VerifySecurityAnswersServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();
        
        String userIdStr = request.getParameter("userId");
        String answer1 = request.getParameter("answer1");
        String answer2 = request.getParameter("answer2");
        
        if (userIdStr == null || userIdStr.isEmpty()) {
            result.put("success", false);
            result.put("message", "User ID is required");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        try {
            int userId = Integer.parseInt(userIdStr);
            
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            String sql = "SELECT security_answer_1, security_answer_2 FROM users WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedAnswer1 = rs.getString("security_answer_1");
                String storedAnswer2 = rs.getString("security_answer_2");
                
                if (storedAnswer1 != null && storedAnswer1.equalsIgnoreCase(answer1.trim()) &&
                    storedAnswer2 != null && storedAnswer2.equalsIgnoreCase(answer2.trim())) {
                    result.put("success", true);
                    result.put("message", "Answers verified successfully");
                } else {
                    result.put("success", false);
                    result.put("message", "Incorrect answers. Please try again.");
                }
            } else {
                result.put("success", false);
                result.put("message", "User not found");
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