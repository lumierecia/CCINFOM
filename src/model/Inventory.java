package model;

public class Inventory {
    private int productId;
    private String productName;
    private int categoryId;
    private String categoryName;
    private double makePrice;
    private double sellPrice;
    private int quantity;
    private String status;
    private String recipeInstructions;
    private int lastRestockedBy;

    public Inventory() {
    }

    public Inventory(int productId, String productName, String categoryName, int quantity, 
                    double makePrice, double sellPrice) {
        this.productId = productId;
        this.productName = productName;
        this.categoryName = categoryName;
        this.quantity = quantity;
        this.makePrice = makePrice;
        this.sellPrice = sellPrice;
        this.status = "Available";
    }

    // Getters and setters
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getMakePrice() {
        return makePrice;
    }

    public void setMakePrice(double makePrice) {
        this.makePrice = makePrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecipeInstructions() {
        return recipeInstructions;
    }

    public void setRecipeInstructions(String recipeInstructions) {
        this.recipeInstructions = recipeInstructions;
    }

    public int getLastRestockedBy() {
        return lastRestockedBy;
    }

    public void setLastRestockedBy(int lastRestockedBy) {
        this.lastRestockedBy = lastRestockedBy;
    }

    @Override
    public String toString() {
        return productName;
    }
} 