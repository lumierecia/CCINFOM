package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private int customerId;
    private Timestamp orderDateTime;
    private String orderType;
    private String orderStatus;
    private String paymentStatus;
    private double totalAmount;
    private String paymentMethod;
    private List<OrderItem> items;
    private List<Integer> assignedEmployees;

    public Order() {
        this.items = new ArrayList<>();
        this.assignedEmployees = new ArrayList<>();
        this.totalAmount = 0.0;
    }

    public Order(int orderId, int customerId, Timestamp orderDateTime, String orderType, 
                String orderStatus, String paymentStatus, double totalAmount, String paymentMethod) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDateTime = orderDateTime;
        this.orderType = orderType;
        this.orderStatus = orderStatus;
        this.paymentStatus = paymentStatus;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.items = new ArrayList<>();
        this.assignedEmployees = new ArrayList<>();
    }

    // Getters and setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Timestamp getOrderDateTime() {
        return orderDateTime;
    }

    public void setOrderDateTime(Timestamp orderDateTime) {
        this.orderDateTime = orderDateTime;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
    }

    public List<Integer> getAssignedEmployees() {
        return assignedEmployees;
    }

    public void setAssignedEmployees(List<Integer> assignedEmployees) {
        this.assignedEmployees = assignedEmployees;
    }

    public void addAssignedEmployee(int employeeId) {
        assignedEmployees.add(employeeId);
    }

    public void removeAssignedEmployee(int employeeId) {
        assignedEmployees.remove(Integer.valueOf(employeeId));
    }
} 