
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
import java.util.HashMap;
import java.util.Map;

public class ToggleUserStatusServlet extends HttpServlet {
    
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
        
        int userId = Integer.parseInt(request.getParameter("userId"));
        String action = request.getParameter("action");
        
        boolean newStatus = action.equals("activate");
        
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, newStatus);
            pstmt.setInt(2, userId);
            
            int affected = pstmt.executeUpdate();
            pstmt.close();
            db.closeConnection();
            
            if (affected > 0) {
                result.put("success", true);
                result.put("message", "User " + action + "d successfully");
            } else {
                result.put("success", false);
                result.put("message", "User not found");
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