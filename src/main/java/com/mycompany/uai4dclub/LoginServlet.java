package com.mycompany.uai4dclub;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.io.PrintWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class LoginServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = new HashMap<>();
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");
        
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            
            result.put("success", false);
            result.put("message", "Username and password are required");
            out.print(mapper.writeValueAsString(result));
            return;
        }
        
        UserDAO userDAO = new UserDAO();
        User user = userDAO.findUserByUsername(username.trim());
        
        if (user == null) {
            user = userDAO.findUserByEmail(username.trim());
        }
        
        if (user != null && user.isActive()) {
            if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                
                // ===== CREATE SESSION =====
                HttpSession session = request.getSession();
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("role", user.getRole());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("fullName", user.getFullName());
                session.setAttribute("email", user.getEmail());
                session.setMaxInactiveInterval(60 * 60 * 24); // 24 hours
                
                // ===== UPDATE LAST LOGIN =====
                userDAO.updateLastLogin(user.getUsername());
                
                // ===== REMEMBER ME COOKIE =====
                if ("on".equals(rememberMe)) {
                    Cookie rememberCookie = new Cookie("UAI4D_REMEMBER", user.getUsername());
                    rememberCookie.setMaxAge(60 * 60 * 24 * 30); // 30 days
                    rememberCookie.setHttpOnly(true);
                    rememberCookie.setSecure(true);
                    rememberCookie.setPath("/");
                    response.addCookie(rememberCookie);
                }
                
                // ===== SESSION COOKIE =====
                Cookie sessionCookie = new Cookie("UAI4D_SESSION", session.getId());
                sessionCookie.setHttpOnly(true);
                sessionCookie.setSecure(true);
                sessionCookie.setPath("/");
                response.addCookie(sessionCookie);
                
                // ===== LOG SUCCESS =====
                System.out.println("✅ User logged in: " + user.getUsername() + " (Role: " + user.getRole() + ")");
                
                result.put("success", true);
                result.put("message", "Login successful!");
                result.put("redirect", "dashboard.html");
                result.put("role", user.getRole());
                result.put("userId", user.getUserId());
                
            } else {
                result.put("success", false);
                result.put("message", "Invalid password");
            }
        } else {
            result.put("success", false);
            result.put("message", "User not found or inactive");
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // ===== LOGOUT =====
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Clear session attributes
            session.removeAttribute("user");
            session.removeAttribute("userId");
            session.removeAttribute("role");
            session.removeAttribute("username");
            session.removeAttribute("fullName");
            session.removeAttribute("email");
            session.invalidate();
        }
        
        // ===== CLEAR COOKIES =====
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("UAI4D_REMEMBER".equals(cookie.getName()) || 
                    "UAI4D_SESSION".equals(cookie.getName())) {
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }
            }
        }
        
        System.out.println("🔓 User logged out");
        response.sendRedirect("login.html");
    }
}