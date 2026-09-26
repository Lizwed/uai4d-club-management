package com.mycompany.uai4dclub;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String username;
    private String email;
    private String passwordHash;
    private String fullName;
    private String role;
    private String profilePic;
    private String course;
    private int yearOfStudy;
    private String skills;
    private String bio;
    private boolean isActive;
    private boolean emailVerified;
    private String googleId;
    private String oauthProvider;
    private String oauthId;
    private Timestamp lastLogin;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // ===== NEW PROFILE FIELDS =====
    private String university;
    private String aiExperience;
    private String interests;
    private int eventCount;
    private int projectCount;
    
    public User() {
        this.role = "MEMBER";
        this.isActive = true;
        this.emailVerified = false;
        this.profilePic = "default_profile.png";
        this.course = "";
        this.skills = "";
        this.bio = "";
        this.university = "";
        this.aiExperience = "";
        this.interests = "";
        this.eventCount = 0;
        this.projectCount = 0;
    }
    
    public User(String username, String email, String passwordHash, String fullName) {
        this();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }
    
    // ===== ORIGINAL GETTERS AND SETTERS =====
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getProfilePic() { return profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }
    
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course != null ? course : ""; }
    
    public int getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(int yearOfStudy) { this.yearOfStudy = yearOfStudy; }
    
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills != null ? skills : ""; }
    
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio != null ? bio : ""; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
    
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    
    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }
    
    public String getOauthProvider() { return oauthProvider; }
    public void setOauthProvider(String oauthProvider) { this.oauthProvider = oauthProvider; }
    
    public String getOauthId() { return oauthId; }
    public void setOauthId(String oauthId) { this.oauthId = oauthId; }
    
    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    
    public boolean isOAuthUser() {
        return oauthProvider != null && !oauthProvider.isEmpty();
    }
    
    // ===== NEW GETTERS AND SETTERS =====
    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university != null ? university : ""; }
    
    public String getAiExperience() { return aiExperience; }
    public void setAiExperience(String aiExperience) { this.aiExperience = aiExperience != null ? aiExperience : ""; }
    
    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests != null ? interests : ""; }
    
    public int getEventCount() { return eventCount; }
    public void setEventCount(int eventCount) { this.eventCount = eventCount; }
    
    public int getProjectCount() { return projectCount; }
    public void setProjectCount(int projectCount) { this.projectCount = projectCount; }
    
    // ===== HELPER METHODS =====
    public boolean isProfileComplete() {
        return fullName != null && !fullName.isEmpty()
            && course != null && !course.isEmpty()
            && yearOfStudy > 0
            && university != null && !university.isEmpty()
            && aiExperience != null && !aiExperience.isEmpty()
            && skills != null && !skills.isEmpty()
            && bio != null && !bio.isEmpty();
    }
    
    public int getProfileCompletionPercentage() {
        int total = 7;
        int completed = 0;
        if (fullName != null && !fullName.isEmpty()) completed++;
        if (course != null && !course.isEmpty()) completed++;
        if (yearOfStudy > 0) completed++;
        if (university != null && !university.isEmpty()) completed++;
        if (aiExperience != null && !aiExperience.isEmpty()) completed++;
        if (skills != null && !skills.isEmpty()) completed++;
        if (bio != null && !bio.isEmpty()) completed++;
        return (int) Math.round((completed * 100.0) / total);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", course='" + course + '\'' +
                ", university='" + university + '\'' +
                ", aiExperience='" + aiExperience + '\'' +
                ", active=" + isActive +
                '}';
    }
}