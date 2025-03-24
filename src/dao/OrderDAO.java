package dao;

import model.Order;
import model.OrderItem;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.JOptionPane;

public class OrderDAO {
    private IngredientDAO ingredientDAO;
    private DishDAO dishDAO;

    public OrderDAO() {
        this.ingredientDAO = new IngredientDAO();
        this.dishDAO = new DishDAO();
    }

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM Orders WHERE is_deleted = FALSE ORDER BY order_datetime DESC";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("order_id"),
                    rs.getInt("customer_id"),
                    rs.getTimestamp("order_datetime"),
                    rs.getString("order_type"),
                    rs.getString("order_status"),
                    rs.getString("payment_status"),
                    rs.getDouble("total_amount"),
                    rs.getString("payment_method")
                );
                order.setItems(getOrderItems(order.getOrderId()));
                order.setAssignedEmployees(getAssignedEmployees(order.getOrderId()));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public Order getOrderById(int orderId) {
        String query = "SELECT * FROM Orders WHERE order_id = ? AND is_deleted = FALSE";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getTimestamp("order_datetime"),
                        rs.getString("order_type"),
                        rs.getString("order_status"),
                        rs.getString("payment_status"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_method")
                    );
                    order.setItems(getOrderItems(order.getOrderId()));
                    order.setAssignedEmployees(getAssignedEmployees(order.getOrderId()));
                    return order;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Integer> getAssignedEmployees(int orderId) {
        List<Integer> employees = new ArrayList<>();
        String query = "SELECT employee_id FROM AssignedEmployeesToOrders WHERE order_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    employees.add(rs.getInt("employee_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    public int createOrder(Order order) {
        Connection conn = null;
        boolean originalAutoCommit = false;
        try {
            conn = getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // First insert the order to get the order ID
            String query = """
                INSERT INTO Orders (customer_id, order_type, order_status, total_amount, payment_status)
                VALUES (?, ?, ?, ?, ?)
            """;
            
            int orderId;
            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, order.getCustomerId());
                stmt.setString(2, order.getOrderType());
                stmt.setString(3, order.getOrderStatus());
                stmt.setDouble(4, order.getTotalAmount());
                stmt.setString(5, order.getPaymentStatus());
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    return -1;
                }

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                        order.setOrderId(orderId);
                    } else {
                        conn.rollback();
                        return -1;
                    }
                }
            }

            // Now check and deduct ingredients with the valid order ID
            if (!deductIngredients(conn, orderId, order.getItems())) {
                conn.rollback();
                JOptionPane.showMessageDialog(null,
                    "Failed to place order. Please check ingredient availability.",
                    "Order Error",
                    JOptionPane.ERROR_MESSAGE);
                return -1;
            }

            // Add order items
            if (!addOrderItems(conn, orderId, order.getItems())) {
                conn.rollback();
                return -1;
            }

            // Assign employees
            if (!assignEmployeesToOrder(conn, orderId, order.getAssignedEmployees())) {
                conn.rollback();
                return -1;
            }

            conn.commit();
            return orderId;
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to create order: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return -1;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(originalAutoCommit);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean deductIngredients(Connection conn, int orderId, List<OrderItem> items) throws SQLException {
        // Get all ingredients needed for this order
        Map<Integer, Double> totalIngredientsNeeded = new HashMap<>();
        
        // Calculate total ingredients needed
        for (OrderItem item : items) {
            Map<Integer, Double> dishIngredients = dishDAO.getDishIngredients(item.getDishId());
            for (Map.Entry<Integer, Double> entry : dishIngredients.entrySet()) {
                int ingredientId = entry.getKey();
                double quantityPerDish = entry.getValue();
                double totalQuantity = quantityPerDish * item.getQuantity();
                
                totalIngredientsNeeded.merge(ingredientId, totalQuantity, Double::sum);
            }
        }
        
        // Check if we have enough of all ingredients
        String checkQuery = "SELECT ingredient_id, quantity_in_stock FROM Ingredients WHERE ingredient_id = ? FOR UPDATE";
        String updateQuery = "UPDATE Ingredients SET quantity_in_stock = quantity_in_stock - ? WHERE ingredient_id = ?";
        String transactionQuery = """
            INSERT INTO IngredientTransactions 
            (ingredient_id, transaction_type, quantity_change, order_id, employee_id, notes)
            VALUES (?, 'Usage', ?, ?, ?, ?)
        """;
        
        for (Map.Entry<Integer, Double> entry : totalIngredientsNeeded.entrySet()) {
            int ingredientId = entry.getKey();
            double neededQuantity = entry.getValue();
            
            // Check current stock with lock
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, ingredientId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (!rs.next() || rs.getDouble("quantity_in_stock") < neededQuantity) {
                    return false;
                }
            }
            
            // Update stock
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setDouble(1, neededQuantity);
                updateStmt.setInt(2, ingredientId);
                updateStmt.executeUpdate();
            }
            
            // Record transaction
            try (PreparedStatement transStmt = conn.prepareStatement(transactionQuery)) {
                transStmt.setInt(1, ingredientId);
                transStmt.setDouble(2, -neededQuantity); // Negative because it's a deduction
                transStmt.setInt(3, orderId);
                transStmt.setInt(4, 1); // Default to admin ID 1
                transStmt.setString(5, "Ingredients used for order #" + orderId);
                transStmt.executeUpdate();
            }
        }
        
        return true;
    }

    private boolean addOrderItems(Connection conn, int orderId, List<OrderItem> items) throws SQLException {
        String query = "INSERT INTO OrderItems (order_id, dish_id, quantity, price_at_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            for (OrderItem item : items) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, item.getDishId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setDouble(4, item.getPriceAtTime());
                pstmt.addBatch();
            }
            int[] results = pstmt.executeBatch();
            return results.length == items.size();
        }
    }

    private boolean assignEmployeesToOrder(Connection conn, int orderId, List<Integer> employees) throws SQLException {
        String query = "INSERT INTO AssignedEmployeesToOrders (order_id, employee_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            for (Integer employeeId : employees) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, employeeId);
                pstmt.addBatch();
            }
            int[] results = pstmt.executeBatch();
            return results.length == employees.size();
        }
    }

    public boolean updateOrderStatus(int orderId, String status) {
        String query = "UPDATE Orders SET order_status = ? WHERE order_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Order> getOrdersByDateRange(String startDate, String endDate) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM Orders WHERE DATE(order_datetime) BETWEEN ? AND ? ORDER BY order_datetime DESC";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getTimestamp("order_datetime"),
                        rs.getString("order_type"),
                        rs.getString("order_status"),
                        rs.getString("payment_status"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_method")
                    );
                    order.setItems(getOrderItems(order.getOrderId()));
                    order.setAssignedEmployees(getAssignedEmployees(order.getOrderId()));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String query = """
            SELECT oi.*, d.name as dish_name 
            FROM OrderItems oi 
            JOIN Dishes d ON oi.dish_id = d.dish_id 
            WHERE oi.order_id = ?
        """;
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                        rs.getInt("order_id"),
                        rs.getInt("dish_id"),
                        rs.getString("dish_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price_at_time")
                    );
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error fetching order items: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return items;
    }

    public List<Order> getOrdersByCustomerId(int customerId) {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM Orders WHERE customer_id = ? ORDER BY order_datetime DESC";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getTimestamp("order_datetime"),
                        rs.getString("order_type"),
                        rs.getString("order_status"),
                        rs.getString("payment_status"),
                        rs.getDouble("total_amount"),
                        rs.getString("payment_method")
                    );
                    order.setItems(getOrderItems(order.getOrderId()));
                    order.setAssignedEmployees(getAssignedEmployees(order.getOrderId()));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public double calculateOrderTotal(int orderId) {
        List<OrderItem> items = getOrderItems(orderId);
        double total = 0.0;
        for (OrderItem item : items) {
            total += item.getQuantity() * item.getPriceAtTime();
        }
        return total;
    }

    public boolean deleteOrder(int orderId) {
        String updateQuery = "UPDATE Orders SET is_deleted = TRUE WHERE order_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(updateQuery)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Order> getDeletedOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT o.*, c.first_name, c.last_name FROM Orders o " +
                      "JOIN Customers c ON o.customer_id = c.customer_id " +
                      "WHERE o.is_deleted = TRUE " +
                      "ORDER BY o.order_datetime DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("order_id"),
                    rs.getInt("customer_id"),
                    rs.getTimestamp("order_datetime"),
                    rs.getString("order_type"),
                    rs.getString("order_status"),
                    rs.getString("payment_status"),
                    rs.getDouble("total_amount"),
                    rs.getString("payment_method")
                );
                order.setCustomerName(rs.getString("first_name") + " " + rs.getString("last_name"));
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch deleted orders: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return orders;
    }

    public boolean restoreOrder(int orderId) {
        String query = "UPDATE Orders SET is_deleted = FALSE WHERE order_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to restore order: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
} 
