package dao;

import model.UserCredentials;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class LoginDAO {
    private Connection connection;

    public LoginDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    public UserCredentials authenticateUser(String username, String password) {
        String query = "SELECT uc.*, e.role_id FROM UserCredentials uc " +
                      "JOIN Employees e ON uc.employee_id = e.employee_id " +
                      "WHERE uc.username = ? AND uc.password_hash = ? AND uc.is_active = TRUE";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password); // In a real app, this should be hashed

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UserCredentials user = new UserCredentials(
                    rs.getInt("user_id"),
                    rs.getInt("employee_id"),
                    rs.getString("username"),
                    rs.getString("password_hash"),
                    rs.getTimestamp("last_login"),
                    rs.getBoolean("is_active"),
                    rs.getInt("role_id")
                );
                
                // Update last login
                updateLastLogin(user.getUserId());
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateLastLogin(int userId) {
        String query = "UPDATE UserCredentials SET last_login = ? WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 