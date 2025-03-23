package model;

import java.util.Date;

public class IngredientBatch {
    private int batchId;
    private int ingredientId;
    private String ingredientName;
    private int supplierId;
    private String supplierName;
    private double quantity;
    private Date purchaseDate;
    private Date expiryDate;
    private double purchasePrice;
    private double remainingQuantity;
    private String status;

    public IngredientBatch() {
        // Default constructor
    }

    // Getters and setters
    public int getBatchId() {
        return batchId;
    }

    public void setBatchId(int batchId) {
        this.batchId = batchId;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public double getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(double remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Business logic
    public boolean isExpired() {
        return expiryDate != null && expiryDate.before(new Date());
    }

    public boolean isLowStock() {
        return remainingQuantity < (quantity * 0.2);  // Less than 20% remaining
    }

    public double getUsedQuantity() {
        return quantity - remainingQuantity;
    }

    public double getUsagePercentage() {
        return (getUsedQuantity() / quantity) * 100;
    }

    @Override
    public String toString() {
        return String.format("%s (Batch #%d) - %.2f %s remaining", 
            ingredientName != null ? ingredientName : "Unknown Ingredient",
            batchId,
            remainingQuantity,
            status);
    }
} 