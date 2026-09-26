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
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class CreateProjectServlet extends HttpServlet {
    
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
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String status = request.getParameter("status");
            String techStack = request.getParameter("techStack");
            String githubRepo = request.getParameter("githubRepo");
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            String objectives = request.getParameter("objectives");
            
            // Validate
            if (name == null || name.trim().isEmpty() ||
                description == null || description.trim().isEmpty() ||
                status == null || status.trim().isEmpty()) {
                
                result.put("success", false);
                result.put("message", "All required fields must be filled");
                out.print(mapper.writeValueAsString(result));
                return;
            }
            
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            String sql = "INSERT INTO projects (name, description, status, tech_stack, github_repo, start_date, end_date, objectives, team_lead_id) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name.trim());
            pstmt.setString(2, description.trim());
            pstmt.setString(3, status);
            pstmt.setString(4, techStack != null ? techStack.trim() : "");
            pstmt.setString(5, githubRepo != null ? githubRepo.trim() : "");
            
            if (startDate != null && !startDate.isEmpty()) {
                pstmt.setDate(6, Date.valueOf(startDate));
            } else {
                pstmt.setDate(6, null);
            }
            
            if (endDate != null && !endDate.isEmpty()) {
                pstmt.setDate(7, Date.valueOf(endDate));
            } else {
                pstmt.setDate(7, null);
            }
            
            pstmt.setString(8, objectives != null ? objectives.trim() : "");
            pstmt.setInt(9, currentUser.getUserId());
            
            int affected = pstmt.executeUpdate();
            pstmt.close();
            db.closeConnection();
            
            if (affected > 0) {
                result.put("success", true);
                result.put("message", "Project created successfully");
            } else {
                result.put("success", false);
                result.put("message", "Failed to create project");
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