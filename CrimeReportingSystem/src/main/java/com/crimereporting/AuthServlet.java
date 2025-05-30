package com.crimereporting;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet; // Using Jakarta EE 6.0 annotations
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Servlet for handling user authentication (login and registration).
 * Mapped to /AuthServlet in web.xml.
 */
//@WebServlet("/AuthServlet") // Annotation equivalent to web.xml mapping for AuthServlet
public class AuthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L; // Recommended for Servlets

    /**
     * Handles HTTP POST requests for login and registration.
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

        if ("login".equals(action)) {
            handleLogin(request, response);
        } else if ("register".equals(action)) {
            handleRegister(request, response);
        } else {
            // Invalid action, redirect to login with an error message
            response.sendRedirect("index.html?message=" +
                    java.net.URLEncoder.encode("Invalid action.", "UTF-8") +
                    "&type=error");
        }
    }

    /**
     * Handles HTTP GET requests, primarily for logout.
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

        if ("logout".equals(action)) {
            handleLogout(request, response);
        } else {
            // For any other GET request, redirect to login page
            response.sendRedirect("index.html");
        }
    }

    /**
     * Handles user login.
     *
     * @param request The HttpServletRequest object.
     * @param response The HttpServletResponse object.
     * @throws IOException if an I/O error occurs during redirection.
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password"); // Plain text password for this example

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT id, fullname, username, email FROM users WHERE username = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password); // WARNING: In a real app, hash and compare passwords!
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // User found, login successful
                HttpSession session = request.getSession(); // Get or create a new session
                session.setAttribute("userId", rs.getInt("id"));
                session.setAttribute("username", rs.getString("username"));
                session.setAttribute("fullname", rs.getString("fullname"));

                // Redirect to file complaint page upon successful login
                response.sendRedirect("fileComplaint.html?message=" +
                        java.net.URLEncoder.encode("Login successful! Welcome, " + rs.getString("fullname") + ".", "UTF-8") +
                        "&type=success");
            } else {
                // Invalid credentials
                response.sendRedirect("index.html?message=" +
                        java.net.URLEncoder.encode("Invalid username or password.", "UTF-8") +
                        "&type=error");
            }
        } catch (SQLException e) {
            System.err.println("Database error during login: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("index.html?message=" +
                    java.net.URLEncoder.encode("A database error occurred. Please try again.", "UTF-8") +
                    "&type=error");
        } finally {
            // Ensure database resources are closed
            DBConnection.close(conn, pstmt, rs);
        }
    }

    /**
     * Handles user registration.
     *
     * @param request The HttpServletRequest object.
     * @param response The HttpServletResponse object.
     * @throws IOException if an I/O error occurs during redirection.
     */
    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String fullname = request.getParameter("fullname");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password"); // Plain text password for this example

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rsCheck = null; // Separate ResultSet for the check query

        try {
            conn = DBConnection.getConnection();

            // Check if username or email already exists to prevent duplicates
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            rsCheck = pstmt.executeQuery(); // Execute the check query
            if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                // Username or Email already exists
                response.sendRedirect("register.html?message=" +
                        java.net.URLEncoder.encode("Username or Email already exists. Please choose another.", "UTF-8") +
                        "&type=error");
                return; // Stop further processing
            }
            // Close the PreparedStatement and ResultSet used for the check query before reusing pstmt
            DBConnection.close(null, pstmt, rsCheck);
            pstmt = null; // Set to null to ensure it's re-initialized for the insert query

            // Insert new user into the database
            String insertSql = "INSERT INTO users (fullname, username, email, password) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setString(1, fullname);
            pstmt.setString(2, username);
            pstmt.setString(3, email);
            pstmt.setString(4, password); // WARNING: In a real app, hash passwords!
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Registration successful
                response.sendRedirect("index.html?message=" +
                        java.net.URLEncoder.encode("Registration successful! Please login.", "UTF-8") +
                        "&type=success");
            } else {
                // Registration failed for unknown reason
                response.sendRedirect("register.html?message=" +
                        java.net.URLEncoder.encode("Registration failed. Please try again.", "UTF-8") +
                        "&type=error");
            }
        } catch (SQLException e) {
            System.err.println("Database error during registration: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect("register.html?message=" +
                    java.net.URLEncoder.encode("A database error occurred during registration. Please try again.", "UTF-8") +
                    "&type=error");
        } finally {
            // Ensure database resources are closed
            DBConnection.close(conn, pstmt, rsCheck);
        }
    }

    /**
     * Handles user logout by invalidating the session.
     *
     * @param request The HttpServletRequest object.
     * @param response The HttpServletResponse object.
     * @throws IOException if an I/O error occurs during redirection.
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false); // Get existing session, don't create a new one
        if (session != null) {
            session.invalidate(); // Invalidate the session to log out the user
        }
        response.sendRedirect("index.html?message=" +
                java.net.URLEncoder.encode("You have been logged out successfully.", "UTF-8") +
                "&type=success");
    }
}
