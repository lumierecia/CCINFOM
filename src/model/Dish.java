package model;

public class Dish {
    private int dishId;
    private String name;
    private String categoryName;
    private double sellingPrice;
    private String recipeInstructions;
    private boolean isAvailable;

    public Dish() {
        // Default constructor
    }

    public Dish(String name, String categoryName, double sellingPrice, String recipeInstructions, boolean isAvailable) {
        this.name = name;
        this.categoryName = categoryName;
        this.sellingPrice = sellingPrice;
        this.recipeInstructions = recipeInstructions;
        this.isAvailable = isAvailable;
    }

    // Getters and setters
    public int getDishId() {
        return dishId;
    }

    public void setDishId(int dishId) {
        this.dishId = dishId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public String getRecipeInstructions() {
        return recipeInstructions;
    }

    public void setRecipeInstructions(String recipeInstructions) {
        this.recipeInstructions = recipeInstructions;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public String toString() {
        return String.format("%s - $%.2f", name, sellingPrice);
    }
} 