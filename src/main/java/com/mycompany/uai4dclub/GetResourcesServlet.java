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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetResourcesServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();
        
        String filter = request.getParameter("filter");
        String search = request.getParameter("search");
        
        List<Map<String, Object>> resources = new ArrayList<>();
        
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            String sql = "SELECT r.*, u.full_name as author_name " +
                        "FROM resources r " +
                        "LEFT JOIN users u ON r.uploaded_by = u.user_id " +
                        "WHERE 1=1";
            
            if (filter != null && !filter.isEmpty() && !filter.equals("all")) {
                sql += " AND LOWER(r.resource_type) = ?";
            }
            
            if (search != null && !search.isEmpty()) {
                sql += " AND (LOWER(r.title) LIKE ? OR LOWER(r.description) LIKE ?)";
            }
            
            sql += " ORDER BY r.created_at DESC";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            int paramIndex = 1;
            
            if (filter != null && !filter.isEmpty() && !filter.equals("all")) {
                pstmt.setString(paramIndex++, filter.toLowerCase());
            }
            
            if (search != null && !search.isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> resource = new HashMap<>();
                resource.put("id", rs.getInt("resource_id"));
                resource.put("title", rs.getString("title"));
                resource.put("description", rs.getString("description"));
                resource.put("type", rs.getString("resource_type") != null ? rs.getString("resource_type") : "TUTORIAL");
                resource.put("url", rs.getString("resource_url"));
                resource.put("author", rs.getString("author_name") != null ? rs.getString("author_name") : "UAI4D");
                resource.put("views", rs.getInt("view_count"));
                resource.put("downloads", rs.getInt("download_count"));
                resources.add(resource);
            }
            
            rs.close();
            pstmt.close();
            db.closeConnection();
            
            result.put("success", true);
            result.put("resources", resources);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error loading resources: " + e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}