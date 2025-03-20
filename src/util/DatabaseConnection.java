package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/s17_group8";
    private static final String USER = "root";
    private static final String PASSWORD = "cgkghj"; // Default empty password
    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Try to connect to the database
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                showError("MySQL JDBC Driver not found.\nPlease add the MySQL JDBC driver to your project.");
                e.printStackTrace();
            } catch (SQLException e) {
                String message = "Database connection failed.\n\n";
                if (e.getMessage().contains("Access denied")) {
                    message += "Please check your MySQL username and password.\n";
                    message += "Current settings:\n";
                    message += "Username: " + USER + "\n";
                    message += "Password: " + (PASSWORD.isEmpty() ? "(empty)" : "(set)") + "\n\n";
                    message += "Update these in DatabaseConnection.java";
                } else if (e.getMessage().contains("Unknown database")) {
                    message += "Database 's17_group8' does not exist.\n";
                    message += "Please create the database and import the required schema.";
                } else {
                    message += e.getMessage();
                }
                showError(message);
                e.printStackTrace();
            }
        }
        return connection;
    }

    private static void showError(String message) {
        JOptionPane.showMessageDialog(null, 
            message,
            "Database Connection Error",
            JOptionPane.ERROR_MESSAGE);
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
} 