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

public class GetUsersServlet extends HttpServlet {
    
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
        
        if (!"ADMIN".equals(user.getRole())) {
            result.put("success", false);
            result.put("message", "Access denied");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        try {
            int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
            int limit = 10;
            int offset = (page - 1) * limit;
            
            String search = request.getParameter("search");
            String role = request.getParameter("role");
            String status = request.getParameter("status");
            
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            // Build query
            StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
            StringBuilder countSql = new StringBuilder("SELECT COUNT(*) as total FROM users WHERE 1=1");
            
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim() + "%";
                sql.append(" AND (username LIKE ? OR email LIKE ? OR full_name LIKE ?)");
                countSql.append(" AND (username LIKE ? OR email LIKE ? OR full_name LIKE ?)");
            }
            
            if (role != null && !role.equals("all")) {
                sql.append(" AND role = ?");
                countSql.append(" AND role = ?");
            }
            
            if (status != null && !status.equals("all")) {
                if (status.equals("active")) {
                    sql.append(" AND is_active = 1");
                    countSql.append(" AND is_active = 1");
                } else if (status.equals("inactive")) {
                    sql.append(" AND is_active = 0");
                    countSql.append(" AND is_active = 0");
                }
            }
            
            sql.append(" ORDER BY user_id DESC LIMIT ? OFFSET ?");
            
            // Prepare count statement
            PreparedStatement countStmt = conn.prepareStatement(countSql.toString());
            int paramIndex = 1;
            
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim() + "%";
                countStmt.setString(paramIndex++, searchPattern);
                countStmt.setString(paramIndex++, searchPattern);
                countStmt.setString(paramIndex++, searchPattern);
            }
            
            if (role != null && !role.equals("all")) {
                countStmt.setString(paramIndex++, role);
            }
            
            ResultSet countRs = countStmt.executeQuery();
            int total = 0;
            if (countRs.next()) {
                total = countRs.getInt("total");
            }
            countRs.close();
            countStmt.close();
            
            // Prepare data statement
            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            paramIndex = 1;
            
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim() + "%";
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
            }
            
            if (role != null && !role.equals("all")) {
                pstmt.setString(paramIndex++, role);
            }
            
            pstmt.setInt(paramIndex++, limit);
            pstmt.setInt(paramIndex++, offset);
            
            ResultSet rs = pstmt.executeQuery();
            
            List<Map<String, Object>> users = new ArrayList<>();
            
            while (rs.next()) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", rs.getInt("user_id"));
                userData.put("username", rs.getString("username"));
                userData.put("email", rs.getString("email"));
                userData.put("fullName", rs.getString("full_name"));
                userData.put("role", rs.getString("role"));
                userData.put("isActive", rs.getBoolean("is_active"));
                userData.put("createdAt", rs.getTimestamp("created_at"));
                users.add(userData);
            }
            
            rs.close();
            pstmt.close();
            db.closeConnection();
            
            result.put("success", true);
            result.put("users", users);
            result.put("total", total);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}