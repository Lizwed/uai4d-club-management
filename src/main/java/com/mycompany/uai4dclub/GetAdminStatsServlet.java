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

public class GetAdminStatsServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
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
        
        User user = (User) session.getAttribute("user");
        
        // Check if admin
        if (!"ADMIN".equals(user.getRole())) {
            result.put("success", false);
            result.put("message", "Access denied");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            // Count users
            String userSql = "SELECT COUNT(*) as total FROM users";
            PreparedStatement userStmt = conn.prepareStatement(userSql);
            ResultSet userRs = userStmt.executeQuery();
            int userCount = 0;
            if (userRs.next()) {
                userCount = userRs.getInt("total");
            }
            userRs.close();
            userStmt.close();
            
            // Count events
            String eventSql = "SELECT COUNT(*) as total FROM events";
            PreparedStatement eventStmt = conn.prepareStatement(eventSql);
            ResultSet eventRs = eventStmt.executeQuery();
            int eventCount = 0;
            if (eventRs.next()) {
                eventCount = eventRs.getInt("total");
            }
            eventRs.close();
            eventStmt.close();
            
            // Count projects
            String projectSql = "SELECT COUNT(*) as total FROM projects";
            PreparedStatement projectStmt = conn.prepareStatement(projectSql);
            ResultSet projectRs = projectStmt.executeQuery();
            int projectCount = 0;
            if (projectRs.next()) {
                projectCount = projectRs.getInt("total");
            }
            projectRs.close();
            projectStmt.close();
            
            // Count resources
            String resourceSql = "SELECT COUNT(*) as total FROM resources";
            PreparedStatement resourceStmt = conn.prepareStatement(resourceSql);
            ResultSet resourceRs = resourceStmt.executeQuery();
            int resourceCount = 0;
            if (resourceRs.next()) {
                resourceCount = resourceRs.getInt("total");
            }
            resourceRs.close();
            resourceStmt.close();
            
            // Count announcements
            String annSql = "SELECT COUNT(*) as total FROM announcements";
            PreparedStatement annStmt = conn.prepareStatement(annSql);
            ResultSet annRs = annStmt.executeQuery();
            int annCount = 0;
            if (annRs.next()) {
                annCount = annRs.getInt("total");
            }
            annRs.close();
            annStmt.close();
            
            db.closeConnection();
            
            result.put("success", true);
            result.put("users", userCount);
            result.put("events", eventCount);
            result.put("projects", projectCount);
            result.put("resources", resourceCount);
            result.put("announcements", annCount);
            result.put("adminName", user.getFullName());
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}