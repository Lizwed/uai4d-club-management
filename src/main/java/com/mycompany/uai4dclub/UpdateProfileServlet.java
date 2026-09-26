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

public class UpdateProfileServlet extends HttpServlet {
    
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
        
        // Get all parameters including new fields
        String fullName = request.getParameter("fullName");
        String course = request.getParameter("course");
        String yearOfStudyStr = request.getParameter("yearOfStudy");
        String skills = request.getParameter("skills");
        String bio = request.getParameter("bio");
        String university = request.getParameter("university");
        String aiExperience = request.getParameter("aiExperience");
        String interests = request.getParameter("interests");
        
        UserDAO userDAO = new UserDAO();
        User user = userDAO.findUserById(currentUser.getUserId());
        
        if (user == null) {
            result.put("success", false);
            result.put("message", "User not found");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        // Update all fields
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }
        user.setCourse(course != null ? course.trim() : "");
        if (yearOfStudyStr != null && !yearOfStudyStr.isEmpty()) {
            try {
                user.setYearOfStudy(Integer.parseInt(yearOfStudyStr));
            } catch (NumberFormatException e) {
                user.setYearOfStudy(0);
            }
        }
        user.setSkills(skills != null ? skills.trim() : "");
        user.setBio(bio != null ? bio.trim() : "");
        
        // ===== NEW FIELDS =====
        user.setUniversity(university != null ? university.trim() : "");
        user.setAiExperience(aiExperience != null ? aiExperience.trim() : "");
        user.setInterests(interests != null ? interests.trim() : "");
        
        if (userDAO.updateUser(user)) {
            // Update session
            session.setAttribute("user", user);
            result.put("success", true);
            result.put("message", "Profile updated successfully");
        } else {
            result.put("success", false);
            result.put("message", "Failed to update profile");
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}