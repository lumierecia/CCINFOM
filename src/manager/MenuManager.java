package manager;

import dao.CategoryDAO;
import dao.DishDAO;
import dao.DishIngredientDAO;
import dao.IngredientDAO;
import model.Category;
import model.Dish;
import model.Ingredient;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuManager {
    private final CategoryDAO categoryDAO;
    private final DishDAO dishDAO;
    private final DishIngredientDAO dishIngredientDAO;
    private final IngredientDAO ingredientDAO;

    public MenuManager() {
        this.categoryDAO = new CategoryDAO();
        this.dishDAO = new DishDAO();
        this.dishIngredientDAO = new DishIngredientDAO();
        this.ingredientDAO = new IngredientDAO();
    }

    // Category Management
    public boolean createCategory(String name, String description) throws SQLException {
        return categoryDAO.createCategory(name, description);
    }

    public boolean updateCategory(int categoryId, String name, String description) throws SQLException {
        return categoryDAO.updateCategory(categoryId, name, description);
    }

    public boolean deleteCategory(int categoryId) throws SQLException {
        return categoryDAO.deleteCategory(categoryId);
    }

    public Category getCategoryById(int categoryId) throws SQLException {
        return categoryDAO.getCategoryById(categoryId);
    }

    public List<Category> getAllCategories() throws SQLException {
        return categoryDAO.getAllCategories();
    }

    // Dish Management
    public boolean createDish(String name, int categoryId, double sellingPrice, 
                            String recipeInstructions, List<Map<String, Object>> ingredients) throws SQLException {
        // Start transaction
        try {
            // Create the dish
            boolean dishCreated = dishDAO.createDish(name, categoryId, sellingPrice, recipeInstructions);
            if (!dishCreated) {
                return false;
            }

            // Get the created dish ID
            Dish createdDish = dishDAO.getDishById(dishDAO.getLastInsertedId());
            if (createdDish == null) {
                return false;
            }

            // Add ingredients
            for (Map<String, Object> ingredient : ingredients) {
                int ingredientId = (int) ingredient.get("ingredientId");
                double quantity = (double) ingredient.get("quantity");
                String unit = (String) ingredient.get("unit");
                
                boolean ingredientAdded = dishIngredientDAO.addIngredientToDish(
                    createdDish.getDishId(), ingredientId, quantity, unit);
                if (!ingredientAdded) {
                    // Rollback by deleting the dish
                    dishDAO.deleteDish(createdDish.getDishId());
                    return false;
                }
            }

            return true;
        } catch (SQLException e) {
            throw new SQLException("Failed to create dish: " + e.getMessage());
        }
    }

    public boolean updateDish(int dishId, String name, int categoryId, double sellingPrice,
                            String recipeInstructions, boolean isAvailable, 
                            List<Map<String, Object>> ingredients) throws SQLException {
        try {
            // Update dish details
            boolean dishUpdated = dishDAO.updateDish(dishId, name, categoryId, sellingPrice, 
                                                   recipeInstructions, isAvailable);
            if (!dishUpdated) {
                return false;
            }

            // Remove existing ingredients
            dishIngredientDAO.removeAllIngredientsFromDish(dishId);

            // Add new ingredients
            for (Map<String, Object> ingredient : ingredients) {
                int ingredientId = (int) ingredient.get("ingredientId");
                double quantity = (double) ingredient.get("quantity");
                String unit = (String) ingredient.get("unit");
                
                boolean ingredientAdded = dishIngredientDAO.addIngredientToDish(
                    dishId, ingredientId, quantity, unit);
                if (!ingredientAdded) {
                    return false;
                }
            }

            return true;
        } catch (SQLException e) {
            throw new SQLException("Failed to update dish: " + e.getMessage());
        }
    }

    public boolean deleteDish(int dishId) throws SQLException {
        try {
            // Remove all ingredients first
            dishIngredientDAO.removeAllIngredientsFromDish(dishId);
            // Then delete the dish
            return dishDAO.deleteDish(dishId);
        } catch (SQLException e) {
            throw new SQLException("Failed to delete dish: " + e.getMessage());
        }
    }

    public Dish getDishById(int dishId) throws SQLException {
        return dishDAO.getDishById(dishId);
    }

    public List<Dish> getAllDishes() throws SQLException {
        return dishDAO.getAllDishes();
    }

    public List<Dish> getAvailableDishes() throws SQLException {
        return dishDAO.getAvailableDishes();
    }

    public boolean updateDishAvailability(int dishId, boolean isAvailable) throws SQLException {
        return dishDAO.updateDishAvailability(dishId, isAvailable);
    }

    public boolean updateDishPrice(int dishId, double newPrice) throws SQLException {
        return dishDAO.updateDishPrice(dishId, newPrice);
    }

    // Menu Analysis
    public List<Map<String, Object>> getDishesWithLowStockIngredients() throws SQLException {
        return dishDAO.getDishesWithLowStockIngredients();
    }

    public List<Map<String, Object>> getDishesByCategory(int categoryId) throws SQLException {
        return dishDAO.getDishesByCategory(categoryId);
    }

    public List<Map<String, Object>> getDishIngredients(int dishId) throws SQLException {
        return dishIngredientDAO.getDishIngredients(dishId);
    }

    public List<Map<String, Object>> getIngredientUsage(int ingredientId) throws SQLException {
        return dishIngredientDAO.getIngredientUsage(ingredientId);
    }

    // Menu Optimization
    public List<Map<String, Object>> getMostProfitableDishes() throws SQLException {
        List<Map<String, Object>> dishes = new ArrayList<>();
        String query = """
            SELECT d.*, 
                   (d.selling_price - COALESCE(SUM(di.quantity * i.unit_price), 0)) as profit_margin
            FROM Dishes d
            LEFT JOIN DishIngredients di ON d.dish_id = di.dish_id
            LEFT JOIN Ingredients i ON di.ingredient_id = i.ingredient_id
            WHERE d.is_deleted = FALSE
            GROUP BY d.dish_id
            ORDER BY profit_margin DESC
            LIMIT 10
        """;
        // Implementation would go here
        return dishes;
    }

    public List<Map<String, Object>> getLeastProfitableDishes() throws SQLException {
        List<Map<String, Object>> dishes = new ArrayList<>();
        String query = """
            SELECT d.*, 
                   (d.selling_price - COALESCE(SUM(di.quantity * i.unit_price), 0)) as profit_margin
            FROM Dishes d
            LEFT JOIN DishIngredients di ON d.dish_id = di.dish_id
            LEFT JOIN Ingredients i ON di.ingredient_id = i.ingredient_id
            WHERE d.is_deleted = FALSE
            GROUP BY d.dish_id
            ORDER BY profit_margin ASC
            LIMIT 10
        """;
        // Implementation would go here
        return dishes;
    }

    public List<Map<String, Object>> getDishesWithHighIngredientCost() throws SQLException {
        List<Map<String, Object>> dishes = new ArrayList<>();
        String query = """
            SELECT d.*, 
                   COALESCE(SUM(di.quantity * i.unit_price), 0) as total_ingredient_cost
            FROM Dishes d
            LEFT JOIN DishIngredients di ON d.dish_id = di.dish_id
            LEFT JOIN Ingredients i ON di.ingredient_id = i.ingredient_id
            WHERE d.is_deleted = FALSE
            GROUP BY d.dish_id
            HAVING total_ingredient_cost > d.selling_price * 0.7
            ORDER BY total_ingredient_cost DESC
        """;
        // Implementation would go here
        return dishes;
    }
} 