package dao;

import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DishIngredientDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public boolean addIngredientToDish(int dishId, int ingredientId, double quantity, String unit) throws SQLException {
        String query = "INSERT INTO DishIngredients (dish_id, ingredient_id, quantity, unit) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, dishId);
            stmt.setInt(2, ingredientId);
            stmt.setDouble(3, quantity);
            stmt.setString(4, unit);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateIngredientInDish(int dishId, int ingredientId, double quantity, String unit) throws SQLException {
        String query = "UPDATE DishIngredients SET quantity = ?, unit = ? WHERE dish_id = ? AND ingredient_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setDouble(1, quantity);
            stmt.setString(2, unit);
            stmt.setInt(3, dishId);
            stmt.setInt(4, ingredientId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean removeIngredientFromDish(int dishId, int ingredientId) throws SQLException {
        String query = "DELETE FROM DishIngredients WHERE dish_id = ? AND ingredient_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, dishId);
            stmt.setInt(2, ingredientId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean removeAllIngredientsFromDish(int dishId) throws SQLException {
        String query = "DELETE FROM DishIngredients WHERE dish_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, dishId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Map<String, Object>> getDishIngredients(int dishId) throws SQLException {
        List<Map<String, Object>> ingredients = new ArrayList<>();
        String query = """
            SELECT di.*, i.name as ingredient_name, i.unit_price
            FROM DishIngredients di
            JOIN Ingredients i ON di.ingredient_id = i.ingredient_id
            WHERE di.dish_id = ?
            ORDER BY i.name
        """;
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, dishId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> ingredient = new HashMap<>();
                    ingredient.put("ingredientId", rs.getInt("ingredient_id"));
                    ingredient.put("ingredientName", rs.getString("ingredient_name"));
                    ingredient.put("quantity", rs.getDouble("quantity"));
                    ingredient.put("unit", rs.getString("unit"));
                    ingredient.put("unitPrice", rs.getDouble("unit_price"));
                    ingredients.add(ingredient);
                }
            }
        }
        return ingredients;
    }

    public List<Map<String, Object>> getIngredientUsage(int ingredientId) throws SQLException {
        List<Map<String, Object>> usage = new ArrayList<>();
        String query = """
            SELECT di.*, d.name as dish_name, d.selling_price
            FROM DishIngredients di
            JOIN Dishes d ON di.dish_id = d.dish_id
            WHERE di.ingredient_id = ? AND d.is_deleted = FALSE
            ORDER BY d.name
        """;
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("dishId", rs.getInt("dish_id"));
                    item.put("dishName", rs.getString("dish_name"));
                    item.put("quantity", rs.getDouble("quantity"));
                    item.put("unit", rs.getString("unit"));
                    item.put("sellingPrice", rs.getDouble("selling_price"));
                    usage.add(item);
                }
            }
        }
        return usage;
    }

    public List<Map<String, Object>> getDishesByIngredient(int ingredientId) throws SQLException {
        List<Map<String, Object>> dishes = new ArrayList<>();
        String query = """
            SELECT di.*, d.name as dish_name, d.selling_price, d.is_available
            FROM DishIngredients di
            JOIN Dishes d ON di.dish_id = d.dish_id
            WHERE di.ingredient_id = ? AND d.is_deleted = FALSE
        """;
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, ingredientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> dish = new HashMap<>();
                    dish.put("dishId", rs.getInt("dish_id"));
                    dish.put("dishName", rs.getString("dish_name"));
                    dish.put("quantityNeeded", rs.getDouble("quantity"));
                    dish.put("sellingPrice", rs.getDouble("selling_price"));
                    dish.put("isAvailable", rs.getBoolean("is_available"));
                    dishes.add(dish);
                }
            }
        }
        return dishes;
    }

    public boolean checkIngredientAvailability(int dishId) throws SQLException {
        String query = """
            SELECT i.quantity_in_stock, di.quantity
            FROM DishIngredients di
            JOIN Ingredients i ON di.ingredient_id = i.ingredient_id
            WHERE di.dish_id = ? AND i.is_deleted = FALSE
        """;
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, dishId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (rs.getDouble("quantity_in_stock") < rs.getDouble("quantity")) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
} 