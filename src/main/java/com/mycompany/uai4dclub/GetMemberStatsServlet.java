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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetMemberStatsServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
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
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            // ===== TOTAL MEMBERS =====
            String totalSql = "SELECT COUNT(*) as total FROM users WHERE role != 'ADMIN'";
            PreparedStatement totalStmt = conn.prepareStatement(totalSql);
            ResultSet totalRs = totalStmt.executeQuery();
            int totalMembers = 0;
            if (totalRs.next()) {
                totalMembers = totalRs.getInt("total");
            }
            totalRs.close();
            totalStmt.close();
            
            // ===== ACTIVE MEMBERS =====
            String activeSql = "SELECT COUNT(*) as total FROM users WHERE role != 'ADMIN' AND is_active = 1";
            PreparedStatement activeStmt = conn.prepareStatement(activeSql);
            ResultSet activeRs = activeStmt.executeQuery();
            int activeMembers = 0;
            if (activeRs.next()) {
                activeMembers = activeRs.getInt("total");
            }
            activeRs.close();
            activeStmt.close();
            
            // ===== COMPLETED PROFILES =====
            String completedSql = "SELECT COUNT(*) as total FROM users WHERE role != 'ADMIN' AND full_name IS NOT NULL AND full_name != '' "
                + "AND course IS NOT NULL AND course != '' AND year_of_study > 0 "
                + "AND university IS NOT NULL AND university != '' AND ai_experience IS NOT NULL AND ai_experience != '' "
                + "AND skills IS NOT NULL AND skills != '' AND bio IS NOT NULL AND bio != ''";
            PreparedStatement completedStmt = conn.prepareStatement(completedSql);
            ResultSet completedRs = completedStmt.executeQuery();
            int completedProfiles = 0;
            if (completedRs.next()) {
                completedProfiles = completedRs.getInt("total");
            }
            completedRs.close();
            completedStmt.close();
            
            // ===== AI EXPERTS (Advanced + Expert) =====
            String expertSql = "SELECT COUNT(*) as total FROM users WHERE role != 'ADMIN' AND ai_experience IN ('advanced', 'expert')";
            PreparedStatement expertStmt = conn.prepareStatement(expertSql);
            ResultSet expertRs = expertStmt.executeQuery();
            int aiExperts = 0;
            if (expertRs.next()) {
                aiExperts = expertRs.getInt("total");
            }
            expertRs.close();
            expertStmt.close();
            
            // ===== YEAR DISTRIBUTION =====
            Map<String, Integer> yearDistribution = new HashMap<>();
            String yearSql = "SELECT year_of_study, COUNT(*) as count FROM users WHERE role != 'ADMIN' AND year_of_study > 0 GROUP BY year_of_study";
            PreparedStatement yearStmt = conn.prepareStatement(yearSql);
            ResultSet yearRs = yearStmt.executeQuery();
            while (yearRs.next()) {
                yearDistribution.put(String.valueOf(yearRs.getInt("year_of_study")), yearRs.getInt("count"));
            }
            yearRs.close();
            yearStmt.close();
            
            // ===== AI EXPERIENCE DISTRIBUTION =====
            Map<String, Integer> aiExpDistribution = new HashMap<>();
            String expSql = "SELECT ai_experience, COUNT(*) as count FROM users WHERE role != 'ADMIN' AND ai_experience IS NOT NULL AND ai_experience != '' GROUP BY ai_experience";
            PreparedStatement expStmt = conn.prepareStatement(expSql);
            ResultSet expRs = expStmt.executeQuery();
            while (expRs.next()) {
                aiExpDistribution.put(expRs.getString("ai_experience"), expRs.getInt("count"));
            }
            expRs.close();
            expStmt.close();
            
            // ===== TOP SKILLS =====
            List<Map<String, Object>> topSkills = new ArrayList<>();
            // This is a simplified version - in production, you'd parse skills properly
            String skillSql = "SELECT skills FROM users WHERE role != 'ADMIN' AND skills IS NOT NULL AND skills != ''";
            PreparedStatement skillStmt = conn.prepareStatement(skillSql);
            ResultSet skillRs = skillStmt.executeQuery();
            Map<String, Integer> skillCount = new HashMap<>();
            while (skillRs.next()) {
                String skills = skillRs.getString("skills");
                if (skills != null) {
                    String[] skillArray = skills.split(",");
                    for (String skill : skillArray) {
                        String trimmed = skill.trim();
                        if (!trimmed.isEmpty()) {
                            skillCount.put(trimmed, skillCount.getOrDefault(trimmed, 0) + 1);
                        }
                    }
                }
            }
            skillRs.close();
            skillStmt.close();
            
            skillCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {
                    Map<String, Object> skill = new HashMap<>();
                    skill.put("skill", entry.getKey());
                    skill.put("count", entry.getValue());
                    topSkills.add(skill);
                });
            
            // ===== TOP UNIVERSITY =====
            String uniSql = "SELECT university, COUNT(*) as count FROM users WHERE role != 'ADMIN' AND university IS NOT NULL AND university != '' GROUP BY university ORDER BY count DESC LIMIT 1";
            PreparedStatement uniStmt = conn.prepareStatement(uniSql);
            ResultSet uniRs = uniStmt.executeQuery();
            Map<String, Object> topUniversity = null;
            if (uniRs.next()) {
                topUniversity = new HashMap<>();
                topUniversity.put("university", uniRs.getString("university"));
                topUniversity.put("count", uniRs.getInt("count"));
            }
            uniRs.close();
            uniStmt.close();
            
            // ===== MEMBER LIST WITH COMPLETION =====
            List<Map<String, Object>> members = new ArrayList<>();
            String memberSql = "SELECT user_id, full_name, email, course, university, ai_experience, skills, bio, year_of_study FROM users WHERE role != 'ADMIN' ORDER BY user_id DESC LIMIT 50";
            PreparedStatement memberStmt = conn.prepareStatement(memberSql);
            ResultSet memberRs = memberStmt.executeQuery();
            while (memberRs.next()) {
                Map<String, Object> member = new HashMap<>();
                member.put("userId", memberRs.getInt("user_id"));
                member.put("fullName", memberRs.getString("full_name"));
                member.put("email", memberRs.getString("email"));
                member.put("course", memberRs.getString("course"));
                member.put("university", memberRs.getString("university"));
                member.put("aiExperience", memberRs.getString("ai_experience"));
                
                // Calculate completion percentage
                int completion = 0;
                if (memberRs.getString("full_name") != null && !memberRs.getString("full_name").isEmpty()) completion += 15;
                if (memberRs.getString("course") != null && !memberRs.getString("course").isEmpty()) completion += 15;
                if (memberRs.getInt("year_of_study") > 0) completion += 15;
                if (memberRs.getString("university") != null && !memberRs.getString("university").isEmpty()) completion += 15;
                if (memberRs.getString("ai_experience") != null && !memberRs.getString("ai_experience").isEmpty()) completion += 15;
                if (memberRs.getString("skills") != null && !memberRs.getString("skills").isEmpty()) completion += 15;
                if (memberRs.getString("bio") != null && !memberRs.getString("bio").isEmpty()) completion += 10;
                
                member.put("completionPercentage", completion);
                members.add(member);
            }
            memberRs.close();
            memberStmt.close();
            
            db.closeConnection();
            
            result.put("success", true);
            result.put("totalMembers", totalMembers);
            result.put("activeMembers", activeMembers);
            result.put("completedProfiles", completedProfiles);
            result.put("aiExperts", aiExperts);
            result.put("yearDistribution", yearDistribution);
            result.put("aiExperienceDistribution", aiExpDistribution);
            result.put("topSkills", topSkills);
            result.put("topUniversity", topUniversity);
            result.put("members", members);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
        }
        
        out.print(mapper.writeValueAsString(result));
        out.flush();
    }
}