package com.mycompany.uai4dclub;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SecurityFilter implements Filter {
    
    // Public paths (no login required)
    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
        "/login.html",
        "/register.html",
        "/forgot-password.html",
        "/reset-password.html",
        "/terms-of-service.html",
        "/privacy-policy.html",
        "/LoginServlet",
        "/RegisterServlet",
        "/CheckUsernameServlet",
        "/ForgotPasswordServlet",
        "/ResetPasswordServlet",
        "/GetSecurityQuestionsServlet",
        "/VerifySecurityAnswersServlet",
        "/ResetPasswordFromSecurityServlet",
        "/CheckSessionServlet",
        "/oauth2callback",
        "/css/",
        "/js/",
        "/images/"
    ));
    
   // Admin only paths
private static final Set<String> ADMIN_PATHS = new HashSet<>(Arrays.asList(
    "/admin.html",
    "/manage-users.html",
    "/member-stats.html",
    "/GetAdminStatsServlet",
    "/GetUsersServlet",
    "/UpdateUserRoleServlet",
    "/ToggleUserStatusServlet",
    "/ResetUserPasswordServlet",
    "/DeleteEventServlet",
    "/DeleteProjectServlet",
    "/DeleteResourceServlet",
    "/GetMemberStatsServlet"
));
    
    // Member only paths (require login but not admin)
    private static final Set<String> MEMBER_PATHS = new HashSet<>(Arrays.asList(
        "/dashboard.html",
        "/profile.html",
        "/events.html",
        "/projects.html",
        "/resources.html",
        "/create-event.html",
        "/create-project.html",
        "/add-resource.html",
        "/GetUserInfoServlet",
        "/GetAnnouncementsServlet",
        "/GetDashboardStatsServlet",
        "/GetEventsServlet",
        "/RegisterEventServlet",
        "/GetProjectsServlet",
        "/JoinProjectServlet",
        "/GetResourcesServlet",
        "/UpdateProfileServlet",
        "/ChangePasswordServlet"
    ));
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("🔒 SecurityFilter initialized!");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        
        String path = req.getRequestURI().substring(req.getContextPath().length());
        
        // ===== CHECK IF PATH IS PUBLIC =====
        boolean isPublicPath = isPublicPath(path);
        
        if (isPublicPath) {
            chain.doFilter(request, response);
            return;
        }
        
        // ===== CHECK IF USER IS LOGGED IN =====
        User user = null;
        if (session != null && session.getAttribute("user") != null) {
            user = (User) session.getAttribute("user");
        }
        
        if (user == null) {
            // Not logged in, redirect to login
            res.sendRedirect(req.getContextPath() + "/login.html");
            return;
        }
        
        // ===== CHECK IF USER IS ACTIVE =====
        if (!user.isActive()) {
            session.invalidate();
            res.sendRedirect(req.getContextPath() + "/login.html?error=Account deactivated");
            return;
        }
        
        // ===== CHECK ADMIN PATHS =====
        if (isAdminPath(path)) {
            if (!"ADMIN".equals(user.getRole())) {
                // Not admin, redirect to dashboard with error
                res.sendRedirect(req.getContextPath() + "/dashboard.html?error=Access denied");
                return;
            }
        }
        
        // ===== CHECK MEMBER PATHS =====
        if (isMemberPath(path)) {
            // All logged-in users can access member paths
            // Continue
        }
        
        // ===== UPDATE LAST ACTIVITY =====
        session.setAttribute("lastActivity", System.currentTimeMillis());
        
        // ===== CONTINUE =====
        chain.doFilter(request, response);
    }
    
    private boolean isPublicPath(String path) {
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isAdminPath(String path) {
        for (String adminPath : ADMIN_PATHS) {
            if (path.startsWith(adminPath)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isMemberPath(String path) {
        for (String memberPath : MEMBER_PATHS) {
            if (path.startsWith(memberPath)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void destroy() {
        System.out.println("🔒 SecurityFilter destroyed!");
    }
}