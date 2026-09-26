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

public class GetSecurityQuestionsServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();
        
        String email = request.getParameter("email");
        
        if (email == null || email.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Email is required");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            String sql = "SELECT user_id, security_question_1, security_question_2 FROM users WHERE email = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email.trim());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                result.put("success", true);
                result.put("userId", rs.getInt("user_id"));
                result.put("question1", rs.getString("security_question_1") != null ? rs.getString("security_question_1") : "");
                result.put("question2", rs.getString("security_question_2") != null ? rs.getString("security_question_2") : "");
            } else {
                result.put("success", false);
                result.put("message", "Email not found. Please check and try again.");
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