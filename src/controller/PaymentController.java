package controller;

import dao.PaymentDAO;
import dao.OrderDAO;
import model.Payment;
import model.Order;

import java.sql.SQLException;
import java.util.List;

public class PaymentController {
    private final PaymentDAO paymentDAO;
    private final OrderDAO orderDAO;

    public PaymentController() throws SQLException {
        this.paymentDAO = new PaymentDAO();
        this.orderDAO = new OrderDAO();
    }

    public int processPayment(int orderId, double amount, String paymentMethod, String transactionId, String notes) throws SQLException {
        // Validate order exists and is not already paid
        Order order = orderDAO.getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }
        if ("Paid".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("Order is already paid");
        }

        // Create payment record
        Payment payment = new Payment(orderId, amount, paymentMethod, "Completed");
        payment.setTransactionId(transactionId);
        payment.setNotes(notes);

        int paymentId = paymentDAO.createPayment(payment);

        // Update order payment status
        orderDAO.updatePaymentStatus(orderId, paymentMethod, "Paid");

        return paymentId;
    }

    public Payment getPayment(int paymentId) throws SQLException {
        return paymentDAO.getPayment(paymentId);
    }

    public List<Payment> getPaymentsByOrder(int orderId) throws SQLException {
        return paymentDAO.getPaymentsByOrder(orderId);
    }

    public List<Payment> getPaymentsByDateRange(java.sql.Date startDate, java.sql.Date endDate) throws SQLException {
        return paymentDAO.getPaymentsByDateRange(startDate, endDate);
    }

    public boolean updatePaymentStatus(int paymentId, String status) throws SQLException {
        return paymentDAO.updatePaymentStatus(paymentId, status);
    }

    public String generatePaymentReport(java.sql.Date startDate, java.sql.Date endDate) throws SQLException {
        List<Payment> payments = getPaymentsByDateRange(startDate, endDate);
        
        double totalAmount = payments.stream()
            .mapToDouble(Payment::getAmount)
            .sum();
        
        long cashPayments = payments.stream()
            .filter(p -> "Cash".equals(p.getPaymentMethod()))
            .count();
            
        long cardPayments = payments.stream()
            .filter(p -> "Credit Card".equals(p.getPaymentMethod()))
            .count();

        StringBuilder report = new StringBuilder();
        report.append("Payment Report for Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");
        report.append("Summary:\n");
        report.append(String.format("Total Number of Payments: %d\n", payments.size()));
        report.append(String.format("Total Amount Processed: $%.2f\n", totalAmount));
        report.append(String.format("Cash Payments: %d\n", cashPayments));
        report.append(String.format("Credit Card Payments: %d\n", cardPayments));
        
        report.append("\nDetailed Payment List:\n");
        for (Payment payment : payments) {
            report.append(String.format("\nPayment #%d\n", payment.getPaymentId()));
            report.append(String.format("Order #%d\n", payment.getOrderId()));
            report.append(String.format("Amount: $%.2f\n", payment.getAmount()));
            report.append(String.format("Method: %s\n", payment.getPaymentMethod()));
            report.append(String.format("Date: %s\n", payment.getPaymentDate()));
            if (payment.getTransactionId() != null && !payment.getTransactionId().isEmpty()) {
                report.append(String.format("Transaction ID: %s\n", payment.getTransactionId()));
            }
            if (payment.getNotes() != null && !payment.getNotes().isEmpty()) {
                report.append(String.format("Notes: %s\n", payment.getNotes()));
            }
        }

        return report.toString();
    }
} 