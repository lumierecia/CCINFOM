package dao;

import model.Inventory;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public List<Inventory> getAllInventoryItems() {
        List<Inventory> items = new ArrayList<>();
        String query = "SELECT * FROM InventoryItems ORDER BY product_name";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Inventory item = new Inventory(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    rs.getDouble("make_price"),
                    rs.getDouble("sell_price")
                );
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public Inventory getInventoryItemById(int productId) {
        String query = "SELECT * FROM InventoryItems WHERE product_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Inventory(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("category"),
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
        String query = "DELETE FROM InventoryItems WHERE product_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStock(int productId, int quantity) {
        String query = "UPDATE InventoryItems SET quantity = quantity + ? WHERE product_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 