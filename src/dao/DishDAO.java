package dao;

import model.Dish;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import java.util.Map;
import java.util.HashMap;

public class DishDAO {
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public List<Dish> getAllDishes() {
        List<Dish> dishes = new ArrayList<>();
        String query = """
            SELECT d.*, c.category_name 
            FROM Dishes d 
            JOIN Categories c ON d.category_id = c.category_id 
            WHERE d.is_deleted = FALSE
            ORDER BY d.name
        """;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                dishes.add(mapResultSetToDish(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch dishes: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return dishes;
    }

    public List<Dish> getDishesByCategory(String categoryName) {
        List<Dish> dishes = new ArrayList<>();
        String query = """
            SELECT d.*, c.category_name 
            FROM Dishes d 
            JOIN Categories c ON d.category_id = c.category_id 
            WHERE c.category_name = ? AND d.is_deleted = FALSE
            ORDER BY d.name
        """;
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, categoryName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    dishes.add(mapResultSetToDish(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch dishes by category: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return dishes;
    }

    public Dish getDishById(int dishId) {
        String query = """
            SELECT d.*, c.category_name 
            FROM Dishes d 
            JOIN Categories c ON d.category_id = c.category_id 
            WHERE d.dish_id = ? AND d.is_deleted = FALSE
        """;
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, dishId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDish(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch dish: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    public boolean addDish(Dish dish) {
        String query = """
            INSERT INTO Dishes (name, category_id, selling_price, recipe_instructions, is_available) 
            VALUES (?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, dish.getName());
            stmt.setInt(2, getCategoryId(dish.getCategoryName()));
            stmt.setDouble(3, dish.getSellingPrice());
            stmt.setString(4, dish.getRecipeInstructions());
            stmt.setBoolean(5, dish.isAvailable());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        dish.setDishId(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to add dish: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    public boolean updateDish(Dish dish) {
        String query = """
            UPDATE Dishes 
            SET name = ?, category_id = ?, selling_price = ?, 
                recipe_instructions = ?, is_available = ? 
            WHERE dish_id = ?
        """;
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, dish.getName());
            stmt.setInt(2, getCategoryId(dish.getCategoryName()));
            stmt.setDouble(3, dish.getSellingPrice());
            stmt.setString(4, dish.getRecipeInstructions());
            stmt.setBoolean(5, dish.isAvailable());
            stmt.setInt(6, dish.getDishId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to update dish: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    public boolean deleteDish(int dishId) {
        String query = "UPDATE Dishes SET is_deleted = TRUE WHERE dish_id = ?";
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, dishId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to delete dish: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private Dish mapResultSetToDish(ResultSet rs) throws SQLException {
        Dish dish = new Dish();
        dish.setDishId(rs.getInt("dish_id"));
        dish.setName(rs.getString("name"));
        dish.setCategoryName(rs.getString("category_name"));
        dish.setSellingPrice(rs.getDouble("selling_price"));
        dish.setRecipeInstructions(rs.getString("recipe_instructions"));
        dish.setAvailable(rs.getBoolean("is_available"));
        return dish;
    }

    private int getCategoryId(String categoryName) throws SQLException {
        String query = "SELECT category_id FROM Categories WHERE category_name = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, categoryName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("category_id");
                }
            }
        }
        throw new SQLException("Category not found: " + categoryName);
    }

    public Map<Integer, Double> getDishIngredients(int dishId) {
        Map<Integer, Double> ingredients = new HashMap<>();
        String query = """
            SELECT ingredient_id, quantity_needed
            FROM DishIngredients
            WHERE dish_id = ?
        """;
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, dishId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ingredients.put(
                        rs.getInt("ingredient_id"),
                        rs.getDouble("quantity_needed")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch dish ingredients: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return ingredients;
    }
} 