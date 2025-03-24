package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/restaurantdb";
    private static final String USER = "root";
    private static final String PASSWORD = "hoshionna"; // Update this with your actual MySQL root password
    private static Connection connection = null;

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Set look and feel for better-looking dialogs
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Try to connect to the database
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                connection.setAutoCommit(true);
            } catch (ClassNotFoundException e) {
                showError(
                    "MySQL Driver Not Found",
                    "The MySQL JDBC driver is missing from the project.\n" +
                    "Please make sure you have added mysql-connector-j to your project libraries."
                );
                e.printStackTrace();
                throw new SQLException("MySQL JDBC Driver not found", e);
            } catch (SQLException e) {
                if (e.getMessage().contains("Access denied")) {
                    Object[] options = {"Open MySQL Instructions", "OK"};
                    int choice = JOptionPane.showOptionDialog(null,
                        "⚠️ Database Connection Failed\n\n" +
                        "It seems your MySQL password is not set correctly.\n\n" +
                        "Current Settings:\n" +
                        "• Username: " + USER + "\n" +
                        "• Password: " + (PASSWORD.isEmpty() ? "(empty)" : "(set)") + "\n\n" +
                        "To fix this:\n" +
                        "1. Open src/util/DatabaseConnection.java\n" +
                        "2. Update the PASSWORD field with your MySQL root password\n" +
                        "3. Restart the application\n\n" +
                        "Need help? Click 'Open MySQL Instructions' for a guide.",
                        "Database Password Error",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        options,
                        options[1]
                    );
                    
                    if (choice == 0) {
                        showInstructions();
                    }
                } else if (e.getMessage().contains("Unknown database")) {
                    showError(
                        "Database Not Found",
                        "The database 'restaurantdb' does not exist.\n\n" +
                        "To fix this:\n" +
                        "1. Open MySQL Workbench or your MySQL terminal\n" +
                        "2. Run: CREATE DATABASE restaurantdb;\n" +
                        "3. Import the database schema from sql/Group8_DB.sql"
                    );
                } else {
                    showError(
                        "Database Error",
                        "An unexpected database error occurred:\n" + e.getMessage()
                    );
                }
                e.printStackTrace();
                throw e;
            } catch (Exception e) {
                showError(
                    "System Error",
                    "An unexpected error occurred:\n" + e.getMessage()
                );
                e.printStackTrace();
                throw new SQLException("Failed to initialize database connection", e);
            }
        }
        return connection;
    }

    private static void showError(String title, String message) {
        JOptionPane.showMessageDialog(null, 
            message,
            title,
            JOptionPane.ERROR_MESSAGE);
    }

    private static void showInstructions() {
        JOptionPane.showMessageDialog(null,
            "MySQL Setup Instructions:\n\n" +
            "1. Open MySQL Workbench\n" +
            "2. Click on your local MySQL connection\n" +
            "3. If you don't remember your root password:\n" +
            "   a. Open Windows Services\n" +
            "   b. Stop MySQL service\n" +
            "   c. Open Command Prompt as Administrator\n" +
            "   d. Run: mysqld --init-file=C:\\mysql-init.txt\n" +
            "   e. Create a file mysql-init.txt with:\n" +
            "      ALTER USER 'root'@'localhost' IDENTIFIED BY 'your_new_password';\n\n" +
            "4. Once you have your password:\n" +
            "   a. Open src/util/DatabaseConnection.java\n" +
            "   b. Update the PASSWORD field\n" +
            "   c. Save and restart the application",
            "MySQL Setup Guide",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                connection = null;
            }
        }
    }
} 