package com.crimereporting;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.Gson; // Import Gson library

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet for handling crime complaint operations (filing and viewing).
 * Mapped to /ComplaintServlet in web.xml.
 */
@WebServlet("/ComplaintServlet") // Annotation equivalent to web.xml mapping for ComplaintServlet
public class ComplaintServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson(); // Initialize Gson for JSON serialization

    /**
     * Handles HTTP POST requests for filing a complaint.
     *
     * @param request The HttpServletRequest object.
     * @param response The HttpServletResponse object.
     * @throws ServletException if a servlet-specific error occurs.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("file".equals(action)) {
            handleFileComplaint(request, response);
        } else {
            response.sendRedirect("fileComplaint.html?message=" +
                    java.net.URLEncoder.encode("Invalid action.", "UTF-8") +
                    "&type=error");
        }
    }

    /**
     * Handles HTTP GET requests for viewing complaints.
     *
     * @param request The HttpServletRequest object.
     * @param response The HttpServletResponse object.
     * @throws ServletException if a servlet-specific error occurs.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("view".equals(action)) {
            handleViewComplaints(request, response);
        } else {
            response.sendRedirect("viewComplaints.html?message=" +
                    java.net.URLEncoder.encode("Invalid action.", "UTF-8") +
                    "&type=error");
        }
    }

    /**
     * Handles filing a new crime complaint.
     * Ensures the user is logged in before allowing complaint submission.
     *
     * @param request The HttpServletRequest object.
     * @param response The HttpServletResponse object.
     * @throws IOException if an I/O error occurs during redirection.
     */
    private void handleFileComplaint(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false); // Get existing session, don't create new
        if (session == null || session.getAttribute("userId") == null) {
            // User not logged in, redirect to login page
            response.sendRedirect("index.html?message=" +
                    java.net.URLEncoder.encode("Please login to file a complaint.", "UTF-8") +
                    "&type=error");
            return;
        }

        int userId = (int) session.getAttribute("userId"); // Get userId from session
        String crimeType = request.getParameter("crimeType");
        String location = request.getParameter("location");
        String dateStr = request.getParameter("date");
        String description = request.getParameter("description");

        // Basic input validation
        if (crimeType == null || crimeType.trim().isEmpty() ||
                location == null || location.trim().isEmpty() ||
                dateStr == null || dateStr.trim().isEmpty() ||
                description == null || description.trim().isEmpty()) {
            response.sendRedirect("fileComplaint.html?message=" +
                    java.net.URLEncoder.encode("All fields are required.", "UTF-8") +
                    "&type=error");
            return;
        }

        LocalDate incidentDate = null;
        try {
            incidentDate = LocalDate.parse(dateStr); // Parse date string to LocalDate
        } catch (DateTimeParseException e) {
            response.sendRedirect("fileComplaint.html?message=" +
                    java.net.URLEncoder.encode("Invalid date format. Please use YYYY-MM-DD.", "UTF-8") +
                    "&type=error");
            return;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO complaints (user_id, crime_type, location, incident_date, description, status) VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, crimeType);
            pstmt.setString(3, location);
            pstmt.setDate(4, java.sql.Date.valueOf(incidentDate)); // Convert LocalDate to java.sql.Date
            pstmt.setString(5, description);
            pstmt.setString(6, "Pending"); // Default status for new complaints
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                response.sendRedirect("fileComplaint.html?message=" +
                        java.net.URLEncoder.encode("Complaint filed successfully!", "UTF-8") +
                        "&type=success");
            } else {
                response.sendRedirect("fileComplaint.html?message=" +
                        java.net.URLEncoder.encode("Failed to file complaint. Please try again.", "UTF-8") +
                        "&type=error");
            }
        } catch (SQLException e) {
            System.err.println("Database error during complaint filing: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("fileComplaint.html?message=" +
                    java.net.URLEncoder.encode("A database error occurred. Please try again.", "UTF-8") +
                    "&type=error");
        } finally {
            DBConnection.close(conn, pstmt, null); // Close resources
        }
    }

    /**
     * Handles viewing all crime complaints.
     * Fetches complaints from the database and returns them as JSON.
     *
     * @param request The HttpServletRequest object.
     * @param response The HttpServletResponse object.
     * @throws IOException if an I/O error occurs during JSON writing.
     */
    private void handleViewComplaints(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            // User not logged in, send an empty JSON array as they don't have access
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(new ArrayList<>())); // Return empty array
            return;
        }

        List<Complaint> complaints = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            // SQL query to retrieve all complaints and the username of the user who filed them
            String sql = "SELECT c.id, c.user_id, c.crime_type, c.location, c.incident_date, c.description, c.status, c.filed_on, u.username " +
                    "FROM complaints c JOIN users u ON c.user_id = u.id ORDER BY c.filed_on DESC";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Complaint complaint = new Complaint();
                complaint.setId(rs.getInt("id"));
                complaint.setUserId(rs.getInt("user_id"));
                complaint.setCrimeType(rs.getString("crime_type"));
                complaint.setLocation(rs.getString("location"));
                complaint.setIncidentDate(rs.getDate("incident_date").toLocalDate()); // Convert java.sql.Date to LocalDate
                complaint.setDescription(rs.getString("description"));
                complaint.setStatus(rs.getString("status"));
                complaint.setFiledOn(rs.getTimestamp("filed_on").toLocalDateTime()); // Convert Timestamp to LocalDateTime
                complaint.setFiledByUsername(rs.getString("username")); // Set the username of the filer
                complaints.add(complaint);
            }

            // Convert the list of complaints to JSON and send it as response
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(complaints));

        } catch (SQLException e) {
            System.err.println("Database error during complaint viewing: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Set HTTP status code for error
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(gson.toJson(new ErrorResponse("Failed to retrieve complaints due to a database error.")));
        } finally {
            DBConnection.close(conn, pstmt, rs); // Close resources
        }
    }

    // Helper class for structured error responses (sent as JSON)
    private static class ErrorResponse {
        String message;
        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
