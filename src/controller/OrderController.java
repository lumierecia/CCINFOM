package controller;

import dao.OrderDAO;
import model.Order;
import model.OrderItem;

import java.sql.SQLException;
import java.util.List;

public class OrderController {
    private OrderDAO orderDAO;

    public OrderController() throws SQLException {
        this.orderDAO = new OrderDAO();
    }

    // Create a new order
    public int createOrder(Order order) throws SQLException {
        // Validate order
        if (!isValidOrder(order)) {
            throw new IllegalArgumentException("Invalid order data");
        }

        // Calculate total amount
        double totalAmount = calculateTotalAmount(order.getItems());
        order.setTotalAmount(totalAmount);

        // Create order in database
        int orderId = orderDAO.createOrder(order);
        if (orderId == -1) {
            throw new SQLException("Failed to create order");
        }

        // Add order items
        orderDAO.addOrderItems(orderId, order.getItems());

        // Assign employees
        orderDAO.assignEmployeesToOrder(orderId, order.getAssignedEmployees());

        return orderId;
    }

    // Get order by ID
    public Order getOrderById(int orderId) throws SQLException {
        return orderDAO.getOrderById(orderId);
    }

    // Get all active orders
    public List<Order> getAllActiveOrders() throws SQLException {
        return orderDAO.getAllActiveOrders();
    }

    // Update order status
    public void updateOrderStatus(int orderId, String newStatus) throws SQLException {
        // Validate status
        if (!isValidOrderStatus(newStatus)) {
            throw new IllegalArgumentException("Invalid order status");
        }

        orderDAO.updateOrderStatus(orderId, newStatus);
    }

    // Update payment status
    public void updatePaymentStatus(int orderId, String paymentMethod, String paymentStatus) throws SQLException {
        // Validate payment data
        if (!isValidPaymentMethod(paymentMethod) || !isValidPaymentStatus(paymentStatus)) {
            throw new IllegalArgumentException("Invalid payment data");
        }

        orderDAO.updatePaymentStatus(orderId, paymentMethod, paymentStatus);
    }

    // Delete order (soft delete)
    public void deleteOrder(int orderId) throws SQLException {
        orderDAO.deleteOrder(orderId);
    }

    // Helper methods for validation
    private boolean isValidOrder(Order order) {
        return order != null &&
               order.getCustomerId() > 0 &&
               order.getOrderType() != null &&
               order.getOrderStatus() != null &&
               order.getItems() != null &&
               !order.getItems().isEmpty() &&
               order.getAssignedEmployees() != null &&
               !order.getAssignedEmployees().isEmpty();
    }

    private boolean isValidOrderStatus(String status) {
        return status != null && (
            status.equals("In Progress") ||
            status.equals("Ready") ||
            status.equals("Served") ||
            status.equals("Completed") ||
            status.equals("Cancelled")
        );
    }

    private boolean isValidPaymentMethod(String method) {
        return method != null && (
            method.equals("Cash") ||
            method.equals("Credit Card")
        );
    }

    private boolean isValidPaymentStatus(String status) {
        return status != null && (
            status.equals("Pending") ||
            status.equals("Paid")
        );
    }

    private double calculateTotalAmount(List<OrderItem> items) {
        return items.stream()
                   .mapToDouble(OrderItem::getSubtotal)
                   .sum();
    }
} 