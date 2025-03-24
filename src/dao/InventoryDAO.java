package dao;

import model.Inventory;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class InventoryDAO {
    private final Connection connection;

    public InventoryDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    public List<Inventory> getAllInventoryItems() throws SQLException {
        List<Inventory> items = new ArrayList<>();
        String query = """
            SELECT i.*, c.category_name,
                   CASE 
                       WHEN i.quantity = 0 THEN 'Unavailable'
                       ELSE 'Available'
                   END as status
            FROM InventoryItems i 
            JOIN Categories c ON i.category_id = c.category_id 
            WHERE i.is_deleted = FALSE
            ORDER BY i.product_name
        """;
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                items.add(createInventoryFromResultSet(rs));
            }
        }
        return items;
    }

    public List<Inventory> getInventoryItemsByCategory(String categoryName) throws SQLException {
        List<Inventory> items = new ArrayList<>();
        String query = """
            SELECT i.*, c.category_name,
                   CASE 
                       WHEN i.quantity = 0 THEN 'Unavailable'
                       ELSE 'Available'
                   END as status
            FROM InventoryItems i 
            JOIN Categories c ON i.category_id = c.category_id 
            WHERE c.category_name = ? AND i.is_deleted = FALSE
            ORDER BY i.product_name
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, categoryName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(createInventoryFromResultSet(rs));
                }
            }
        }
        return items;
    }

    public Inventory getInventoryItemById(int id) throws SQLException {
        String query = """
            SELECT i.*, c.category_name,
                   CASE 
                       WHEN i.quantity = 0 THEN 'Unavailable'
                       ELSE 'Available'
                   END as status
            FROM InventoryItems i 
            JOIN Categories c ON i.category_id = c.category_id 
            WHERE i.product_id = ? AND i.is_deleted = FALSE
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return createInventoryFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public boolean addInventoryItem(String name, String categoryName, double makePrice, 
                                  double sellPrice, int quantity, String recipe, int employeeId) throws SQLException {
        int categoryId = getCategoryId(categoryName);
        if (categoryId == -1) {
            throw new SQLException("Invalid category: " + categoryName);
        }

        String query = """
            INSERT INTO InventoryItems (
                product_name, category_id, make_price, sell_price, quantity, 
                status, recipe_instructions, last_restocked_by
            ) VALUES (?, ?, ?, ?, ?, 'Available', ?, ?)
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setInt(2, categoryId);
            stmt.setDouble(3, makePrice);
            stmt.setDouble(4, sellPrice);
            stmt.setInt(5, quantity);
            stmt.setString(6, recipe);
            stmt.setInt(7, employeeId);
            
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateInventoryItem(int id, String name, String categoryName, 
                                     double makePrice, double sellPrice, 
                                     int quantity, String recipe) throws SQLException {
        int categoryId = getCategoryId(categoryName);
        if (categoryId == -1) {
            throw new SQLException("Invalid category: " + categoryName);
        }

        String query = """
            UPDATE InventoryItems 
            SET product_name = ?, category_id = ?, make_price = ?, 
                sell_price = ?, quantity = ?, recipe_instructions = ?,
                status = CASE 
                    WHEN ? = 0 THEN 'Unavailable'
                    ELSE 'Available'
                END
            WHERE product_id = ? AND is_deleted = FALSE
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setInt(2, categoryId);
            stmt.setDouble(3, makePrice);
            stmt.setDouble(4, sellPrice);
            stmt.setInt(5, quantity);
            stmt.setString(6, recipe);
            stmt.setInt(7, quantity);
            stmt.setInt(8, id);
            
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteInventoryItem(int id) throws SQLException {
        String query = "UPDATE InventoryItems SET is_deleted = TRUE WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean restoreInventoryItem(int productId) throws SQLException {
        String query = "UPDATE InventoryItems SET is_deleted = FALSE WHERE product_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean restockInventory(int productId, int newQuantity, int employeeId) throws SQLException {
        String query = """
            UPDATE InventoryItems 
            SET quantity = ?, 
                last_restocked_by = ?,
                status = CASE 
                    WHEN ? = 0 THEN 'Unavailable'
                    ELSE 'Available'
                END
            WHERE product_id = ? AND is_deleted = FALSE
        """;
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, employeeId);
            stmt.setInt(3, newQuantity);
            stmt.setInt(4, productId);
            
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Inventory> getDeletedInventoryItems() throws SQLException {
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
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                items.add(createInventoryFromResultSet(rs));
            }
        }
        return items;
    }

    private Inventory createInventoryFromResultSet(ResultSet rs) throws SQLException {
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
        return item;
    }

    private int getCategoryId(String categoryName) throws SQLException {
        String query = "SELECT category_id FROM Categories WHERE category_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, categoryName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("category_id");
                }
            }
        }
        return -1;
    }
} 