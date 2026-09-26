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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class CreateEventServlet extends HttpServlet {
    
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
            String eventDate = request.getParameter("eventDate");
            String location = request.getParameter("location");
            String eventType = request.getParameter("eventType");
            String maxAttendeesStr = request.getParameter("maxAttendees");
            String status = request.getParameter("status");
            
            // Validate
            if (title == null || title.trim().isEmpty() ||
                description == null || description.trim().isEmpty() ||
                eventDate == null || eventDate.trim().isEmpty() ||
                location == null || location.trim().isEmpty() ||
                eventType == null || eventType.trim().isEmpty()) {
                
                result.put("success", false);
                result.put("message", "All required fields must be filled");
                out.print(mapper.writeValueAsString(result));
                return;
            }
            
            int maxAttendees = 50;
            if (maxAttendeesStr != null && !maxAttendeesStr.isEmpty()) {
                maxAttendees = Integer.parseInt(maxAttendeesStr);
            }
            
            // Parse date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            java.util.Date parsedDate = dateFormat.parse(eventDate);
            Timestamp timestamp = new Timestamp(parsedDate.getTime());
            
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            String sql = "INSERT INTO events (title, description, event_date, location, max_attendees, event_type, status, created_by) "
                       + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, title.trim());
            pstmt.setString(2, description.trim());
            pstmt.setTimestamp(3, timestamp);
            pstmt.setString(4, location.trim());
            pstmt.setInt(5, maxAttendees);
            pstmt.setString(6, eventType);
            pstmt.setString(7, status != null ? status : "UPCOMING");
            pstmt.setInt(8, currentUser.getUserId());
            
            int affected = pstmt.executeUpdate();
            pstmt.close();
            db.closeConnection();
            
            if (affected > 0) {
                result.put("success", true);
                result.put("message", "Event created successfully");
            } else {
                result.put("success", false);
                result.put("message", "Failed to create event");
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