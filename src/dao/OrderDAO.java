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

    public OrderDAO() {
        this.ingredientDAO = new IngredientDAO();
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
        // First check if we have enough ingredients for all items
        if (!checkIngredientAvailability(order)) {
            return -1; // Not enough ingredients
        }

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
            
            String query = "INSERT INTO Orders (customer_id, order_datetime, order_type, order_status, payment_status) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, order.getCustomerId());
                pstmt.setTimestamp(2, order.getOrderDateTime());
                pstmt.setString(3, order.getOrderType());
                pstmt.setString(4, order.getOrderStatus());
                pstmt.setString(5, order.getPaymentStatus());
                
                pstmt.executeUpdate();
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int orderId = rs.getInt(1);
                        order.setOrderId(orderId);
                        
                        // Add order items
                        if (!addOrderItems(order)) {
                            conn.rollback();
                            return -1;
                        }
                        
                        // Assign employees
                        if (!assignEmployeesToOrder(order)) {
                            conn.rollback();
                            return -1;
                        }
                        
                        // Deduct ingredients
                        if (!deductIngredients(order)) {
                            conn.rollback();
                            return -1;
                        }
                        
                        conn.commit();
                        return orderId;
                    }
                }
            }
            conn.rollback();
            return -1;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    private boolean checkIngredientAvailability(Order order) {
        Map<Integer, Double> requiredIngredients = new HashMap<>();
        
        // Calculate total required ingredients
        for (OrderItem item : order.getItems()) {
            Map<Integer, Double> itemIngredients = getRequiredIngredientsForItem(item);
            for (Map.Entry<Integer, Double> entry : itemIngredients.entrySet()) {
                requiredIngredients.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }
        
        // Check if we have enough of each ingredient
        String query = "SELECT ingredient_id, quantity_in_stock FROM Ingredients WHERE ingredient_id = ?";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            for (Map.Entry<Integer, Double> entry : requiredIngredients.entrySet()) {
                pstmt.setInt(1, entry.getKey());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        double stock = rs.getDouble("quantity_in_stock");
                        if (stock < entry.getValue()) {
                            return false;
                        }
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Map<Integer, Double> getRequiredIngredientsForItem(OrderItem item) {
        Map<Integer, Double> ingredients = new HashMap<>();
        String query = """
            SELECT di.ingredient_id, di.quantity_needed * ? as required_quantity
            FROM DishIngredients di
            WHERE di.dish_id = ?
        """;
        
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            pstmt.setInt(1, item.getQuantity());
            pstmt.setInt(2, item.getDishId());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ingredients.put(
                        rs.getInt("ingredient_id"),
                        rs.getDouble("required_quantity")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients;
    }

    private boolean deductIngredients(Order order) {
        Map<Integer, Double> requiredIngredients = new HashMap<>();
        
        // Calculate total required ingredients
        for (OrderItem item : order.getItems()) {
            Map<Integer, Double> itemIngredients = getRequiredIngredientsForItem(item);
            for (Map.Entry<Integer, Double> entry : itemIngredients.entrySet()) {
                requiredIngredients.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }
        
        // Deduct ingredients and record transactions
        for (Map.Entry<Integer, Double> entry : requiredIngredients.entrySet()) {
            if (!ingredientDAO.updateStock(
                entry.getKey(),
                -entry.getValue(),
                order.getAssignedEmployees().get(0), // Use first assigned employee
                "Usage",
                "Used in Order #" + order.getOrderId()
            )) {
                return false;
            }
        }
        
        return true;
    }

    private boolean addOrderItems(Order order) {
        String query = "INSERT INTO OrderItems (order_id, dish_id, quantity, price_at_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            for (OrderItem item : order.getItems()) {
                pstmt.setInt(1, order.getOrderId());
                pstmt.setInt(2, item.getDishId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setDouble(4, item.getPriceAtTime());
                pstmt.addBatch();
            }
            int[] results = pstmt.executeBatch();
            return results.length == order.getItems().size();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean assignEmployeesToOrder(Order order) {
        String query = "INSERT INTO AssignedEmployeesToOrders (order_id, employee_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(query)) {
            for (Integer employeeId : order.getAssignedEmployees()) {
                pstmt.setInt(1, order.getOrderId());
                pstmt.setInt(2, employeeId);
                pstmt.addBatch();
            }
            int[] results = pstmt.executeBatch();
            return results.length == order.getAssignedEmployees().size();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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