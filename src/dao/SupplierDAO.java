package dao;

import model.Supplier;
import model.Inventory;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class SupplierDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public Supplier getSupplierById(int supplierId) {
        String query = "SELECT * FROM Suppliers WHERE supplier_id = ? AND is_deleted = FALSE";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, supplierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSupplier(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String query = "SELECT * FROM Suppliers WHERE is_deleted = FALSE ORDER BY name";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    public boolean addSupplier(Supplier supplier) {
        String query = "INSERT INTO Suppliers (name, contact_person, email, phone, address, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getContactPerson());
            pstmt.setString(3, supplier.getEmail());
            pstmt.setString(4, supplier.getPhone());
            pstmt.setString(5, supplier.getAddress());
            pstmt.setString(6, supplier.getStatus());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        supplier.setSupplierId(generatedKeys.getInt(1));
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

    public boolean updateSupplier(Supplier supplier) {
        String query = "UPDATE Suppliers SET name = ?, contact_person = ?, email = ?, phone = ?, address = ?, status = ? WHERE supplier_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setString(1, supplier.getName());
            pstmt.setString(2, supplier.getContactPerson());
            pstmt.setString(3, supplier.getEmail());
            pstmt.setString(4, supplier.getPhone());
            pstmt.setString(5, supplier.getAddress());
            pstmt.setString(6, supplier.getStatus());
            pstmt.setInt(7, supplier.getSupplierId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSupplier(int supplierId) {
        // First check if supplier has any active ingredient relationships
        String checkQuery = "SELECT COUNT(*) FROM IngredientSuppliers WHERE supplier_id = ? AND is_primary_supplier = TRUE";
        
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            
            checkStmt.setInt(1, supplierId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(null,
                        "Cannot delete supplier because they are a primary supplier for some ingredients.",
                        "Delete Failed",
                        JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            }
            
            // If not a primary supplier, proceed with soft delete
            String updateQuery = "UPDATE Suppliers SET is_deleted = TRUE WHERE supplier_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, supplierId);
                return updateStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to delete supplier: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public List<Inventory> getSupplierIngredients(int supplierId) {
        List<Inventory> ingredients = new ArrayList<>();
        String query = """
            SELECT i.*, s.unit_price, s.lead_time_days, s.minimum_order_quantity, s.is_primary_supplier
            FROM Ingredients i
            JOIN IngredientSuppliers s ON i.ingredient_id = s.ingredient_id
            WHERE s.supplier_id = ?
        """;
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, supplierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Inventory ingredient = new Inventory(
                        rs.getInt("ingredient_id"),
                        rs.getString("name"),
                        "Ingredient",
                        rs.getInt("quantity_in_stock"),
                        rs.getDouble("cost_per_unit"),
                        rs.getDouble("unit_price")
                    );
                    ingredients.add(ingredient);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients;
    }

    public List<Supplier> searchSuppliers(String searchTerm) {
        List<Supplier> suppliers = new ArrayList<>();
        String query = "SELECT * FROM Suppliers WHERE is_deleted = FALSE AND " +
                      "(name LIKE ? OR contact_person LIKE ? OR email LIKE ?) " +
                      "ORDER BY name";
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            String term = "%" + searchTerm + "%";
            pstmt.setString(1, term);
            pstmt.setString(2, term);
            pstmt.setString(3, term);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(mapResultSetToSupplier(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    public List<Supplier> getDeletedSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String query = "SELECT * FROM Suppliers WHERE is_deleted = TRUE ORDER BY name";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Supplier supplier = new Supplier(
                    rs.getInt("supplier_id"),
                    rs.getString("name"),
                    rs.getString("contact_person"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address"),
                    rs.getString("status")
                );
                suppliers.add(supplier);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch deleted suppliers: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return suppliers;
    }

    public boolean restoreSupplier(int supplierId) {
        String query = "UPDATE Suppliers SET is_deleted = FALSE WHERE supplier_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, supplierId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to restore supplier: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        return new Supplier(
            rs.getInt("supplier_id"),
            rs.getString("name"),
            rs.getString("contact_person"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("address"),
            rs.getString("status")
        );
    }
} 