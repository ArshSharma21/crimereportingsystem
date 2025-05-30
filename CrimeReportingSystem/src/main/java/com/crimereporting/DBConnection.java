package com.crimereporting;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnection {

   
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/crimereportingsystem?useSSL=false&serverTimezone=UTC";
    private static final String JDBC_USERNAME = "root"; 
    private static final String JDBC_PASSWORD = "";     

 
    public static Connection getConnection() throws SQLException {
       
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Make sure mysql-connector-java is in your classpath.");
            throw new SQLException("JDBC Driver not found", e);
        }
        
        return DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
    }

 
    public static void close(AutoCloseable conn, AutoCloseable stmt, AutoCloseable rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (Exception e) {
          
            System.err.println("Error closing database resources: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
