package dao;

import model.IngredientBatch;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class IngredientBatchDAO {
    private final Connection connection;

    public IngredientBatchDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    public List<IngredientBatch> getBatchesByIngredient(int ingredientId) throws SQLException {
        List<IngredientBatch> batches = new ArrayList<>();
        String query = """
            SELECT b.*, i.ingredient_name, u.unit_name
            FROM IngredientBatches b
            JOIN Ingredients i ON b.ingredient_id = i.ingredient_id
            JOIN Units u ON b.unit_id = u.unit_id
            WHERE b.is_deleted = FALSE
            AND (? = -1 OR b.ingredient_id = ?)
            ORDER BY b.expiry_date ASC
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            stmt.setInt(2, ingredientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    batches.add(createBatchFromResultSet(rs));
                }
            }
        }
        return batches;
    }

    public List<IngredientBatch> getExpiringBatches(int days) throws SQLException {
        List<IngredientBatch> batches = new ArrayList<>();
        String query = """
            SELECT b.*, i.ingredient_name, u.unit_name
            FROM IngredientBatches b
            JOIN Ingredients i ON b.ingredient_id = i.ingredient_id
            JOIN Units u ON b.unit_id = u.unit_id
            WHERE b.is_deleted = FALSE
            AND b.expiry_date <= DATE_ADD(CURRENT_DATE, INTERVAL ? DAY)
            AND b.remaining_quantity > 0
            ORDER BY b.expiry_date ASC
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, days);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    batches.add(createBatchFromResultSet(rs));
                }
            }
        }
        return batches;
    }

    public IngredientBatch getBatchById(int batchId) throws SQLException {
        String query = """
            SELECT b.*, i.ingredient_name, u.unit_name
            FROM IngredientBatches b
            JOIN Ingredients i ON b.ingredient_id = i.ingredient_id
            JOIN Units u ON b.unit_id = u.unit_id
            WHERE b.batch_id = ? AND b.is_deleted = FALSE
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, batchId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createBatchFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public boolean addBatch(IngredientBatch batch) throws SQLException {
        String query = """
            INSERT INTO IngredientBatches (
                ingredient_id, unit_id, quantity, remaining_quantity,
                purchase_date, expiry_date, purchase_price, supplier_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, batch.getIngredientId());
            stmt.setInt(2, batch.getUnitId());
            stmt.setDouble(3, batch.getQuantity());
            stmt.setDouble(4, batch.getRemainingQuantity());
            stmt.setDate(5, new java.sql.Date(batch.getPurchaseDate().getTime()));
            stmt.setDate(6, new java.sql.Date(batch.getExpiryDate().getTime()));
            stmt.setDouble(7, batch.getPurchasePrice());
            stmt.setInt(8, batch.getSupplierId());
            
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateBatchQuantity(int batchId, double newQuantity) throws SQLException {
        String query = """
            UPDATE IngredientBatches 
            SET remaining_quantity = ?
            WHERE batch_id = ? AND is_deleted = FALSE
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, newQuantity);
            stmt.setInt(2, batchId);
            
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteBatch(int batchId) throws SQLException {
        String query = "UPDATE IngredientBatches SET is_deleted = TRUE WHERE batch_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, batchId);
            return stmt.executeUpdate() > 0;
        }
    }

    private IngredientBatch createBatchFromResultSet(ResultSet rs) throws SQLException {
        IngredientBatch batch = new IngredientBatch(
            rs.getInt("batch_id"),
            rs.getInt("ingredient_id"),
            rs.getInt("unit_id"),
            rs.getDouble("quantity"),
            rs.getDouble("remaining_quantity"),
            rs.getDate("purchase_date"),
            rs.getDate("expiry_date"),
            rs.getDouble("purchase_price"),
            rs.getInt("supplier_id")
        );
        batch.setIngredientName(rs.getString("ingredient_name"));
        batch.setUnitName(rs.getString("unit_name"));
        return batch;
    }
} 