package dao;

import model.Dish;
import model.DishIngredient;
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

    public int createDish(Dish dish) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO Dishes (
                    name, category_id, selling_price, recipe_instructions, is_available
                ) VALUES (?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, dish.getName());
            stmt.setInt(2, dish.getCategoryId());
            stmt.setDouble(3, dish.getSellingPrice());
            stmt.setString(4, dish.getRecipeInstructions());
            stmt.setBoolean(5, dish.isAvailable());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int dishId = rs.getInt(1);
                        if (!dish.getIngredients().isEmpty()) {
                            addDishIngredients(dishId, dish.getIngredients());
                        }
                        return dishId;
                    }
                }
            }
            return -1;
        }
    }

    public boolean addDishIngredients(int dishId, List<DishIngredient> ingredients) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO DishIngredients (
                    dish_id, ingredient_id, quantity_needed, unit_id
                ) VALUES (?, ?, ?, ?)
                """)) {
            for (DishIngredient ingredient : ingredients) {
                stmt.setInt(1, dishId);
                stmt.setInt(2, ingredient.getIngredientId());
                stmt.setDouble(3, ingredient.getQuantityNeeded());
                stmt.setInt(4, ingredient.getUnitId());
                stmt.addBatch();
            }
            return stmt.executeBatch().length > 0;
        }
    }

    public Dish getDishById(int dishId) throws SQLException {
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
                    Dish dish = new Dish(
                            rs.getInt("dish_id"),
                            rs.getString("name"),
                            rs.getInt("category_id"),
                            rs.getDouble("selling_price"),
                            rs.getString("recipe_instructions"),
                            rs.getBoolean("is_available")
                    );
                    dish.setCategoryName(rs.getString("category_name"));
                    dish.setIngredients(getDishIngredients(dishId));
                    return dish;
                }
            }
        }
        return null;
    }

    public List<Dish> getAllDishes() throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        try (Connection conn = getConnection()) {
            // First get all dishes
            String query = """
                SELECT d.*, c.category_name
                FROM Dishes d
                JOIN Categories c ON d.category_id = c.category_id
                WHERE d.is_deleted = FALSE
                ORDER BY d.name
            """;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    Dish dish = new Dish(
                        rs.getInt("dish_id"),
                        rs.getString("name"),
                        rs.getInt("category_id"),
                        rs.getDouble("selling_price"),
                        rs.getString("recipe_instructions"),
                        rs.getBoolean("is_available")
                    );
                    dish.setCategoryName(rs.getString("category_name"));
                    
                    // Get ingredients using the same connection
                    String ingredientQuery = """
                        SELECT di.*, i.name as ingredient_name, u.unit_name
                        FROM DishIngredients di
                        JOIN Ingredients i ON di.ingredient_id = i.ingredient_id
                        JOIN Units u ON di.unit_id = u.unit_id
                        WHERE di.dish_id = ?
                    """;
                    try (PreparedStatement ingredientStmt = conn.prepareStatement(ingredientQuery)) {
                        ingredientStmt.setInt(1, dish.getDishId());
                        try (ResultSet ingredientRs = ingredientStmt.executeQuery()) {
                            List<DishIngredient> ingredients = new ArrayList<>();
                            while (ingredientRs.next()) {
                                DishIngredient ingredient = new DishIngredient(
                                    ingredientRs.getInt("dish_id"),
                                    ingredientRs.getInt("ingredient_id"),
                                    ingredientRs.getDouble("quantity_needed"),
                                    ingredientRs.getInt("unit_id")
                                );
                                ingredient.setIngredientName(ingredientRs.getString("ingredient_name"));
                                ingredient.setUnitName(ingredientRs.getString("unit_name"));
                                ingredients.add(ingredient);
                            }
                            dish.setIngredients(ingredients);
                        }
                    }
                    dishes.add(dish);
                }
            }
        }
        return dishes;
    }

    public List<Dish> getDishesByCategory(String category) throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        try (Connection conn = getConnection()) {
            String query = """
                SELECT d.*, c.category_name
                FROM Dishes d
                JOIN Categories c ON d.category_id = c.category_id
                WHERE c.category_name = ? AND d.is_deleted = FALSE
                ORDER BY d.name
            """;

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, category);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Dish dish = new Dish(
                            rs.getInt("dish_id"),
                            rs.getString("name"),
                            rs.getInt("category_id"),
                            rs.getDouble("selling_price"),
                            rs.getString("recipe_instructions"),
                            rs.getBoolean("is_available")
                        );
                        dish.setCategoryName(rs.getString("category_name"));
                        
                        // Get ingredients using the same connection
                        String ingredientQuery = """
                            SELECT di.*, i.name as ingredient_name, u.unit_name
                            FROM DishIngredients di
                            JOIN Ingredients i ON di.ingredient_id = i.ingredient_id
                            JOIN Units u ON di.unit_id = u.unit_id
                            WHERE di.dish_id = ?
                        """;
                        try (PreparedStatement ingredientStmt = conn.prepareStatement(ingredientQuery)) {
                            ingredientStmt.setInt(1, dish.getDishId());
                            try (ResultSet ingredientRs = ingredientStmt.executeQuery()) {
                                List<DishIngredient> ingredients = new ArrayList<>();
                                while (ingredientRs.next()) {
                                    DishIngredient ingredient = new DishIngredient(
                                        ingredientRs.getInt("dish_id"),
                                        ingredientRs.getInt("ingredient_id"),
                                        ingredientRs.getDouble("quantity_needed"),
                                        ingredientRs.getInt("unit_id")
                                    );
                                    ingredient.setIngredientName(ingredientRs.getString("ingredient_name"));
                                    ingredient.setUnitName(ingredientRs.getString("unit_name"));
                                    ingredients.add(ingredient);
                                }
                                dish.setIngredients(ingredients);
                            }
                        }
                        dishes.add(dish);
                    }
                }
            }
        }
        return dishes;
    }

    public boolean updateDish(Dish dish) throws SQLException {
        String query = """
            UPDATE Dishes 
            SET name = ?, category_id = ?, selling_price = ?, 
                recipe_instructions = ?, is_available = ?
            WHERE dish_id = ? AND is_deleted = FALSE
        """;

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, dish.getName());
            stmt.setInt(2, dish.getCategoryId());
            stmt.setDouble(3, dish.getSellingPrice());
            stmt.setString(4, dish.getRecipeInstructions());
            stmt.setBoolean(5, dish.isAvailable());
            stmt.setInt(6, dish.getDishId());

            boolean success = stmt.executeUpdate() > 0;
            if (success && !dish.getIngredients().isEmpty()) {
                // Delete existing ingredients
                String deleteQuery = "DELETE FROM DishIngredients WHERE dish_id = ?";
                try (PreparedStatement deleteStmt = getConnection().prepareStatement(deleteQuery)) {
                    deleteStmt.setInt(1, dish.getDishId());
                    deleteStmt.executeUpdate();
                }
                // Add new ingredients
                addDishIngredients(dish.getDishId(), dish.getIngredients());
            }
            return success;
        }
    }

    public List<DishIngredient> getDishIngredients(int dishId) throws SQLException {
        List<DishIngredient> ingredients = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                SELECT di.*, i.name as ingredient_name, u.unit_name
                FROM DishIngredients di
                JOIN Ingredients i ON di.ingredient_id = i.ingredient_id
                JOIN Units u ON di.unit_id = u.unit_id
                WHERE di.dish_id = ?
                """)) {
            stmt.setInt(1, dishId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    DishIngredient ingredient = new DishIngredient(
                            rs.getInt("dish_id"),
                            rs.getInt("ingredient_id"),
                            rs.getDouble("quantity_needed"),
                            rs.getInt("unit_id")
                    );
                    ingredient.setIngredientName(rs.getString("ingredient_name"));
                    ingredient.setUnitName(rs.getString("unit_name"));
                    ingredients.add(ingredient);
                }
            }
        }
        return ingredients;
    }

    public boolean createDish(String name, int categoryId, double sellingPrice, String recipeInstructions) throws SQLException {
        String query = "INSERT INTO Dishes (name, category_id, selling_price, recipe_instructions) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setInt(2, categoryId);
            stmt.setDouble(3, sellingPrice);
            stmt.setString(4, recipeInstructions);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateDish(int dishId, String name, int categoryId, double sellingPrice,
                              String recipeInstructions, boolean isAvailable) throws SQLException {
        String query = """
            UPDATE Dishes 
            SET name = ?, category_id = ?, selling_price = ?, 
                recipe_instructions = ?, is_available = ?
            WHERE dish_id = ? AND is_deleted = FALSE
        """;
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setInt(2, categoryId);
            stmt.setDouble(3, sellingPrice);
            stmt.setString(4, recipeInstructions);
            stmt.setBoolean(5, isAvailable);
            stmt.setInt(6, dishId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteDish(int dishId) throws SQLException {
        String query = "UPDATE Dishes SET is_deleted = TRUE WHERE dish_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, dishId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Dish> getAvailableDishes() throws SQLException {
        List<Dish> dishes = new ArrayList<>();
        String query = "SELECT * FROM Dishes WHERE is_available = TRUE AND is_deleted = FALSE ORDER BY name";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                dishes.add(new Dish(
                        rs.getInt("dish_id"),
                        rs.getString("name"),
                        rs.getInt("category_id"),
                        rs.getDouble("selling_price"),
                        rs.getString("recipe_instructions"),
                        rs.getBoolean("is_available")
                ));
            }
        }
        return dishes;
    }

    public boolean updateDishAvailability(int dishId, boolean isAvailable) throws SQLException {
        String query = "UPDATE Dishes SET is_available = ? WHERE dish_id = ? AND is_deleted = FALSE";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setBoolean(1, isAvailable);
            stmt.setInt(2, dishId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateDishPrice(int dishId, double newPrice) throws SQLException {
        String query = "UPDATE Dishes SET selling_price = ? WHERE dish_id = ? AND is_deleted = FALSE";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setDouble(1, newPrice);
            stmt.setInt(2, dishId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Map<String, Object>> getDishesWithLowStockIngredients() throws SQLException {
        List<Map<String, Object>> dishes = new ArrayList<>();
        String query = """
            SELECT DISTINCT d.*, c.category_name
            FROM Dishes d
            JOIN Categories c ON d.category_id = c.category_id
            JOIN DishIngredients di ON d.dish_id = di.dish_id
            JOIN Ingredients i ON di.ingredient_id = i.ingredient_id
            WHERE d.is_deleted = FALSE 
            AND i.quantity_in_stock <= i.minimum_stock_level
            ORDER BY d.name
        """;
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Map<String, Object> dish = new HashMap<>();
                dish.put("dishId", rs.getInt("dish_id"));
                dish.put("name", rs.getString("name"));
                dish.put("categoryName", rs.getString("category_name"));
                dish.put("sellingPrice", rs.getDouble("selling_price"));
                dish.put("isAvailable", rs.getBoolean("is_available"));
                dishes.add(dish);
            }
        }
        return dishes;
    }

    public List<Map<String, Object>> getDishesByCategory(int categoryId) throws SQLException {
        List<Map<String, Object>> dishes = new ArrayList<>();
        String query = """
            SELECT d.*, c.category_name
            FROM Dishes d
            JOIN Categories c ON d.category_id = c.category_id
            WHERE d.category_id = ? AND d.is_deleted = FALSE
            ORDER BY d.name
        """;
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> dish = new HashMap<>();
                    dish.put("dishId", rs.getInt("dish_id"));
                    dish.put("name", rs.getString("name"));
                    dish.put("categoryName", rs.getString("category_name"));
                    dish.put("sellingPrice", rs.getDouble("selling_price"));
                    dish.put("isAvailable", rs.getBoolean("is_available"));
                    dishes.add(dish);
                }
            }
        }
        return dishes;
    }

    public int getLastInsertedId() throws SQLException {
        String query = "SELECT LAST_INSERT_ID() as id";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    public List<Map<String, Object>> getDishesByIngredient(int ingredientId) throws SQLException {
        List<Map<String, Object>> dishes = new ArrayList<>();
        String query = """
            SELECT di.*, d.name, d.selling_price, d.is_available
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
                    dish.put("name", rs.getString("name"));
                    dish.put("sellingPrice", rs.getDouble("selling_price"));
                    dish.put("isAvailable", rs.getBoolean("is_available"));
                    dishes.add(dish);
                }
            }
        }
        return dishes;
    }
} 