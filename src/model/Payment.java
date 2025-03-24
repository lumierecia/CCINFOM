package model;

import java.sql.Timestamp;

public class Payment {
    private int paymentId;
    private int orderId;
    private double amount;
    private String paymentMethod;
    private String status;
    private Timestamp paymentDate;
    private String transactionId; // For credit card payments
    private String notes;

    public Payment() {
    }

    public Payment(int orderId, double amount, String paymentMethod, String status) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paymentDate = new Timestamp(System.currentTimeMillis());
    }

    // Getters
    public int getPaymentId() { return paymentId; }
    public int getOrderId() { return orderId; }
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public Timestamp getPaymentDate() { return paymentDate; }
    public String getTransactionId() { return transactionId; }
    public String getNotes() { return notes; }

    // Setters
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setStatus(String status) { this.status = status; }
    public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return String.format("Payment #%d - Order #%d - $%.2f - %s", 
            paymentId, orderId, amount, paymentMethod);
    }
} 