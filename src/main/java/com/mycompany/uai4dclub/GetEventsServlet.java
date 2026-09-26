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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetEventsServlet extends HttpServlet {
    
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
        List<Map<String, Object>> events = new ArrayList<>();
        
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            String sql = "SELECT e.*, " +
                        "(SELECT COUNT(*) FROM event_registrations WHERE event_id = e.event_id) as attendee_count, " +
                        "(SELECT COUNT(*) FROM event_registrations WHERE event_id = e.event_id AND user_id = ?) as is_registered " +
                        "FROM events e WHERE 1=1";
            
            if (filter != null && !filter.isEmpty() && !filter.equals("all")) {
                if (filter.equals("upcoming") || filter.equals("ongoing") || filter.equals("completed") || filter.equals("cancelled")) {
                    sql += " AND LOWER(e.status) = ?";
                } else {
                    sql += " AND LOWER(e.event_type) = ?";
                }
            }
            
            sql += " ORDER BY e.event_date ASC";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            int paramIndex = 2;
            if (filter != null && !filter.isEmpty() && !filter.equals("all")) {
                pstmt.setString(paramIndex, filter.toLowerCase());
            }
            
            ResultSet rs = pstmt.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            
            while (rs.next()) {
                Map<String, Object> event = new HashMap<>();
                event.put("id", rs.getInt("event_id"));
                event.put("title", rs.getString("title"));
                event.put("description", rs.getString("description"));
                event.put("date", rs.getTimestamp("event_date") != null ? dateFormat.format(rs.getTimestamp("event_date")) : "");
                event.put("location", rs.getString("location"));
                event.put("status", rs.getString("status") != null ? rs.getString("status") : "UPCOMING");
                event.put("type", rs.getString("event_type") != null ? rs.getString("event_type") : "MEETUP");
                event.put("capacity", rs.getInt("max_attendees"));
                event.put("attendees", rs.getInt("attendee_count"));
                event.put("registered", rs.getInt("is_registered") > 0);
                events.add(event);
            }
            
            rs.close();
            pstmt.close();
            db.closeConnection();
            
            result.put("success", true);
            result.put("events", events);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error loading events: " + e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}