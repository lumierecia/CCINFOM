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
    private Connection connection;
    private final TableDAO tableDAO;

    public OrderDAO() throws SQLException {
        this.ingredientDAO = new IngredientDAO();
        this.connection = DatabaseConnection.getConnection();
        this.tableDAO = new TableDAO();
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM Orders WHERE is_deleted = FALSE ORDER BY order_datetime DESC";
        try (Statement stmt = connection.createStatement();
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
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
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
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
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

    public int createOrder(Order order) throws SQLException {
        String sql = """
            INSERT INTO Orders (customer_id, order_type, order_status, total_amount, 
                              payment_method, payment_status, table_id) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, order.getCustomerId());
            pstmt.setString(2, order.getOrderType());
            pstmt.setString(3, order.getOrderStatus());
            pstmt.setDouble(4, order.getTotalAmount());
            pstmt.setString(5, order.getPaymentMethod());
            pstmt.setString(6, order.getPaymentStatus());
            pstmt.setInt(7, order.getTableId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);
                    
                    // Update table status if it's a dine-in order
                    if ("Dine-In".equals(order.getOrderType()) && order.getTableId() > 0) {
                        tableDAO.updateTableStatus(order.getTableId(), "Occupied");
                    }
                    
                    return orderId;
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
        }
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
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
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
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
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
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
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
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
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
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
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
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
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
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem(
                        rs.getInt("order_id"),
                        rs.getInt("dish_id"),
                        rs.getInt("quantity"),
                        rs.getDouble("price_at_time"),
                        rs.getString("dish_name")
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
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
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
        try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
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
        
        try (Statement stmt = connection.createStatement();
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
        
        try (Connection conn = connection;
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

    public boolean addOrderItems(int orderId, List<OrderItem> items) {
        String query = "INSERT INTO OrderItems (order_id, dish_id, quantity, price_at_time) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            for (OrderItem item : items) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, item.getDishId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setDouble(4, item.getPriceAtTime());
                pstmt.addBatch();
            }
            int[] results = pstmt.executeBatch();
            return results.length == items.size();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean assignEmployeesToOrder(int orderId, List<Integer> employeeIds) {
        String query = "INSERT INTO AssignedEmployeesToOrders (order_id, employee_id) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            for (Integer employeeId : employeeIds) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, employeeId);
                pstmt.addBatch();
            }
            int[] results = pstmt.executeBatch();
            return results.length == employeeIds.size();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Order> getAllActiveOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM Orders WHERE is_deleted = FALSE ORDER BY order_datetime DESC";
        try (Statement stmt = connection.createStatement();
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

    public boolean updatePaymentStatus(int orderId, String paymentMethod, String paymentStatus) {
        String query = "UPDATE Orders SET payment_method = ?, payment_status = ? WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, paymentMethod);
            pstmt.setString(2, paymentStatus);
            pstmt.setInt(3, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean completeOrder(int orderId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Get order details first
                Order order = getOrderById(orderId);
                if (order == null) {
                    throw new SQLException("Order not found");
                }

                // Update order status
                boolean orderUpdated = updateOrderStatus(orderId, "Completed");
                if (!orderUpdated) {
                    throw new SQLException("Failed to update order status");
                }

                // If it's a dine-in order, update table status
                if ("Dine-In".equals(order.getOrderType()) && order.getTableId() > 0) {
                    boolean tableUpdated = tableDAO.updateTableStatus(order.getTableId(), "Available");
                    if (!tableUpdated) {
                        throw new SQLException("Failed to update table status");
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public boolean cancelOrder(int orderId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Get order details first
                Order order = getOrderById(orderId);
                if (order == null) {
                    throw new SQLException("Order not found");
                }

                // Update order status
                boolean orderUpdated = updateOrderStatus(orderId, "Cancelled");
                if (!orderUpdated) {
                    throw new SQLException("Failed to update order status");
                }

                // If it's a dine-in order, update table status
                if ("Dine-In".equals(order.getOrderType()) && order.getTableId() > 0) {
                    boolean tableUpdated = tableDAO.updateTableStatus(order.getTableId(), "Available");
                    if (!tableUpdated) {
                        throw new SQLException("Failed to update table status");
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
} 