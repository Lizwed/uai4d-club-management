package com.mycompany.uai4dclub;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class ChangePasswordServlet extends HttpServlet {
    
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
        
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        
        if (currentPassword == null || currentPassword.trim().isEmpty() ||
            newPassword == null || newPassword.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "All fields are required");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        if (!PasswordUtil.isPasswordStrong(newPassword)) {
            result.put("success", false);
            result.put("message", "New password must be at least 8 characters with uppercase, lowercase, number, and special character");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        UserDAO userDAO = new UserDAO();
        User user = userDAO.findUserById(currentUser.getUserId());
        
        if (user == null) {
            result.put("success", false);
            result.put("message", "User not found");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        if (!PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash())) {
            result.put("success", false);
            result.put("message", "Current password is incorrect");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        String newHashedPassword = PasswordUtil.hashPassword(newPassword);
        if (userDAO.updatePassword(user.getUserId(), newHashedPassword)) {
            result.put("success", true);
            result.put("message", "Password changed successfully");
        } else {
            result.put("success", false);
            result.put("message", "Failed to change password");
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}