package model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Ingredient {
    private int ingredientId;
    private String name;
    private int unitId;
    private String unitName;  // For display purposes
    private double quantityInStock;
    private double minimumStockLevel;
    private double costPerUnit;
    private Date lastRestockDate;
    private int lastRestockedBy;
    private boolean isDeleted;
    private Map<Integer, SupplierPrice> supplierPrices;

    public Ingredient() {
        // Default constructor
    }

    public Ingredient(int ingredientId, String name, int unitId, String unitName, double quantityInStock,
                     double minimumStockLevel, double costPerUnit, Date lastRestockDate,
                     int lastRestockedBy) {
        this.ingredientId = ingredientId;
        this.name = name;
        this.unitId = unitId;
        this.unitName = unitName;
        this.quantityInStock = quantityInStock;
        this.minimumStockLevel = minimumStockLevel;
        this.costPerUnit = costPerUnit;
        this.lastRestockDate = lastRestockDate;
        this.lastRestockedBy = lastRestockedBy;
        this.supplierPrices = new HashMap<>();
    }

    // Inner class to store supplier pricing information
    public static class SupplierPrice {
        private double unitPrice;
        private int leadTimeDays;
        private double minimumOrderQuantity;
        private boolean isPrimarySupplier;

        public SupplierPrice(double unitPrice, int leadTimeDays, double minimumOrderQuantity, boolean isPrimarySupplier) {
            this.unitPrice = unitPrice;
            this.leadTimeDays = leadTimeDays;
            this.minimumOrderQuantity = minimumOrderQuantity;
            this.isPrimarySupplier = isPrimarySupplier;
        }

        // Getters
        public double getUnitPrice() { return unitPrice; }
        public int getLeadTimeDays() { return leadTimeDays; }
        public double getMinimumOrderQuantity() { return minimumOrderQuantity; }
        public boolean isPrimarySupplier() { return isPrimarySupplier; }
    }

    // Methods to manage supplier prices
    public void addSupplierPrice(int supplierId, SupplierPrice price) {
        supplierPrices.put(supplierId, price);
    }

    public SupplierPrice getSupplierPrice(int supplierId) {
        return supplierPrices.get(supplierId);
    }

    public Map<Integer, SupplierPrice> getAllSupplierPrices() {
        return new HashMap<>(supplierPrices);
    }

    // Business logic methods
    public boolean isLowStock() {
        return quantityInStock <= minimumStockLevel;
    }

    public double getReorderQuantity() {
        if (!isLowStock()) return 0;
        // Calculate reorder quantity based on minimum stock level and current stock
        return minimumStockLevel * 2 - quantityInStock;
    }

    // Getters
    public int getIngredientId() { return ingredientId; }
    public String getName() { return name; }
    public int getUnitId() { return unitId; }
    public String getUnitName() { return unitName; }
    public double getQuantityInStock() { return quantityInStock; }
    public double getMinimumStockLevel() { return minimumStockLevel; }
    public double getCostPerUnit() { return costPerUnit; }
    public Date getLastRestockDate() { return lastRestockDate; }
    public int getLastRestockedBy() { return lastRestockedBy; }
    public Map<Integer, SupplierPrice> getSupplierPrices() { return supplierPrices; }
    public boolean isDeleted() { return isDeleted; }

    // Setters
    public void setIngredientId(int ingredientId) { this.ingredientId = ingredientId; }
    public void setName(String name) { this.name = name; }
    public void setUnitId(int unitId) { this.unitId = unitId; }
    public void setUnitName(String unitName) { this.unitName = unitName; }
    public void setQuantityInStock(double quantityInStock) { this.quantityInStock = quantityInStock; }
    public void setMinimumStockLevel(double minimumStockLevel) { this.minimumStockLevel = minimumStockLevel; }
    public void setCostPerUnit(double costPerUnit) { this.costPerUnit = costPerUnit; }
    public void setLastRestockDate(Date lastRestockDate) { this.lastRestockDate = lastRestockDate; }
    public void setLastRestockedBy(int lastRestockedBy) { this.lastRestockedBy = lastRestockedBy; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    public void setSupplierPrices(Map<Integer, SupplierPrice> supplierPrices) { this.supplierPrices = supplierPrices; }

    @Override
    public String toString() {
        return name + " (" + quantityInStock + " " + unitName + ")";
    }
} 