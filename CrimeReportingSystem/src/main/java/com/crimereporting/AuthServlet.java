package com.crimereporting;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class AuthServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("login".equals(action)) {
            handleLogin(request, response);
        } else if ("register".equals(action)) {
            handleRegister(request, response);
        } else {

            response.sendRedirect("index.html?message=" +
                    java.net.URLEncoder.encode("Invalid action.", "UTF-8") +
                    "&type=error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("logout".equals(action)) {
            handleLogout(request, response);
        } else {
            response.sendRedirect("index.html");
        }
    }
    
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password"); 

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT id, fullname, username, email FROM users WHERE username = ? AND password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password); 
            rs = pstmt.executeQuery();

            if (rs.next()) {
                HttpSession session = request.getSession();
                session.setAttribute("userId", rs.getInt("id"));
                session.setAttribute("username", rs.getString("username"));
                session.setAttribute("fullname", rs.getString("fullname"));
                response.sendRedirect("fileComplaint.html?message=" +
                        java.net.URLEncoder.encode("Login successful! Welcome, " + rs.getString("fullname") + ".", "UTF-8") +
                        "&type=success");
            } else {
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
            DBConnection.close(conn, pstmt, rs);
        }
    }
    
    private void handleRegister(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String fullname = request.getParameter("fullname");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password"); 

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rsCheck = null; 

        try {
            conn = DBConnection.getConnection();
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            rsCheck = pstmt.executeQuery();
            if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                response.sendRedirect("register.html?message=" +
                        java.net.URLEncoder.encode("Username or Email already exists. Please choose another.", "UTF-8") +
                        "&type=error");
                return; 
            }
            DBConnection.close(null, pstmt, rsCheck);
            pstmt = null; 

            String insertSql = "INSERT INTO users (fullname, username, email, password) VALUES (?, ?, ?, ?)";
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setString(1, fullname);
            pstmt.setString(2, username);
            pstmt.setString(3, email);
            pstmt.setString(4, password); 
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                response.sendRedirect("index.html?message=" +
                        java.net.URLEncoder.encode("Registration successful! Please login.", "UTF-8") +
                        "&type=success");
            } else {
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
            DBConnection.close(conn, pstmt, rsCheck);
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect("index.html?message=" +
                java.net.URLEncoder.encode("You have been logged out successfully.", "UTF-8") +
                "&type=success");
    }
}
