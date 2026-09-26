package com.mycompany.uai4dclub;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();
        
        String email = request.getParameter("email");
        
        if (email == null || email.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Email is required");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        UserDAO userDAO = new UserDAO();
        User user = userDAO.findUserByEmail(email.trim());
        
        if (user == null) {
            // Don't reveal if email exists for security
            result.put("success", true);
            result.put("message", "If the email exists, a reset link has been sent");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        // Create reset token
        PasswordResetDAO resetDAO = new PasswordResetDAO();
        String token = resetDAO.createResetToken(user.getUserId(), 1440); // 24 hours
        
        if (token != null) {
            // In production, send email here
            String resetLink = "http://localhost:8080/UAI4DClub/reset-password.html?token=" + token;
            System.out.println("🔐 Reset link: " + resetLink);
            
            // TODO: Send email with reset link
            // EmailUtil.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetLink);
            
            result.put("success", true);
            result.put("message", "Reset link sent to your email");
        } else {
            result.put("success", false);
            result.put("message", "Failed to generate reset token");
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}