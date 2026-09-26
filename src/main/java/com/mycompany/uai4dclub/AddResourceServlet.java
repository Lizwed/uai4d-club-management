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

public class AddResourceServlet extends HttpServlet {
    
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
        
        try {
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String resourceType = request.getParameter("resourceType");
            String resourceUrl = request.getParameter("resourceUrl");
            String category = request.getParameter("category");
            String isPremium = request.getParameter("isPremium");
            
            // Validate
            if (title == null || title.trim().isEmpty() ||
                description == null || description.trim().isEmpty() ||
                resourceType == null || resourceType.trim().isEmpty() ||
                resourceUrl == null || resourceUrl.trim().isEmpty()) {
                
                result.put("success", false);
                result.put("message", "All required fields must be filled");
                out.print(mapper.writeValueAsString(result));
                return;
            }
            
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            String sql = "INSERT INTO resources (title, description, resource_url, resource_type, category, is_premium, uploaded_by) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, title.trim());
            pstmt.setString(2, description.trim());
            pstmt.setString(3, resourceUrl.trim());
            pstmt.setString(4, resourceType);
            pstmt.setString(5, category != null ? category.trim() : "");
            pstmt.setBoolean(6, "true".equals(isPremium));
            pstmt.setInt(7, currentUser.getUserId());
            
            int affected = pstmt.executeUpdate();
            pstmt.close();
            db.closeConnection();
            
            if (affected > 0) {
                result.put("success", true);
                result.put("message", "Resource added successfully");
            } else {
                result.put("success", false);
                result.put("message", "Failed to add resource");
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