package model;

public class OrderItem {
    private int orderId;
    private int dishId;
    private int quantity;
    private double priceAtTime;
    private String dishName;  // Additional field for display purposes

    // Constructor
    public OrderItem(int orderId, int dishId, int quantity, double priceAtTime, String dishName) {
        this.orderId = orderId;
        this.dishId = dishId;
        this.quantity = quantity;
        this.priceAtTime = priceAtTime;
        this.dishName = dishName;
    }

    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getDishId() { return dishId; }
    public void setDishId(int dishId) { this.dishId = dishId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPriceAtTime() { return priceAtTime; }
    public void setPriceAtTime(double priceAtTime) { this.priceAtTime = priceAtTime; }

    public String getDishName() { return dishName; }
    public void setDishName(String dishName) { this.dishName = dishName; }

    public double getSubtotal() {
        return quantity * priceAtTime;
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderId=" + orderId +
                ", dishId=" + dishId +
                ", dishName='" + dishName + '\'' +
                ", quantity=" + quantity +
                ", priceAtTime=" + priceAtTime +
                ", subtotal=" + getSubtotal() +
                '}';
    }
} 