package dao;

import model.Employee;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class EmployeeDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public Employee getEmployeeById(int employeeId) {
        String query = "SELECT e.*, r.role_name, ts.shift_type, ts.time_start, ts.time_end " +
                      "FROM Employees e " +
                      "LEFT JOIN Roles r ON e.role_id = r.role_id " +
                      "LEFT JOIN TimeShifts ts ON e.time_shiftid = ts.time_shiftid " +
                      "WHERE e.employee_id = ? AND e.is_deleted = FALSE";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEmployee(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Employee getEmployeeByName(String firstName, String lastName) {
        String query = """
            SELECT e.*, r.role_name, ts.shift_type, ts.time_start, ts.time_end
            FROM Employees e
            LEFT JOIN Roles r ON e.role_id = r.role_id
            LEFT JOIN TimeShifts ts ON e.time_shiftid = ts.time_shiftid
            WHERE e.first_name = ? AND e.last_name = ?
            """;
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEmployee(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT e.*, r.role_name, ts.shift_type, ts.time_start, ts.time_end " +
                      "FROM Employees e " +
                      "LEFT JOIN Roles r ON e.role_id = r.role_id " +
                      "LEFT JOIN TimeShifts ts ON e.time_shiftid = ts.time_shiftid " +
                      "WHERE e.is_deleted = FALSE";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Employee emp = new Employee(
                    rs.getInt("employee_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("role_name")
                );
                emp.setShiftType(rs.getString("shift_type"));
                emp.setShiftStart(rs.getTime("time_start"));
                emp.setShiftEnd(rs.getTime("time_end"));
                employees.add(emp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public boolean addEmployee(Employee employee) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // First get the role_id for the employee's role
            int roleId;
            String roleQuery = "SELECT role_id FROM Roles WHERE role_name = ?";
            try (PreparedStatement roleStmt = conn.prepareStatement(roleQuery)) {
                roleStmt.setString(1, employee.getRole());
                try (ResultSet rs = roleStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    roleId = rs.getInt("role_id");
                }
            }

            // Then insert the employee
            String query = "INSERT INTO Employees (first_name, last_name, role_id) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, employee.getFirstName());
                pstmt.setString(2, employee.getLastName());
                pstmt.setInt(3, roleId);
                
                if (pstmt.executeUpdate() > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            employee.setEmployeeId(generatedKeys.getInt(1));
                            conn.commit();
                            return true;
                        }
                    }
                }
            }
            conn.rollback();
            return false;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean updateEmployee(Employee employee) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Get the role_id for the employee's role
            int roleId;
            String roleQuery = "SELECT role_id FROM Roles WHERE role_name = ?";
            try (PreparedStatement roleStmt = conn.prepareStatement(roleQuery)) {
                roleStmt.setString(1, employee.getRole());
                try (ResultSet rs = roleStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    roleId = rs.getInt("role_id");
                }
            }

            String query = "UPDATE Employees SET first_name = ?, last_name = ?, role_id = ? WHERE employee_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, employee.getFirstName());
                pstmt.setString(2, employee.getLastName());
                pstmt.setInt(3, roleId);
                pstmt.setInt(4, employee.getEmployeeId());
                
                boolean success = pstmt.executeUpdate() > 0;
                if (success) {
                    conn.commit();
                    return true;
                }
            }
            conn.rollback();
            return false;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean deleteEmployee(int employeeId) {
        // First check if employee is assigned to any active orders
        String checkQuery = "SELECT COUNT(*) FROM AssignedEmployeesToOrders aeo " +
                          "JOIN Orders o ON aeo.order_id = o.order_id " +
                          "WHERE aeo.employee_id = ? AND o.is_deleted = FALSE";
        
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            
            checkStmt.setInt(1, employeeId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(null,
                        "Cannot delete employee because they are assigned to existing orders.",
                        "Delete Failed",
                        JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }
            
            // If no active orders, proceed with soft delete
            String updateQuery = "UPDATE Employees SET is_deleted = TRUE WHERE employee_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, employeeId);
                return updateStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to delete employee: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean assignShift(int employeeId, int timeShiftId) {
        String query = "UPDATE Employees SET time_shiftid = ? WHERE employee_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, timeShiftId);
            pstmt.setInt(2, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeShift(int employeeId) {
        String query = "UPDATE Employees SET time_shiftid = NULL WHERE employee_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getCurrentShift(int employeeId) {
        String query = """
            SELECT ts.shift_type 
            FROM Employees e 
            JOIN TimeShifts ts ON e.time_shiftid = ts.time_shiftid 
            WHERE e.employee_id = ?
        """;
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("shift_type");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Employee> searchEmployees(String searchTerm) {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT e.*, r.role_name, ts.shift_type, ts.time_start, ts.time_end " +
                      "FROM Employees e " +
                      "LEFT JOIN Roles r ON e.role_id = r.role_id " +
                      "LEFT JOIN TimeShifts ts ON e.time_shiftid = ts.time_shiftid " +
                      "WHERE e.is_deleted = FALSE AND " +
                      "(e.first_name LIKE ? OR e.last_name LIKE ?) " +
                      "ORDER BY e.last_name, e.first_name";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            pstmt.setString(2, "%" + searchTerm + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Employee emp = mapResultSetToEmployee(rs);
                    employees.add(emp);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public List<Employee> getDeletedEmployees() {
        List<Employee> employees = new ArrayList<>();
        String query = """
            SELECT e.*, r.role_name, ts.shift_type 
            FROM Employees e 
            JOIN Roles r ON e.role_id = r.role_id 
            LEFT JOIN TimeShifts ts ON e.time_shiftid = ts.time_shiftid 
            WHERE e.is_deleted = TRUE
            ORDER BY e.last_name, e.first_name
        """;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Employee employee = new Employee(
                    rs.getInt("employee_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getInt("role_id")
                );
                employee.setTimeShiftId(rs.getInt("time_shiftid"));
                employee.setRoleName(rs.getString("role_name"));
                employee.setShiftType(rs.getString("shift_type"));
                employees.add(employee);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch deleted employees: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return employees;
    }

    public boolean restoreEmployee(int employeeId) {
        String query = "UPDATE Employees SET is_deleted = FALSE WHERE employee_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, employeeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to restore employee: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public Employee getEmployeeByUserId(int userId) throws SQLException {
        String query = "SELECT e.* FROM Employees e " +
                      "JOIN UserCredentials uc ON e.employee_id = uc.employee_id " +
                      "WHERE uc.user_id = ? AND e.is_deleted = FALSE";

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeId(rs.getInt("employee_id"));
                employee.setFirstName(rs.getString("first_name"));
                employee.setLastName(rs.getString("last_name"));
                employee.setRoleId(rs.getInt("role_id"));
                employee.setTimeShiftId(rs.getInt("time_shiftid"));
                return employee;
            }
        }
        return null;
    }

    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        Employee emp = new Employee(
            rs.getInt("employee_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("role_name")
        );
        emp.setShiftType(rs.getString("shift_type"));
        emp.setShiftStart(rs.getTime("time_start"));
        emp.setShiftEnd(rs.getTime("time_end"));
        return emp;
    }
} 