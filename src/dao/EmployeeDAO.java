package dao;

import model.Employee;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public Employee getEmployeeById(int employeeId) {
        String query = """
            SELECT e.*, r.role_name, ts.shift_type, ts.time_start, ts.time_end 
            FROM Employees e 
            LEFT JOIN Roles r ON e.role_id = r.role_id 
            LEFT JOIN TimeShifts ts ON e.time_shiftid = ts.time_shiftid 
            WHERE e.employee_id = ?
        """;
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String query = """
            SELECT e.*, r.role_name, ts.shift_type, ts.time_start, ts.time_end 
            FROM Employees e 
            LEFT JOIN Roles r ON e.role_id = r.role_id 
            LEFT JOIN TimeShifts ts ON e.time_shiftid = ts.time_shiftid 
            ORDER BY e.last_name, e.first_name
        """;
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
        String query = "DELETE FROM Employees WHERE employee_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, employeeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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
} 