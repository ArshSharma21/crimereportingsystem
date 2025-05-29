package com.crimereporting;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class for establishing a database connection.
 * This class provides a static method to get a connection to the MySQL database.
 */
public class DBConnection {

    // Database connection parameters
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/crimereportingsystem?useSSL=false&serverTimezone=UTC";
    private static final String JDBC_USERNAME = "root"; // Default XAMPP MySQL username
    private static final String JDBC_PASSWORD = "";     // Default XAMPP MySQL password (empty)

    /**
     * Establishes and returns a database connection.
     *
     * @return A Connection object to the database.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        // Load the MySQL JDBC driver (optional for newer JDBC versions, but good practice for clarity)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Make sure mysql-connector-java is in your classpath.");
            throw new SQLException("JDBC Driver not found", e);
        }
        // Establish the connection using the defined URL, username, and password
        return DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
    }

    /**
     * Closes database resources (Connection, Statement, ResultSet) safely.
     * This helps prevent resource leaks.
     *
     * @param conn The Connection object to close.
     * @param stmt The PreparedStatement or Statement object to close.
     * @param rs The ResultSet object to close.
     */
    public static void close(AutoCloseable conn, AutoCloseable stmt, AutoCloseable rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (Exception e) {
            // Log the error but don't re-throw, as this is a cleanup method
            System.err.println("Error closing database resources: " + e.getMessage());
            e.printStackTrace();
        }
    }
}