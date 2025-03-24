package dao;

import model.Payment;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    private final Connection connection;

    public PaymentDAO() throws SQLException {
        this.connection = DatabaseConnection.getConnection();
    }

    public int createPayment(Payment payment) throws SQLException {
        String query = "INSERT INTO Payments (order_id, amount, payment_method, status, payment_date, transaction_id, notes) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, payment.getOrderId());
            pstmt.setDouble(2, payment.getAmount());
            pstmt.setString(3, payment.getPaymentMethod());
            pstmt.setString(4, payment.getStatus());
            pstmt.setTimestamp(5, payment.getPaymentDate());
            pstmt.setString(6, payment.getTransactionId());
            pstmt.setString(7, payment.getNotes());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("Failed to create payment, no ID obtained.");
            }
        }
    }

    public Payment getPayment(int paymentId) throws SQLException {
        String query = "SELECT * FROM Payments WHERE payment_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, paymentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractPaymentFromResultSet(rs);
                }
                return null;
            }
        }
    }

    public List<Payment> getPaymentsByOrder(int orderId) throws SQLException {
        String query = "SELECT * FROM Payments WHERE order_id = ? ORDER BY payment_date DESC";
        List<Payment> payments = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(extractPaymentFromResultSet(rs));
                }
            }
        }
        return payments;
    }

    public List<Payment> getPaymentsByDateRange(Date startDate, Date endDate) throws SQLException {
        String query = "SELECT * FROM Payments WHERE payment_date BETWEEN ? AND ? ORDER BY payment_date DESC";
        List<Payment> payments = new ArrayList<>();
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(extractPaymentFromResultSet(rs));
                }
            }
        }
        return payments;
    }

    public boolean updatePaymentStatus(int paymentId, String status) throws SQLException {
        String query = "UPDATE Payments SET status = ? WHERE payment_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, paymentId);
            
            return pstmt.executeUpdate() > 0;
        }
    }

    private Payment extractPaymentFromResultSet(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setOrderId(rs.getInt("order_id"));
        payment.setAmount(rs.getDouble("amount"));
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setStatus(rs.getString("status"));
        payment.setPaymentDate(rs.getTimestamp("payment_date"));
        payment.setTransactionId(rs.getString("transaction_id"));
        payment.setNotes(rs.getString("notes"));
        return payment;
    }
} 