package controller;

import manager.MenuManager;
import model.Category;
import model.Dish;
import model.Ingredient;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class MenuController {
    private final MenuManager menuManager;

    public MenuController() {
        this.menuManager = new MenuManager();
    }

    // Category Management
    public boolean createCategory(String name, String description) throws SQLException {
        return menuManager.createCategory(name, description);
    }

    public boolean updateCategory(int categoryId, String name, String description) throws SQLException {
        return menuManager.updateCategory(categoryId, name, description);
    }

    public boolean deleteCategory(int categoryId) throws SQLException {
        return menuManager.deleteCategory(categoryId);
    }

    public Category getCategoryById(int categoryId) throws SQLException {
        return menuManager.getCategoryById(categoryId);
    }

    public List<Category> getAllCategories() throws SQLException {
        return menuManager.getAllCategories();
    }

    // Dish Management
    public boolean createDish(String name, int categoryId, double sellingPrice, 
                            String recipeInstructions, List<Map<String, Object>> ingredients) throws SQLException {
        return menuManager.createDish(name, categoryId, sellingPrice, recipeInstructions, ingredients);
    }

    public boolean updateDish(int dishId, String name, int categoryId, double sellingPrice,
                            String recipeInstructions, boolean isAvailable, 
                            List<Map<String, Object>> ingredients) throws SQLException {
        return menuManager.updateDish(dishId, name, categoryId, sellingPrice, recipeInstructions, isAvailable, ingredients);
    }

    public boolean deleteDish(int dishId) throws SQLException {
        return menuManager.deleteDish(dishId);
    }

    public Dish getDishById(int dishId) throws SQLException {
        return menuManager.getDishById(dishId);
    }

    public List<Dish> getAllDishes() throws SQLException {
        return menuManager.getAllDishes();
    }

    public List<Dish> getAvailableDishes() throws SQLException {
        return menuManager.getAvailableDishes();
    }

    public boolean updateDishAvailability(int dishId, boolean isAvailable) throws SQLException {
        return menuManager.updateDishAvailability(dishId, isAvailable);
    }

    public boolean updateDishPrice(int dishId, double newPrice) throws SQLException {
        return menuManager.updateDishPrice(dishId, newPrice);
    }

    // Menu Analysis
    public List<Map<String, Object>> getDishesWithLowStockIngredients() throws SQLException {
        return menuManager.getDishesWithLowStockIngredients();
    }

    public List<Map<String, Object>> getDishesByCategory(int categoryId) throws SQLException {
        return menuManager.getDishesByCategory(categoryId);
    }

    public List<Map<String, Object>> getDishIngredients(int dishId) throws SQLException {
        return menuManager.getDishIngredients(dishId);
    }

    public List<Map<String, Object>> getIngredientUsage(int ingredientId) throws SQLException {
        return menuManager.getIngredientUsage(ingredientId);
    }

    // Menu Optimization
    public List<Map<String, Object>> getMostProfitableDishes() throws SQLException {
        return menuManager.getMostProfitableDishes();
    }

    public List<Map<String, Object>> getLeastProfitableDishes() throws SQLException {
        return menuManager.getLeastProfitableDishes();
    }

    public List<Map<String, Object>> getDishesWithHighIngredientCost() throws SQLException {
        return menuManager.getDishesWithHighIngredientCost();
    }

    // Integration with Order Management
    public boolean checkDishAvailability(int dishId) throws SQLException {
        Dish dish = getDishById(dishId);
        if (dish == null || !dish.isAvailable()) {
            return false;
        }
        List<Map<String, Object>> ingredients = menuManager.getDishIngredients(dishId);
        for (Map<String, Object> ingredient : ingredients) {
            double requiredQuantity = (double) ingredient.get("quantity");
            double availableQuantity = (double) ingredient.get("unitPrice");
            if (requiredQuantity > availableQuantity) {
                return false;
            }
        }
        return true;
    }

    // Integration with Inventory Management
    public void updateIngredientStock(int ingredientId, double quantity) throws SQLException {
        // This method will be called by the InventoryManager when stock is updated
        // It will automatically update dish availability based on ingredient stock
        List<Map<String, Object>> affectedDishes = getIngredientUsage(ingredientId);
        for (Map<String, Object> dish : affectedDishes) {
            int dishId = (int) dish.get("dishId");
            updateDishAvailability(dishId, checkDishAvailability(dishId));
        }
    }

    // Integration with Customer Loyalty Program
    public List<Map<String, Object>> getRecommendedDishes(int customerId) throws SQLException {
        // This method will be called by the LoyaltyManager to get personalized dish recommendations
        // based on customer preferences and order history
        return menuManager.getMostProfitableDishes();
    }
} 