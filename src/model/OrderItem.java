package model;

public class OrderItem {
    private int orderId;
    private int dishId;
    private String dishName;
    private int quantity;
    private double priceAtTime;

    // No-args constructor
    public OrderItem() {
        this.orderId = 0;
        this.dishId = 0;
        this.quantity = 0;
        this.priceAtTime = 0.0;
    }

    public OrderItem(int orderId, int dishId, String dishName, int quantity, double priceAtTime) {
        this.orderId = orderId;
        this.dishId = dishId;
        this.dishName = dishName;
        this.quantity = quantity;
        this.priceAtTime = priceAtTime;
    }

    public OrderItem(int orderId, int dishId, int quantity, double priceAtTime) {
        this.orderId = orderId;
        this.dishId = dishId;
        this.quantity = quantity;
        this.priceAtTime = priceAtTime;
    }

    // Getters and setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getDishId() {
        return dishId;
    }

    public void setDishId(int dishId) {
        this.dishId = dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPriceAtTime() {
        return priceAtTime;
    }

    public void setPriceAtTime(double priceAtTime) {
        this.priceAtTime = priceAtTime;
    }

    public double getSubtotal() {
        return quantity * priceAtTime;
    }
} 