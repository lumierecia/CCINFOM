package model;

import java.util.Date;

public class IngredientBatch {
    private int batchId;
    private int ingredientId;
    private int unitId;
    private double quantity;
    private double remainingQuantity;
    private Date purchaseDate;
    private Date expiryDate;
    private double purchasePrice;
    private int supplierId;
    private String ingredientName;
    private String unitName;

    public IngredientBatch(int batchId, int ingredientId, int unitId, double quantity, 
                          double remainingQuantity, Date purchaseDate, Date expiryDate, 
                          double purchasePrice, int supplierId) {
        this.batchId = batchId;
        this.ingredientId = ingredientId;
        this.unitId = unitId;
        this.quantity = quantity;
        this.remainingQuantity = remainingQuantity;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.purchasePrice = purchasePrice;
        this.supplierId = supplierId;
    }

    // Getters
    public int getBatchId() { return batchId; }
    public int getIngredientId() { return ingredientId; }
    public int getUnitId() { return unitId; }
    public double getQuantity() { return quantity; }
    public double getRemainingQuantity() { return remainingQuantity; }
    public Date getPurchaseDate() { return purchaseDate; }
    public Date getExpiryDate() { return expiryDate; }
    public double getPurchasePrice() { return purchasePrice; }
    public int getSupplierId() { return supplierId; }
    public String getIngredientName() { return ingredientName; }
    public String getUnitName() { return unitName; }

    // Setters
    public void setBatchId(int batchId) { this.batchId = batchId; }
    public void setIngredientId(int ingredientId) { this.ingredientId = ingredientId; }
    public void setUnitId(int unitId) { this.unitId = unitId; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setRemainingQuantity(double remainingQuantity) { this.remainingQuantity = remainingQuantity; }
    public void setPurchaseDate(Date purchaseDate) { this.purchaseDate = purchaseDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public void setIngredientName(String ingredientName) { this.ingredientName = ingredientName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    @Override
    public String toString() {
        return String.format("%s - %.2f %s (Expires: %s)", 
            ingredientName, remainingQuantity, unitName, expiryDate);
    }
} 