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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetProjectsServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();
        
        HttpSession session = request.getSession(false);
        int userId = -1;
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            userId = user.getUserId();
        }
        
        String filter = request.getParameter("filter");
        List<Map<String, Object>> projects = new ArrayList<>();
        
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            String sql = "SELECT p.*, " +
                        "u.full_name as lead_name, " +
                        "(SELECT COUNT(*) FROM project_members WHERE project_id = p.project_id) as member_count, " +
                        "(SELECT COUNT(*) FROM project_members WHERE project_id = p.project_id AND user_id = ?) as is_member " +
                        "FROM projects p " +
                        "LEFT JOIN users u ON p.team_lead_id = u.user_id " +
                        "WHERE 1=1";
            
            if (filter != null && !filter.isEmpty() && !filter.equals("all")) {
                sql += " AND LOWER(p.status) = ?";
            }
            
            sql += " ORDER BY p.created_at DESC";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            int paramIndex = 2;
            if (filter != null && !filter.isEmpty() && !filter.equals("all")) {
                pstmt.setString(paramIndex, filter.toLowerCase());
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> project = new HashMap<>();
                project.put("id", rs.getInt("project_id"));
                project.put("name", rs.getString("name"));
                project.put("description", rs.getString("description"));
                project.put("status", rs.getString("status") != null ? rs.getString("status") : "IDEA");
                project.put("lead", rs.getString("lead_name") != null ? rs.getString("lead_name") : "TBD");
                project.put("tech", rs.getString("tech_stack") != null ? rs.getString("tech_stack") : "");
                project.put("members", rs.getInt("member_count"));
                project.put("joined", rs.getInt("is_member") > 0);
                projects.add(project);
            }
            
            rs.close();
            pstmt.close();
            db.closeConnection();
            
            result.put("success", true);
            result.put("projects", projects);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error loading projects: " + e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}