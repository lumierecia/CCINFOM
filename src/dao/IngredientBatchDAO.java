package dao;

import model.IngredientBatch;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class IngredientBatchDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public List<IngredientBatch> getBatchesByIngredient(int ingredientId) {
        List<IngredientBatch> batches = new ArrayList<>();
        String query = """
            SELECT b.*, s.name as supplier_name 
            FROM IngredientBatches b
            JOIN Suppliers s ON b.supplier_id = s.supplier_id
            WHERE b.ingredient_id = ? AND b.status != 'Depleted'
            ORDER BY b.expiry_date ASC
        """;

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    batches.add(createBatchFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch ingredient batches: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return batches;
    }

    public boolean addBatch(IngredientBatch batch) {
        String query = """
            INSERT INTO IngredientBatches 
            (ingredient_id, supplier_id, quantity, expiry_date, purchase_price, remaining_quantity)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, batch.getIngredientId());
            stmt.setInt(2, batch.getSupplierId());
            stmt.setDouble(3, batch.getQuantity());
            if (batch.getExpiryDate() != null) {
                stmt.setDate(4, new java.sql.Date(batch.getExpiryDate().getTime()));
            } else {
                stmt.setNull(4, Types.DATE);
            }
            stmt.setDouble(5, batch.getPurchasePrice());
            stmt.setDouble(6, batch.getQuantity()); // Initial remaining quantity equals total quantity

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        batch.setBatchId(rs.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to add ingredient batch: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean updateBatchQuantity(int batchId, double quantityChange) {
        String query = """
            UPDATE IngredientBatches 
            SET remaining_quantity = remaining_quantity + ?,
                status = CASE 
                    WHEN remaining_quantity + ? <= 0 THEN 'Depleted'
                    WHEN remaining_quantity + ? < quantity * 0.2 THEN 'Low'
                    ELSE status 
                END
            WHERE batch_id = ?
        """;

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setDouble(1, quantityChange);
            stmt.setDouble(2, quantityChange);
            stmt.setDouble(3, quantityChange);
            stmt.setInt(4, batchId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to update batch quantity: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public List<IngredientBatch> getExpiringBatches(int daysThreshold) {
        List<IngredientBatch> batches = new ArrayList<>();
        String query = """
            SELECT b.*, s.name as supplier_name, i.name as ingredient_name
            FROM IngredientBatches b
            JOIN Suppliers s ON b.supplier_id = s.supplier_id
            JOIN Ingredients i ON b.ingredient_id = i.ingredient_id
            WHERE b.expiry_date <= DATE_ADD(CURRENT_DATE, INTERVAL ? DAY)
            AND b.status != 'Depleted' AND b.status != 'Expired'
            ORDER BY b.expiry_date ASC
        """;

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, daysThreshold);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    batches.add(createBatchFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch expiring batches: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return batches;
    }

    private IngredientBatch createBatchFromResultSet(ResultSet rs) throws SQLException {
        IngredientBatch batch = new IngredientBatch();
        batch.setBatchId(rs.getInt("batch_id"));
        batch.setIngredientId(rs.getInt("ingredient_id"));
        batch.setSupplierId(rs.getInt("supplier_id"));
        batch.setQuantity(rs.getDouble("quantity"));
        batch.setExpiryDate(rs.getDate("expiry_date"));
        batch.setPurchasePrice(rs.getDouble("purchase_price"));
        batch.setRemainingQuantity(rs.getDouble("remaining_quantity"));
        batch.setStatus(rs.getString("status"));
        if (rs.getString("supplier_name") != null) {
            batch.setSupplierName(rs.getString("supplier_name"));
        }
        if (rs.getString("ingredient_name") != null) {
            batch.setIngredientName(rs.getString("ingredient_name"));
        }
        return batch;
    }
} 