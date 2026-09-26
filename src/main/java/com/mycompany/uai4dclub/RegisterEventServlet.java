package com.mycompany.uai4dclub;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class RegisterEventServlet extends HttpServlet {
    
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
            result.put("message", "Please login to register");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        User user = (User) session.getAttribute("user");
        String eventIdStr = request.getParameter("eventId");
        
        if (eventIdStr == null || eventIdStr.isEmpty()) {
            result.put("success", false);
            result.put("message", "Event ID is required");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        try {
            int eventId = Integer.parseInt(eventIdStr);
            
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            CallableStatement cstmt = conn.prepareCall("{call register_for_event(?, ?)}");
            cstmt.setInt(1, user.getUserId());
            cstmt.setInt(2, eventId);
            
            cstmt.execute();
            
            cstmt.close();
            db.closeConnection();
            
            result.put("success", true);
            result.put("message", "Successfully registered for event");
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}