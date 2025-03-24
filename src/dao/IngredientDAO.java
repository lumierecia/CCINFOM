package dao;

import model.Ingredient;
import model.Ingredient.SupplierPrice;
import util.DatabaseConnection;
import javax.swing.JOptionPane;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class IngredientDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT i.*, u.unit_name FROM Ingredients i " +
                      "JOIN Units u ON i.unit_id = u.unit_id " +
                      "ORDER BY i.name";
        
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ingredient ingredient = createIngredientFromResultSet(rs);
                loadSupplierPrices(ingredient);
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch ingredients: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return ingredients;
    }

    public Ingredient getIngredientById(int ingredientId) {
        String query = "SELECT i.*, u.unit_name FROM Ingredients i " +
                      "JOIN Units u ON i.unit_id = u.unit_id " +
                      "WHERE i.ingredient_id = ?";
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Ingredient ingredient = createIngredientFromResultSet(rs);
                    loadSupplierPrices(ingredient);
                    return ingredient;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch ingredient: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private Ingredient createIngredientFromResultSet(ResultSet rs) throws SQLException {
        return new Ingredient(
            rs.getInt("ingredient_id"),
            rs.getString("name"),
            rs.getInt("unit_id"),
            rs.getString("unit_name"),
            rs.getDouble("quantity_in_stock"),
            rs.getDouble("minimum_stock_level"),
            rs.getDouble("cost_per_unit"),
            rs.getTimestamp("last_restock_date"),
            rs.getInt("last_restocked_by")
        );
    }

    private void loadSupplierPrices(Ingredient ingredient) throws SQLException {
        String query = "SELECT * FROM IngredientSuppliers WHERE ingredient_id = ?";
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, ingredient.getIngredientId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SupplierPrice price = new SupplierPrice(
                        rs.getDouble("unit_price"),
                        rs.getInt("lead_time_days"),
                        rs.getDouble("minimum_order_quantity"),
                        rs.getBoolean("is_primary_supplier")
                    );
                    ingredient.addSupplierPrice(rs.getInt("supplier_id"), price);
                }
            }
        }
    }

    public boolean addIngredient(Ingredient ingredient) {
        String query = "INSERT INTO Ingredients (name, unit_id, quantity_in_stock, " +
                      "minimum_stock_level, cost_per_unit, last_restocked_by) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, ingredient.getName());
                stmt.setInt(2, ingredient.getUnitId());
                stmt.setDouble(3, ingredient.getQuantityInStock());
                stmt.setDouble(4, ingredient.getMinimumStockLevel());
                stmt.setDouble(5, ingredient.getCostPerUnit());
                stmt.setInt(6, ingredient.getLastRestockedBy());
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            ingredient.setIngredientId(generatedKeys.getInt(1));
                            if (updateSupplierPrices(ingredient, conn)) {
                                conn.commit();
                                return true;
                            }
                        }
                    }
                }
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to add ingredient: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
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

    private boolean updateSupplierPrices(Ingredient ingredient, Connection conn) throws SQLException {
        String deleteQuery = "DELETE FROM IngredientSuppliers WHERE ingredient_id = ?";
        String insertQuery = "INSERT INTO IngredientSuppliers (ingredient_id, supplier_id, " +
                           "unit_price, lead_time_days, minimum_order_quantity, is_primary_supplier) " +
                           "VALUES (?, ?, ?, ?, ?, ?)";
        
        // First delete existing supplier prices
        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
            deleteStmt.setInt(1, ingredient.getIngredientId());
            deleteStmt.executeUpdate();
        }
        
        // Then insert new ones
        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            for (Map.Entry<Integer, SupplierPrice> entry : ingredient.getAllSupplierPrices().entrySet()) {
                insertStmt.setInt(1, ingredient.getIngredientId());
                insertStmt.setInt(2, entry.getKey());
                insertStmt.setDouble(3, entry.getValue().getUnitPrice());
                insertStmt.setInt(4, entry.getValue().getLeadTimeDays());
                insertStmt.setDouble(5, entry.getValue().getMinimumOrderQuantity());
                insertStmt.setBoolean(6, entry.getValue().isPrimarySupplier());
                insertStmt.addBatch();
            }
            int[] results = insertStmt.executeBatch();
            for (int result : results) {
                if (result <= 0) return false;
            }
            return true;
        }
    }

    public boolean updateIngredient(Ingredient ingredient) {
        String query = "UPDATE Ingredients SET name = ?, unit_id = ?, quantity_in_stock = ?, " +
                      "minimum_stock_level = ?, cost_per_unit = ?, last_restock_date = ?, " +
                      "last_restocked_by = ? WHERE ingredient_id = ?";
        
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, ingredient.getName());
                stmt.setInt(2, ingredient.getUnitId());
                stmt.setDouble(3, ingredient.getQuantityInStock());
                stmt.setDouble(4, ingredient.getMinimumStockLevel());
                stmt.setDouble(5, ingredient.getCostPerUnit());
                stmt.setTimestamp(6, new Timestamp(ingredient.getLastRestockDate().getTime()));
                stmt.setInt(7, ingredient.getLastRestockedBy());
                stmt.setInt(8, ingredient.getIngredientId());
                
                if (stmt.executeUpdate() > 0 && updateSupplierPrices(ingredient, conn)) {
                    conn.commit();
                    return true;
                }
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to update ingredient: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
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

    public boolean deleteIngredient(int ingredientId) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // First check if ingredient is used in any dishes
            String checkQuery = "SELECT COUNT(*) FROM DishIngredients WHERE ingredient_id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, ingredientId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(null,
                            "Cannot delete ingredient because it is used in one or more dishes.",
                            "Delete Failed",
                            JOptionPane.WARNING_MESSAGE);
                        return false;
                    }
                }
            }

            // If not in use, delete supplier prices first
            String deleteSupplierPricesQuery = "DELETE FROM IngredientSuppliers WHERE ingredient_id = ?";
            try (PreparedStatement deleteSupplierPricesStmt = conn.prepareStatement(deleteSupplierPricesQuery)) {
                deleteSupplierPricesStmt.setInt(1, ingredientId);
                deleteSupplierPricesStmt.executeUpdate();
            }
            
            // Then delete the ingredient
            String deleteIngredientQuery = "UPDATE Ingredients SET is_deleted = TRUE WHERE ingredient_id = ?";
            try (PreparedStatement deleteIngredientStmt = conn.prepareStatement(deleteIngredientQuery)) {
                deleteIngredientStmt.setInt(1, ingredientId);
                if (deleteIngredientStmt.executeUpdate() > 0) {
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
            JOptionPane.showMessageDialog(null,
                "Failed to delete ingredient: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
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

    public List<Ingredient> getLowStockIngredients() {
        List<Ingredient> lowStockIngredients = new ArrayList<>();
        String query = "SELECT i.*, u.unit_name FROM Ingredients i " +
                      "JOIN Units u ON i.unit_id = u.unit_id " +
                      "WHERE i.quantity_in_stock <= i.minimum_stock_level AND i.is_deleted = FALSE";
        
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ingredient ingredient = createIngredientFromResultSet(rs);
                loadSupplierPrices(ingredient);
                lowStockIngredients.add(ingredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch low stock ingredients: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return lowStockIngredients;
    }

    public boolean updateStock(int ingredientId, double quantity, int employeeId, String transactionType, String notes) {
        Connection conn = null;
        try {
            conn = getConnection();
            
            // Update stock level
            String updateQuery = "UPDATE Ingredients SET quantity_in_stock = quantity_in_stock + ?, " +
                               "last_restock_date = CURRENT_TIMESTAMP, last_restocked_by = ? " +
                               "WHERE ingredient_id = ? AND is_deleted = FALSE";
            
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setDouble(1, quantity);
                updateStmt.setInt(2, employeeId);
                updateStmt.setInt(3, ingredientId);
                
                if (updateStmt.executeUpdate() > 0) {
                    // Record the transaction
                    String transactionQuery = "INSERT INTO IngredientTransactions " +
                                           "(ingredient_id, quantity_change, employee_id, " +
                                           "transaction_type, notes) VALUES (?, ?, ?, ?, ?)";
                    
                    try (PreparedStatement transactionStmt = conn.prepareStatement(transactionQuery)) {
                        transactionStmt.setInt(1, ingredientId);
                        transactionStmt.setDouble(2, quantity);
                        transactionStmt.setInt(3, employeeId);
                        transactionStmt.setString(4, transactionType);
                        transactionStmt.setString(5, notes);
                        
                        return transactionStmt.executeUpdate() > 0;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to update stock: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public Map<Integer, Double> calculateIngredientCosts(int productId) {
        Map<Integer, Double> costs = new HashMap<>();
        String query = "SELECT di.ingredient_id, di.quantity_needed * i.cost_per_unit as total_cost " +
                      "FROM DishIngredients di " +
                      "JOIN Ingredients i ON di.ingredient_id = i.ingredient_id " +
                      "WHERE di.product_id = ? AND i.is_deleted = FALSE";
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    costs.put(rs.getInt("ingredient_id"), rs.getDouble("total_cost"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to calculate ingredient costs: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return costs;
    }

    public List<Ingredient> getDeletedIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = """
            SELECT i.*, u.unit_name
            FROM Ingredients i 
            JOIN Units u ON i.unit_id = u.unit_id
            WHERE i.is_deleted = TRUE
            ORDER BY i.name
        """;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ingredient ingredient = mapResultSetToIngredient(rs);
                loadSupplierPrices(ingredient);
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients;
    }

    private Ingredient mapResultSetToIngredient(ResultSet rs) throws SQLException {
        return new Ingredient(
            rs.getInt("ingredient_id"),
            rs.getString("name"),
            rs.getInt("unit_id"),
            rs.getString("unit_name"),
            rs.getDouble("quantity_in_stock"),
            rs.getDouble("minimum_stock_level"),
            rs.getDouble("cost_per_unit"),
            rs.getTimestamp("last_restock_date"),
            rs.getInt("last_restocked_by")
        );
    }

    public boolean restoreIngredient(int ingredientId) {
        String query = "UPDATE Ingredients SET is_deleted = FALSE WHERE ingredient_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 
