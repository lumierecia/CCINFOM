package dao;

import model.Table;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {
    public int createTable(Table table) throws SQLException {
        String sql = "INSERT INTO Tables (table_number, capacity, status) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, table.getTableNumber());
            pstmt.setInt(2, table.getCapacity());
            pstmt.setString(3, table.getStatus());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating table failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating table failed, no ID obtained.");
                }
            }
        }
    }

    public Table getTableById(int tableId) throws SQLException {
        String sql = "SELECT * FROM Tables WHERE table_id = ? AND is_deleted = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tableId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Table(
                        rs.getInt("table_id"),
                        rs.getInt("table_number"),
                        rs.getInt("capacity"),
                        rs.getString("status"),
                        rs.getBoolean("is_deleted")
                    );
                }
                return null;
            }
        }
    }

    public List<Table> getAllTables() throws SQLException {
        List<Table> tables = new ArrayList<>();
        String sql = "SELECT * FROM Tables WHERE is_deleted = FALSE ORDER BY table_number";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                tables.add(new Table(
                    rs.getInt("table_id"),
                    rs.getInt("table_number"),
                    rs.getInt("capacity"),
                    rs.getString("status"),
                    rs.getBoolean("is_deleted")
                ));
            }
        }
        return tables;
    }

    public List<Table> getAvailableTables() throws SQLException {
        List<Table> tables = new ArrayList<>();
        String sql = "SELECT * FROM Tables WHERE status = 'Available' AND is_deleted = FALSE ORDER BY table_number";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                tables.add(new Table(
                    rs.getInt("table_id"),
                    rs.getInt("table_number"),
                    rs.getInt("capacity"),
                    rs.getString("status"),
                    rs.getBoolean("is_deleted")
                ));
            }
        }
        return tables;
    }

    public boolean updateTable(Table table) throws SQLException {
        String sql = "UPDATE Tables SET table_number = ?, capacity = ?, status = ? WHERE table_id = ? AND is_deleted = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, table.getTableNumber());
            pstmt.setInt(2, table.getCapacity());
            pstmt.setString(3, table.getStatus());
            pstmt.setInt(4, table.getTableId());
            
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateTableStatus(int tableId, String status) throws SQLException {
        String sql = "UPDATE Tables SET status = ? WHERE table_id = ? AND is_deleted = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, tableId);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteTable(int tableId) throws SQLException {
        String sql = "UPDATE Tables SET is_deleted = TRUE WHERE table_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tableId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean isTableAvailable(int tableId) throws SQLException {
        String sql = "SELECT status FROM Tables WHERE table_id = ? AND is_deleted = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tableId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return "Available".equals(rs.getString("status"));
                }
                return false;
            }
        }
    }
} 