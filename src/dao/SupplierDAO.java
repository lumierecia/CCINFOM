package dao;

import model.Supplier;
import model.Inventory;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public Supplier getSupplierById(int supplierId) {
        String query = "SELECT * FROM Suppliers WHERE supplier_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, supplierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String query = "SELECT * FROM Suppliers";
        try (Statement stmt = getConnection().createStatement();
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
        String query = "DELETE FROM Suppliers WHERE supplier_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, supplierId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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
} 