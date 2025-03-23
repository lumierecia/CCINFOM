package model;

public class OrderItem {
    private int orderId;
    private int productId;
    private String productName;
    private int quantity;
    private double priceAtTime;

    // No-args constructor
    public OrderItem() {
        this.orderId = 0;
        this.productId = 0;
        this.quantity = 0;
        this.priceAtTime = 0.0;
    }

    public OrderItem(int orderId, int productId, String productName, int quantity, double priceAtTime) {
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.priceAtTime = priceAtTime;
    }

    public OrderItem(int orderId, int productId, int quantity, double priceAtTime) {
        this.orderId = orderId;
        this.productId = productId;
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