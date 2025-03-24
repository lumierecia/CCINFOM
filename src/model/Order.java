package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    private int orderId;
    private int customerId;
    private String customerName;
    private Timestamp orderDateTime;
    private String orderType;
    private String orderStatus;
    private String paymentStatus;
    private double totalAmount;
    private String paymentMethod;
    private List<OrderItem> items;
    private List<Integer> assignedEmployees;
    private int tableId;
    private boolean isDeleted;
    private Table table;

    public Order() {
        this.items = new ArrayList<>();
        this.assignedEmployees = new ArrayList<>();
        this.totalAmount = 0.0;
    }

    public Order(int orderId, int customerId, String customerName, String orderType, 
                String orderStatus, Date orderDateTime, double totalAmount, 
                String paymentMethod, String paymentStatus) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.orderType = orderType;
        this.orderStatus = orderStatus;
        this.orderDateTime = new Timestamp(orderDateTime.getTime());
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.items = new ArrayList<>();
        this.assignedEmployees = new ArrayList<>();
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

    public Order(int orderId, int customerId, String orderType, String orderStatus, 
                Timestamp orderDatetime, double totalAmount, String paymentMethod, 
                String paymentStatus, int tableId, boolean isDeleted) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderType = orderType;
        this.orderStatus = orderStatus;
        this.orderDateTime = orderDatetime;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.tableId = tableId;
        this.isDeleted = isDeleted;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", orderDateTime=" + orderDateTime +
                ", orderType='" + orderType + '\'' +
                ", orderStatus='" + orderStatus + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", totalAmount=" + totalAmount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", tableId=" + tableId +
                ", isDeleted=" + isDeleted +
                '}';
    }
} 