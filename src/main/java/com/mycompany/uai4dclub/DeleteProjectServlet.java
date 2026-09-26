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

public class DeleteProjectServlet extends HttpServlet {
    
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
        
        String projectIdStr = request.getParameter("projectId");
        
        if (projectIdStr == null || projectIdStr.isEmpty()) {
            result.put("success", false);
            result.put("message", "Project ID is required");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        try {
            int projectId = Integer.parseInt(projectIdStr);
            
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            // Delete project members first
            String deleteMembersSql = "DELETE FROM project_members WHERE project_id = ?";
            PreparedStatement membersStmt = conn.prepareStatement(deleteMembersSql);
            membersStmt.setInt(1, projectId);
            membersStmt.executeUpdate();
            membersStmt.close();
            
            // Delete project
            String sql = "DELETE FROM projects WHERE project_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            
            int affected = pstmt.executeUpdate();
            pstmt.close();
            db.closeConnection();
            
            if (affected > 0) {
                result.put("success", true);
                result.put("message", "Project deleted successfully");
            } else {
                result.put("success", false);
                result.put("message", "Project not found");
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