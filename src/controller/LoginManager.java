package controller;

import model.Employee;
import util.DatabaseConnection;
import java.sql.*;

public class LoginManager {
    private final RestaurantController controller;
    private Employee currentUser;
    private String currentRole;

    public LoginManager(RestaurantController controller) {
        this.controller = controller;
        this.currentUser = null;
        this.currentRole = null;
    }

    public boolean login(String username, String password) throws SQLException {
        String query = """
            SELECT uc.user_id, uc.employee_id, uc.username, e.role_id, r.role_name, e.time_shiftid
            FROM UserCredentials uc
            JOIN Employees e ON uc.employee_id = e.employee_id
            JOIN Roles r ON e.role_id = r.role_id
            WHERE uc.username = ? AND uc.password_hash = ? AND uc.is_active = TRUE
            AND e.is_deleted = FALSE
        """;

        try (Connection conn = controller.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password); // In production, use proper password hashing

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    currentUser = new Employee();
                    currentUser.setEmployeeId(rs.getInt("employee_id"));
                    currentUser.setRoleId(rs.getInt("role_id"));
                    currentUser.setTimeShiftId(rs.getInt("time_shiftid"));
                    currentRole = rs.getString("role_name");

                    // Update last login
                    updateLastLogin(rs.getInt("user_id"));
                    return true;
                }
            }
        }
        return false;
    }

    private void updateLastLogin(int userId) throws SQLException {
        String query = "UPDATE UserCredentials SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        try (Connection conn = controller.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    public void logout() {
        currentUser = null;
        currentRole = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public Employee getCurrentUser() {
        return currentUser;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public boolean isManager() {
        return "Manager".equals(currentRole);
    }

    public boolean isOnShift() throws SQLException {
        if (currentUser == null || currentUser.getTimeShiftId() == 0) {
            return false;
        }

        String query = """
            SELECT COUNT(*) as count
            FROM EmployeeShifts
            WHERE employee_id = ? 
            AND time_shiftid = ?
            AND shift_date = CURDATE()
            AND status IN ('Scheduled', 'Present')
        """;

        try (Connection conn = controller.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, currentUser.getEmployeeId());
            stmt.setInt(2, currentUser.getTimeShiftId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }

    public boolean checkShiftAccess() throws SQLException {
        if (!isLoggedIn()) {
            return false;
        }

        if (isManager()) {
            return true; // Managers have access regardless of shift
        }

        return isOnShift();
    }
} 