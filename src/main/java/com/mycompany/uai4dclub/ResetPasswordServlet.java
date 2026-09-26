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

public class ResetPasswordServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();
        
        String token = request.getParameter("token");
        String password = request.getParameter("password");
        
        if (token == null || token.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Invalid reset token");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Password is required");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        // Validate password strength
        if (!PasswordUtil.isPasswordStrong(password)) {
            result.put("success", false);
            result.put("message", "Password must be at least 8 characters with uppercase, lowercase, number, and special character");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        PasswordResetDAO resetDAO = new PasswordResetDAO();
        int userId = resetDAO.validateToken(token.trim());
        
        if (userId == -1) {
            result.put("success", false);
            result.put("message", "Invalid or expired reset token. Please request a new one.");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        // Update password
        UserDAO userDAO = new UserDAO();
        String hashedPassword = PasswordUtil.hashPassword(password);
        boolean updated = userDAO.updatePassword(userId, hashedPassword);
        
        if (updated) {
            resetDAO.markTokenUsed(token.trim());
            result.put("success", true);
            result.put("message", "Password reset successful!");
        } else {
            result.put("success", false);
            result.put("message", "Failed to reset password. Please try again.");
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}