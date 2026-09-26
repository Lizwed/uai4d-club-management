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

public class JoinProjectServlet extends HttpServlet {
    
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
            result.put("message", "Please login to join a project");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        User user = (User) session.getAttribute("user");
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
            
            // Check if already a member
            String checkSql = "SELECT COUNT(*) FROM project_members WHERE project_id = ? AND user_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, projectId);
            checkStmt.setInt(2, user.getUserId());
            ResultSet rs = checkStmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            checkStmt.close();
            
            if (count > 0) {
                result.put("success", false);
                result.put("message", "You are already a member of this project");
                out.print(mapper.writeValueAsString(result));
                db.closeConnection();
                return;
            }
            
            // Join project
            String sql = "INSERT INTO project_members (project_id, user_id, role) VALUES (?, ?, 'MEMBER')";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            pstmt.setInt(2, user.getUserId());
            
            int affected = pstmt.executeUpdate();
            
            pstmt.close();
            db.closeConnection();
            
            if (affected > 0) {
                result.put("success", true);
                result.put("message", "Successfully joined the project!");
            } else {
                result.put("success", false);
                result.put("message", "Failed to join project");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}