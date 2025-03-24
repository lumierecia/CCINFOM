package controller;

import dao.DishDAO;
import model.Dish;
import model.DishIngredient;

import java.sql.SQLException;
import java.util.List;

public class DishController {
    private final DishDAO dishDAO;

    public DishController() throws SQLException {
        this.dishDAO = new DishDAO();
    }

    public boolean addDish(Dish dish, List<DishIngredient> ingredients) throws SQLException {
        if (!validateDish(dish)) {
            return false;
        }

        int dishId = dishDAO.createDish(dish);
        if (dishId > 0 && ingredients != null && !ingredients.isEmpty()) {
            return dishDAO.addDishIngredients(dishId, ingredients);
        }
        return dishId > 0;
    }

    public Dish getDishById(int dishId) throws SQLException {
        return dishDAO.getDishById(dishId);
    }

    public List<Dish> getAllDishes() throws SQLException {
        return dishDAO.getAllDishes();
    }

    public List<Dish> getDishesByCategory(String categoryName) throws SQLException {
        return dishDAO.getDishesByCategory(categoryName);
    }

    public boolean updateDish(Dish dish) throws SQLException {
        if (!validateDish(dish)) {
            return false;
        }
        return dishDAO.updateDish(dish);
    }

    public boolean deleteDish(int dishId) throws SQLException {
        return dishDAO.deleteDish(dishId);
    }

    public List<DishIngredient> getDishIngredients(int dishId) throws SQLException {
        return dishDAO.getDishIngredients(dishId);
    }

    private boolean validateDish(Dish dish) {
        if (dish == null) {
            return false;
        }
        if (dish.getName() == null || dish.getName().trim().isEmpty()) {
            return false;
        }
        if (dish.getCategoryId() <= 0) {
            return false;
        }
        if (dish.getSellingPrice() <= 0) {
            return false;
        }
        return true;
    }
} 