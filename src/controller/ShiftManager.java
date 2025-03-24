package controller;

import model.Employee;
import model.EmployeeShift;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShiftManager {
    private final RestaurantController controller;

    public ShiftManager(RestaurantController controller) {
        this.controller = controller;
    }

    public boolean assignShift(int employeeId, int timeShiftId, Date shiftDate) throws SQLException {
        String query = """
            INSERT INTO EmployeeShifts (employee_id, time_shiftid, shift_date, status)
            VALUES (?, ?, ?, 'Scheduled')
        """;

        try (Connection conn = controller.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, employeeId);
            stmt.setInt(2, timeShiftId);
            stmt.setDate(3, shiftDate);
            
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateShiftStatus(int employeeId, int timeShiftId, Date shiftDate, String status) throws SQLException {
        String query = """
            UPDATE EmployeeShifts
            SET status = ?
            WHERE employee_id = ? AND time_shiftid = ? AND shift_date = ?
        """;

        try (Connection conn = controller.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, employeeId);
            stmt.setInt(3, timeShiftId);
            stmt.setDate(4, shiftDate);
            
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteShift(int employeeId, int timeShiftId, Date shiftDate) throws SQLException {
        String query = """
            DELETE FROM EmployeeShifts
            WHERE employee_id = ? AND time_shiftid = ? AND shift_date = ?
        """;

        try (Connection conn = controller.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, employeeId);
            stmt.setInt(2, timeShiftId);
            stmt.setDate(3, shiftDate);
            
            return stmt.executeUpdate() > 0;
        }
    }

    public List<EmployeeShift> getEmployeeShifts(int employeeId, Date startDate, Date endDate) throws SQLException {
        List<EmployeeShift> shifts = new ArrayList<>();
        String query = """
            SELECT es.*, ts.shift_type, ts.time_start, ts.time_end
            FROM EmployeeShifts es
            JOIN TimeShifts ts ON es.time_shiftid = ts.time_shiftid
            WHERE es.employee_id = ?
            AND es.shift_date BETWEEN ? AND ?
            ORDER BY es.shift_date, ts.time_start
        """;

        try (Connection conn = controller.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, employeeId);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EmployeeShift shift = new EmployeeShift();
                    shift.setShiftId(rs.getInt("shift_id"));
                    shift.setEmployeeId(rs.getInt("employee_id"));
                    shift.setTimeShiftId(rs.getInt("time_shiftid"));
                    shift.setShiftDate(rs.getDate("shift_date"));
                    shift.setStatus(rs.getString("status"));
                    shift.setCheckIn(rs.getTimestamp("check_in"));
                    shift.setCheckOut(rs.getTimestamp("check_out"));
                    shift.setShiftType(rs.getString("shift_type"));
                    shift.setTimeStart(rs.getTime("time_start"));
                    shift.setTimeEnd(rs.getTime("time_end"));
                    shifts.add(shift);
                }
            }
        }
        return shifts;
    }

    public boolean checkIn(int employeeId, int timeShiftId, Date shiftDate) throws SQLException {
        String query = """
            UPDATE EmployeeShifts
            SET status = 'Present',
                check_in = CURRENT_TIMESTAMP
            WHERE employee_id = ? AND time_shiftid = ? AND shift_date = ?
        """;

        try (Connection conn = controller.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, employeeId);
            stmt.setInt(2, timeShiftId);
            stmt.setDate(3, shiftDate);
            
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean checkOut(int employeeId, int timeShiftId, Date shiftDate) throws SQLException {
        String query = """
            UPDATE EmployeeShifts
            SET check_out = CURRENT_TIMESTAMP
            WHERE employee_id = ? AND time_shiftid = ? AND shift_date = ?
        """;

        try (Connection conn = controller.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, employeeId);
            stmt.setInt(2, timeShiftId);
            stmt.setDate(3, shiftDate);
            
            return stmt.executeUpdate() > 0;
        }
    }

    public List<EmployeeShift> getCurrentShifts() throws SQLException {
        List<EmployeeShift> shifts = new ArrayList<>();
        String query = """
            SELECT es.*, ts.shift_type, ts.time_start, ts.time_end,
                   e.first_name, e.last_name
            FROM EmployeeShifts es
            JOIN TimeShifts ts ON es.time_shiftid = ts.time_shiftid
            JOIN Employees e ON es.employee_id = e.employee_id
            WHERE es.shift_date = CURDATE()
            AND es.status IN ('Scheduled', 'Present')
            ORDER BY ts.time_start
        """;

        try (Connection conn = controller.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                EmployeeShift shift = new EmployeeShift();
                shift.setShiftId(rs.getInt("shift_id"));
                shift.setEmployeeId(rs.getInt("employee_id"));
                shift.setTimeShiftId(rs.getInt("time_shiftid"));
                shift.setShiftDate(rs.getDate("shift_date"));
                shift.setStatus(rs.getString("status"));
                shift.setCheckIn(rs.getTimestamp("check_in"));
                shift.setCheckOut(rs.getTimestamp("check_out"));
                shift.setShiftType(rs.getString("shift_type"));
                shift.setTimeStart(rs.getTime("time_start"));
                shift.setTimeEnd(rs.getTime("time_end"));
                shift.setEmployeeName(rs.getString("first_name") + " " + rs.getString("last_name"));
                shifts.add(shift);
            }
        }
        return shifts;
    }
} 