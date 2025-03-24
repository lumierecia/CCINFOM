package model;

import java.util.ArrayList;
import java.util.List;

public class Dish {
    private int dishId;
    private String name;
    private int categoryId;
    private String categoryName;
    private double sellingPrice;
    private String recipeInstructions;
    private boolean isAvailable;
    private boolean isDeleted;
    private List<DishIngredient> ingredients;

    public Dish() {
        this.ingredients = new ArrayList<>();
        this.isAvailable = true;
        this.isDeleted = false;
    }

    public Dish(int dishId, String name, int categoryId, double sellingPrice, String recipeInstructions) {
        this.dishId = dishId;
        this.name = name;
        this.categoryId = categoryId;
        this.sellingPrice = sellingPrice;
        this.recipeInstructions = recipeInstructions;
        this.isAvailable = true;
        this.isDeleted = false;
        this.ingredients = new ArrayList<>();
    }

    public Dish(int dishId, String name, int categoryId, double sellingPrice, 
                String recipeInstructions, boolean isAvailable) {
        this.dishId = dishId;
        this.name = name;
        this.categoryId = categoryId;
        this.sellingPrice = sellingPrice;
        this.recipeInstructions = recipeInstructions;
        this.isAvailable = isAvailable;
        this.isDeleted = false;
        this.ingredients = new ArrayList<>();
    }

    // Getters and Setters
    public int getDishId() { return dishId; }
    public void setDishId(int dishId) { this.dishId = dishId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public double getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(double sellingPrice) { this.sellingPrice = sellingPrice; }
    
    public String getRecipeInstructions() { return recipeInstructions; }
    public void setRecipeInstructions(String recipeInstructions) { this.recipeInstructions = recipeInstructions; }
    
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    
    public List<DishIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<DishIngredient> ingredients) { this.ingredients = ingredients; }
    
    public void addIngredient(DishIngredient ingredient) {
        this.ingredients.add(ingredient);
    }

    @Override
    public String toString() {
        return String.format("%s (%.2f) - %s", name, sellingPrice, categoryName);
    }
} 