package dao;

import model.Ingredient;
import model.Ingredient.SupplierPrice;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class IngredientDAO {
    private Connection connection;

    public IngredientDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT * FROM Ingredients ORDER BY name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ingredient ingredient = createIngredientFromResultSet(rs);
                loadSupplierPrices(ingredient);
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients;
    }

    public Ingredient getIngredientById(int ingredientId) {
        String query = "SELECT * FROM Ingredients WHERE ingredient_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, ingredientId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Ingredient ingredient = createIngredientFromResultSet(rs);
                    loadSupplierPrices(ingredient);
                    return ingredient;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Ingredient createIngredientFromResultSet(ResultSet rs) throws SQLException {
        return new Ingredient(
            rs.getInt("ingredient_id"),
            rs.getString("name"),
            rs.getString("unit"),
            rs.getDouble("quantity_in_stock"),
            rs.getDouble("minimum_stock_level"),
            rs.getDouble("cost_per_unit"),
            rs.getTimestamp("last_restock_date"),
            rs.getInt("last_restocked_by")
        );
    }

    private void loadSupplierPrices(Ingredient ingredient) {
        String query = "SELECT * FROM IngredientSuppliers WHERE ingredient_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, ingredient.getIngredientId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addIngredient(Ingredient ingredient) {
        String query = "INSERT INTO Ingredients (name, unit, quantity_in_stock, minimum_stock_level, cost_per_unit, last_restocked_by) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, ingredient.getName());
            pstmt.setString(2, ingredient.getUnit());
            pstmt.setDouble(3, ingredient.getQuantityInStock());
            pstmt.setDouble(4, ingredient.getMinimumStockLevel());
            pstmt.setDouble(5, ingredient.getCostPerUnit());
            pstmt.setInt(6, ingredient.getLastRestockedBy());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        ingredient.setIngredientId(generatedKeys.getInt(1));
                        return updateSupplierPrices(ingredient);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateSupplierPrices(Ingredient ingredient) {
        String deleteQuery = "DELETE FROM IngredientSuppliers WHERE ingredient_id = ?";
        String insertQuery = "INSERT INTO IngredientSuppliers (ingredient_id, supplier_id, unit_price, lead_time_days, minimum_order_quantity, is_primary_supplier) " +
                           "VALUES (?, ?, ?, ?, ?, ?)";
        
        try {
            // First delete existing supplier prices
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                deleteStmt.setInt(1, ingredient.getIngredientId());
                deleteStmt.executeUpdate();
            }
            
            // Then insert new ones
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                for (Map.Entry<Integer, SupplierPrice> entry : ingredient.getAllSupplierPrices().entrySet()) {
                    insertStmt.setInt(1, ingredient.getIngredientId());
                    insertStmt.setInt(2, entry.getKey());
                    insertStmt.setDouble(3, entry.getValue().getUnitPrice());
                    insertStmt.setInt(4, entry.getValue().getLeadTimeDays());
                    insertStmt.setDouble(5, entry.getValue().getMinimumOrderQuantity());
                    insertStmt.setBoolean(6, entry.getValue().isPrimarySupplier());
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateIngredient(Ingredient ingredient) {
        String query = "UPDATE Ingredients SET name = ?, unit = ?, quantity_in_stock = ?, " +
                      "minimum_stock_level = ?, cost_per_unit = ?, last_restock_date = ?, " +
                      "last_restocked_by = ? WHERE ingredient_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, ingredient.getName());
            pstmt.setString(2, ingredient.getUnit());
            pstmt.setDouble(3, ingredient.getQuantityInStock());
            pstmt.setDouble(4, ingredient.getMinimumStockLevel());
            pstmt.setDouble(5, ingredient.getCostPerUnit());
            pstmt.setTimestamp(6, ingredient.getLastRestockDate());
            pstmt.setInt(7, ingredient.getLastRestockedBy());
            pstmt.setInt(8, ingredient.getIngredientId());
            
            if (pstmt.executeUpdate() > 0) {
                return updateSupplierPrices(ingredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteIngredient(int ingredientId) {
        // First check if ingredient is used in any dishes
        String checkQuery = "SELECT COUNT(*) FROM DishIngredients WHERE ingredient_id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, ingredientId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // Ingredient is in use, cannot delete
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // If not in use, delete supplier prices first
        String deleteSupplierPricesQuery = "DELETE FROM IngredientSuppliers WHERE ingredient_id = ?";
        String deleteIngredientQuery = "DELETE FROM Ingredients WHERE ingredient_id = ?";
        
        try {
            connection.setAutoCommit(false);
            
            try (PreparedStatement deleteSupplierPricesStmt = connection.prepareStatement(deleteSupplierPricesQuery)) {
                deleteSupplierPricesStmt.setInt(1, ingredientId);
                deleteSupplierPricesStmt.executeUpdate();
            }
            
            try (PreparedStatement deleteIngredientStmt = connection.prepareStatement(deleteIngredientQuery)) {
                deleteIngredientStmt.setInt(1, ingredientId);
                boolean success = deleteIngredientStmt.executeUpdate() > 0;
                if (success) {
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Ingredient> getLowStockIngredients() {
        List<Ingredient> lowStockIngredients = new ArrayList<>();
        String query = "SELECT * FROM Ingredients WHERE quantity_in_stock <= minimum_stock_level";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Ingredient ingredient = createIngredientFromResultSet(rs);
                loadSupplierPrices(ingredient);
                lowStockIngredients.add(ingredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lowStockIngredients;
    }

    public boolean updateStock(int ingredientId, double quantity, int employeeId, String transactionType, String notes) {
        String updateQuery = "UPDATE Ingredients SET quantity_in_stock = quantity_in_stock + ?, " +
                           "last_restock_date = CURRENT_TIMESTAMP, last_restocked_by = ? " +
                           "WHERE ingredient_id = ?";
        
        String transactionQuery = "INSERT INTO IngredientTransactions (ingredient_id, transaction_type, " +
                                "quantity, recorded_by, notes) VALUES (?, ?, ?, ?, ?)";
        
        try {
            connection.setAutoCommit(false);
            
            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                updateStmt.setDouble(1, quantity);
                updateStmt.setInt(2, employeeId);
                updateStmt.setInt(3, ingredientId);
                updateStmt.executeUpdate();
            }
            
            try (PreparedStatement transactionStmt = connection.prepareStatement(transactionQuery)) {
                transactionStmt.setInt(1, ingredientId);
                transactionStmt.setString(2, transactionType);
                transactionStmt.setDouble(3, quantity);
                transactionStmt.setInt(4, employeeId);
                transactionStmt.setString(5, notes);
                transactionStmt.executeUpdate();
            }
            
            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<Integer, Double> calculateIngredientCosts(int productId) {
        Map<Integer, Double> costs = new HashMap<>();
        String query = """
            SELECT di.ingredient_id, di.quantity_needed * i.cost_per_unit as cost
            FROM DishIngredients di
            JOIN Ingredients i ON di.ingredient_id = i.ingredient_id
            WHERE di.product_id = ?
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, productId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    costs.put(rs.getInt("ingredient_id"), rs.getDouble("cost"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return costs;
    }
} 