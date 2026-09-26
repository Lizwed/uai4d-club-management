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

public class GetUserInfoServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();
        
        HttpSession session = request.getSession(false);
        
        // Check if session exists and has user
        if (session == null || session.getAttribute("user") == null) {
            result.put("success", false);
            result.put("message", "Not logged in");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        // Reload user from database to get latest data
        UserDAO userDAO = new UserDAO();
        User dbUser = userDAO.findUserById(user.getUserId());
        
        if (dbUser == null) {
            dbUser = user;
        }
        
        result.put("success", true);
        result.put("userId", dbUser.getUserId());
        result.put("fullName", dbUser.getFullName() != null ? dbUser.getFullName() : "Member");
        result.put("username", dbUser.getUsername() != null ? dbUser.getUsername() : "member");
        result.put("role", dbUser.getRole() != null ? dbUser.getRole() : "MEMBER");
        result.put("email", dbUser.getEmail() != null ? dbUser.getEmail() : "member@uai4d.com");
        result.put("course", dbUser.getCourse() != null ? dbUser.getCourse() : "");
        result.put("yearOfStudy", dbUser.getYearOfStudy());
        result.put("skills", dbUser.getSkills() != null ? dbUser.getSkills() : "");
        result.put("bio", dbUser.getBio() != null ? dbUser.getBio() : "");
        result.put("university", dbUser.getUniversity() != null ? dbUser.getUniversity() : "");
        result.put("aiExperience", dbUser.getAiExperience() != null ? dbUser.getAiExperience() : "");
        result.put("interests", dbUser.getInterests() != null ? dbUser.getInterests() : "");
        
        // Get stats
        Map<String, Object> stats = new HashMap<>();
        stats.put("members", userDAO.countUsers());
        stats.put("events", 0);
        stats.put("projects", 0);
        stats.put("resources", 0);
        
        result.put("stats", stats);
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}