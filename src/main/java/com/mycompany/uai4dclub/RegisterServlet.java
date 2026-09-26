package com.mycompany.uai4dclub;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class RegisterServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();
        
        try {
            String fullName = request.getParameter("fullName");
            String username = request.getParameter("username");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String yearOfStudyStr = request.getParameter("yearOfStudy");
            
            System.out.println("Registration attempt for: " + username);
            
            if (fullName == null || fullName.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Full name is required");
                out.print(mapper.writeValueAsString(result));
                return;
            }
            
            if (username == null || username.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Username is required");
                out.print(mapper.writeValueAsString(result));
                return;
            }
            
            if (email == null || email.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Email is required");
                out.print(mapper.writeValueAsString(result));
                return;
            }
            
            if (password == null || password.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Password is required");
                out.print(mapper.writeValueAsString(result));
                return;
            }
            
            if (!PasswordUtil.isPasswordStrong(password)) {
                result.put("success", false);
                result.put("message", "Password must be at least 8 characters with uppercase, lowercase, number, and special character");
                out.print(mapper.writeValueAsString(result));
                return;
            }
            
            UserDAO userDAO = new UserDAO();
            
            if (userDAO.usernameExists(username.trim())) {
                result.put("success", false);
                result.put("message", "Username already taken");
                out.print(mapper.writeValueAsString(result));
                return;
            }
            
            if (userDAO.emailExists(email.trim())) {
                result.put("success", false);
                result.put("message", "Email already registered");
                out.print(mapper.writeValueAsString(result));
                return;
            }
            
            User user = new User();
            user.setUsername(username.trim());
            user.setEmail(email.trim());
            user.setPasswordHash(PasswordUtil.hashPassword(password));
            user.setFullName(fullName.trim());
            user.setRole("MEMBER");
            user.setCourse("");
            
            int year = 0;
            if (yearOfStudyStr != null && !yearOfStudyStr.isEmpty()) {
                try {
                    year = Integer.parseInt(yearOfStudyStr);
                } catch (NumberFormatException e) {
                    year = 0;
                }
            }
            user.setYearOfStudy(year);
            
            if (userDAO.createUser(user)) {
                System.out.println("User created: " + username);
                result.put("success", true);
                result.put("message", "Registration successful! Please login.");
            } else {
                result.put("success", false);
                result.put("message", "Registration failed. Please try again.");
            }
            
        } catch (JsonProcessingException e) {
            System.err.println("Registration error: " + e.getMessage());
            result.put("success", false);
            result.put("message", "An error occurred: " + e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}