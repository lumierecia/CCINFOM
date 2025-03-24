package model;

public class DishIngredient {
    private int dishId;
    private int ingredientId;
    private double quantityNeeded;
    private int unitId;
    private String ingredientName; // For display purposes
    private String unitName; // For display purposes

    public DishIngredient(int dishId, int ingredientId, double quantityNeeded, int unitId) {
        this.dishId = dishId;
        this.ingredientId = ingredientId;
        this.quantityNeeded = quantityNeeded;
        this.unitId = unitId;
    }

    // Getters and Setters
    public int getDishId() { return dishId; }
    public void setDishId(int dishId) { this.dishId = dishId; }
    
    public int getIngredientId() { return ingredientId; }
    public void setIngredientId(int ingredientId) { this.ingredientId = ingredientId; }
    
    public double getQuantityNeeded() { return quantityNeeded; }
    public void setQuantityNeeded(double quantityNeeded) { this.quantityNeeded = quantityNeeded; }
    
    public int getUnitId() { return unitId; }
    public void setUnitId(int unitId) { this.unitId = unitId; }
    
    public String getIngredientName() { return ingredientName; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }
    
    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    @Override
    public String toString() {
        return String.format("%.2f %s %s", quantityNeeded, unitName, ingredientName);
    }
} 