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
    private List<OrderItem> orderItems;
    private List<Integer> assignedEmployees;

    public Order() {
        this.orderItems = new ArrayList<>();
        this.assignedEmployees = new ArrayList<>();
    }

    public Order(int orderId, int customerId, Timestamp orderDateTime, String orderType, String orderStatus) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDateTime = orderDateTime;
        this.orderType = orderType;
        this.orderStatus = orderStatus;
        this.orderItems = new ArrayList<>();
        this.assignedEmployees = new ArrayList<>();
    }

    // Getters
    public int getOrderId() {
        return orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public Timestamp getOrderDateTime() {
        return orderDateTime;
    }

    public String getOrderType() {
        return orderType;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public List<Integer> getAssignedEmployees() {
        return assignedEmployees;
    }

    // Setters
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setOrderDateTime(Timestamp orderDateTime) {
        this.orderDateTime = orderDateTime;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void setAssignedEmployees(List<Integer> assignedEmployees) {
        this.assignedEmployees = assignedEmployees;
    }

    // Helper methods
    public void addOrderItem(OrderItem item) {
        if (this.orderItems == null) {
            this.orderItems = new ArrayList<>();
        }
        this.orderItems.add(item);
    }

    public void addAssignedEmployee(int employeeId) {
        if (this.assignedEmployees == null) {
            this.assignedEmployees = new ArrayList<>();
        }
        this.assignedEmployees.add(employeeId);
    }
} 