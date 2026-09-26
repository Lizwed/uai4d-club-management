package com.mycompany.uai4dclub;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class ExportReportServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String format = request.getParameter("format");
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("user") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        User currentUser = (User) session.getAttribute("user");
        
        if (!"ADMIN".equals(currentUser.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        try {
            // Get member data
            List<Map<String, Object>> members = getMemberData();
            Map<String, Object> stats = getStats();
            
            if ("pdf".equals(format)) {
                exportPDF(response, members, stats);
            } else if ("excel".equals(format)) {
                exportExcel(response, members, stats);
            } else if ("csv".equals(format)) {
                exportCSV(response, members);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private List<Map<String, Object>> getMemberData() throws Exception {
        List<Map<String, Object>> members = new ArrayList<>();
        DatabaseConnection db = DatabaseConnection.getInstance();
        Connection conn = db.getConnection();
        
        String sql = "SELECT user_id, username, full_name, email, role, course, year_of_study, "
                   + "university, ai_experience, skills, bio, is_active, created_at "
                   + "FROM users WHERE role != 'ADMIN' ORDER BY user_id DESC";
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        while (rs.next()) {
            Map<String, Object> member = new HashMap<>();
            member.put("userId", rs.getInt("user_id"));
            member.put("username", rs.getString("username"));
            member.put("fullName", rs.getString("full_name") != null ? rs.getString("full_name") : "");
            member.put("email", rs.getString("email"));
            member.put("role", rs.getString("role"));
            member.put("course", rs.getString("course") != null ? rs.getString("course") : "");
            member.put("yearOfStudy", rs.getInt("year_of_study"));
            member.put("university", rs.getString("university") != null ? rs.getString("university") : "");
            member.put("aiExperience", rs.getString("ai_experience") != null ? rs.getString("ai_experience") : "");
            member.put("skills", rs.getString("skills") != null ? rs.getString("skills") : "");
            member.put("bio", rs.getString("bio") != null ? rs.getString("bio") : "");
            member.put("isActive", rs.getBoolean("is_active"));
            member.put("createdAt", rs.getTimestamp("created_at") != null ? dateFormat.format(rs.getTimestamp("created_at")) : "");
            members.add(member);
        }
        
        rs.close();
        stmt.close();
        db.closeConnection();
        return members;
    }
    
    private Map<String, Object> getStats() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        DatabaseConnection db = DatabaseConnection.getInstance();
        Connection conn = db.getConnection();
        
        // Total members
        String totalSql = "SELECT COUNT(*) as total FROM users WHERE role != 'ADMIN'";
        PreparedStatement totalStmt = conn.prepareStatement(totalSql);
        ResultSet totalRs = totalStmt.executeQuery();
        if (totalRs.next()) {
            stats.put("totalMembers", totalRs.getInt("total"));
        }
        totalRs.close();
        totalStmt.close();
        
        // Active members
        String activeSql = "SELECT COUNT(*) as total FROM users WHERE role != 'ADMIN' AND is_active = 1";
        PreparedStatement activeStmt = conn.prepareStatement(activeSql);
        ResultSet activeRs = activeStmt.executeQuery();
        if (activeRs.next()) {
            stats.put("activeMembers", activeRs.getInt("total"));
        }
        activeRs.close();
        activeStmt.close();
        
        // Completed profiles
        String completedSql = "SELECT COUNT(*) as total FROM users WHERE role != 'ADMIN' AND full_name IS NOT NULL AND full_name != '' "
            + "AND course IS NOT NULL AND course != '' AND year_of_study > 0 "
            + "AND university IS NOT NULL AND university != '' AND ai_experience IS NOT NULL AND ai_experience != '' "
            + "AND skills IS NOT NULL AND skills != '' AND bio IS NOT NULL AND bio != ''";
        PreparedStatement completedStmt = conn.prepareStatement(completedSql);
        ResultSet completedRs = completedStmt.executeQuery();
        if (completedRs.next()) {
            stats.put("completedProfiles", completedRs.getInt("total"));
        }
        completedRs.close();
        completedStmt.close();
        
        // AI Experts
        String expertSql = "SELECT COUNT(*) as total FROM users WHERE role != 'ADMIN' AND ai_experience IN ('advanced', 'expert')";
        PreparedStatement expertStmt = conn.prepareStatement(expertSql);
        ResultSet expertRs = expertStmt.executeQuery();
        if (expertRs.next()) {
            stats.put("aiExperts", expertRs.getInt("total"));
        }
        expertRs.close();
        expertStmt.close();
        
        db.closeConnection();
        return stats;
    }
    
    private void exportPDF(HttpServletResponse response, List<Map<String, Object>> members, Map<String, Object> stats) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=UAI4D_Member_Report.pdf");
        
        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // Title
        document.add(new Paragraph("UAI4D Club - Member Statistics Report")
            .setFontSize(20)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER));
        
        document.add(new Paragraph("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
            .setFontSize(12)
            .setTextAlignment(TextAlignment.CENTER));
        
        document.add(new Paragraph(" "));
        
        // Stats Summary
        document.add(new Paragraph("Summary Statistics").setFontSize(16).setBold());
        document.add(new Paragraph("Total Members: " + stats.get("totalMembers")));
        document.add(new Paragraph("Active Members: " + stats.get("activeMembers")));
        document.add(new Paragraph("Completed Profiles: " + stats.get("completedProfiles")));
        document.add(new Paragraph("AI Experts: " + stats.get("aiExperts")));
        document.add(new Paragraph(" "));
        
        // Member Table
        document.add(new Paragraph("Member Details").setFontSize(16).setBold());
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 15, 15, 20, 12, 13, 10, 10}));
        table.setWidth(UnitValue.createPercentValue(100));
        
        // Headers
        String[] headers = {"ID", "Username", "Full Name", "Email", "Course", "University", "AI Level", "Status"};
        for (String header : headers) {
            table.addCell(new Cell().add(new Paragraph(header).setBold()));
        }
        
        // Data rows
        for (Map<String, Object> member : members) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(member.get("userId")))));
            table.addCell(new Cell().add(new Paragraph((String) member.get("username"))));
            table.addCell(new Cell().add(new Paragraph((String) member.get("fullName"))));
            table.addCell(new Cell().add(new Paragraph((String) member.get("email"))));
            table.addCell(new Cell().add(new Paragraph((String) member.get("course"))));
            table.addCell(new Cell().add(new Paragraph((String) member.get("university"))));
            table.addCell(new Cell().add(new Paragraph((String) member.get("aiExperience"))));
            table.addCell(new Cell().add(new Paragraph((Boolean) member.get("isActive") ? "Active" : "Inactive")));
        }
        
        document.add(table);
        document.close();
    }
    
    private void exportExcel(HttpServletResponse response, List<Map<String, Object>> members, Map<String, Object> stats) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=UAI4D_Member_Report.xlsx");
        
        Workbook workbook = new XSSFWorkbook();
        
        // Stats Sheet
        Sheet statsSheet = workbook.createSheet("Summary");
        Row statsRow1 = statsSheet.createRow(0);
        statsRow1.createCell(0).setCellValue("Total Members");
        statsRow1.createCell(1).setCellValue((Integer) stats.get("totalMembers"));
        Row statsRow2 = statsSheet.createRow(1);
        statsRow2.createCell(0).setCellValue("Active Members");
        statsRow2.createCell(1).setCellValue((Integer) stats.get("activeMembers"));
        Row statsRow3 = statsSheet.createRow(2);
        statsRow3.createCell(0).setCellValue("Completed Profiles");
        statsRow3.createCell(1).setCellValue((Integer) stats.get("completedProfiles"));
        Row statsRow4 = statsSheet.createRow(3);
        statsRow4.createCell(0).setCellValue("AI Experts");
        statsRow4.createCell(1).setCellValue((Integer) stats.get("aiExperts"));
        
        // Members Sheet
        Sheet memberSheet = workbook.createSheet("Members");
        
        // Header
        Row header = memberSheet.createRow(0);
        String[] headers = {"ID", "Username", "Full Name", "Email", "Role", "Course", "Year", "University", "AI Level", "Skills", "Bio", "Status", "Joined"};
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }
        
        // Data
        int rowNum = 1;
        for (Map<String, Object> member : members) {
            Row row = memberSheet.createRow(rowNum++);
            row.createCell(0).setCellValue((Integer) member.get("userId"));
            row.createCell(1).setCellValue((String) member.get("username"));
            row.createCell(2).setCellValue((String) member.get("fullName"));
            row.createCell(3).setCellValue((String) member.get("email"));
            row.createCell(4).setCellValue((String) member.get("role"));
            row.createCell(5).setCellValue((String) member.get("course"));
            row.createCell(6).setCellValue((Integer) member.get("yearOfStudy"));
            row.createCell(7).setCellValue((String) member.get("university"));
            row.createCell(8).setCellValue((String) member.get("aiExperience"));
            row.createCell(9).setCellValue((String) member.get("skills"));
            row.createCell(10).setCellValue((String) member.get("bio"));
            row.createCell(11).setCellValue((Boolean) member.get("isActive") ? "Active" : "Inactive");
            row.createCell(12).setCellValue((String) member.get("createdAt"));
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            memberSheet.autoSizeColumn(i);
        }
        
        workbook.write(response.getOutputStream());
        workbook.close();
    }
    
    private void exportCSV(HttpServletResponse response, List<Map<String, Object>> members) throws Exception {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=UAI4D_Member_Report.csv");
        
        PrintWriter out = response.getWriter();
        CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.withHeader(
            "ID", "Username", "Full Name", "Email", "Role", "Course", "Year", 
            "University", "AI Level", "Skills", "Bio", "Status", "Joined"
        ));
        
        for (Map<String, Object> member : members) {
            printer.printRecord(
                member.get("userId"),
                member.get("username"),
                member.get("fullName"),
                member.get("email"),
                member.get("role"),
                member.get("course"),
                member.get("yearOfStudy"),
                member.get("university"),
                member.get("aiExperience"),
                member.get("skills"),
                member.get("bio"),
                (Boolean) member.get("isActive") ? "Active" : "Inactive",
                member.get("createdAt")
            );
        }
        
        printer.close();
        out.flush();
    }
}