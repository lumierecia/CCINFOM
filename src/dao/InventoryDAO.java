package dao;

import model.Inventory;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class InventoryDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public List<Inventory> getAllInventoryItems() {
        List<Inventory> items = new ArrayList<>();
        String query = "SELECT i.*, c.category_name FROM InventoryItems i " +
                      "JOIN Categories c ON i.category_id = c.category_id " +
                      "WHERE i.is_deleted = FALSE";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                items.add(mapResultSetToInventory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch inventory items: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return items;
    }

    public Inventory getInventoryItemById(int productId) {
        String query = """
            SELECT i.*, c.category_name 
            FROM InventoryItems i 
            JOIN Categories c ON i.category_id = c.category_id 
            WHERE i.product_id = ?
            """;
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Inventory(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("category_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("make_price"),
                        rs.getDouble("sell_price")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateInventoryItem(Inventory item) {
        String query = "UPDATE InventoryItems SET product_name = ?, category = ?, quantity = ?, make_price = ?, sell_price = ? WHERE product_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setString(1, item.getProductName());
            pstmt.setInt(2, item.getCategoryId());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setDouble(4, item.getMakePrice());
            pstmt.setDouble(5, item.getSellPrice());
            pstmt.setInt(6, item.getProductId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addInventoryItem(Inventory item) {
        String query = "INSERT INTO InventoryItems (product_name, category, quantity, make_price, sell_price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, item.getProductName());
            pstmt.setInt(2, item.getCategoryId());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setDouble(4, item.getMakePrice());
            pstmt.setDouble(5, item.getSellPrice());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setProductId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteInventoryItem(int productId) {
        String query = "UPDATE InventoryItems SET is_deleted = TRUE WHERE product_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to delete inventory item: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean updateStock(int productId, int quantity) {
        String query = """
            UPDATE InventoryItems 
            SET quantity = ?, 
                status = CASE 
                    WHEN ? = 0 THEN 'Unavailable'
                    ELSE 'Available'
                END,
                last_restock = CURRENT_TIMESTAMP
            WHERE product_id = ?
        """;
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, quantity);
            pstmt.setInt(3, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Inventory> getDeletedInventoryItems() {
        List<Inventory> items = new ArrayList<>();
        String query = """
            SELECT i.*, c.category_name,
                   CASE 
                       WHEN i.quantity = 0 THEN 'Unavailable'
                       ELSE 'Available'
                   END as status
            FROM InventoryItems i 
            JOIN Categories c ON i.category_id = c.category_id 
            WHERE i.is_deleted = TRUE
            ORDER BY i.product_name
        """;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Inventory item = new Inventory(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("make_price"),
                    rs.getDouble("sell_price"),
                    rs.getString("status")
                );
                item.setRecipeInstructions(rs.getString("recipe_instructions"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch deleted inventory items: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return items;
    }

    public boolean restoreInventoryItem(int productId) {
        String query = "UPDATE InventoryItems SET is_deleted = FALSE WHERE product_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to restore inventory item: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private Inventory mapResultSetToInventory(ResultSet rs) throws SQLException {
        return new Inventory(
            rs.getInt("product_id"),
            rs.getString("product_name"),
            rs.getString("category_name"),
            rs.getInt("quantity"),
            rs.getDouble("make_price"),
            rs.getDouble("sell_price")
        );
    }
} 