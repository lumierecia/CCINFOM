package dao;

import model.Order;
import model.OrderItem;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class OrderDAO {
    private IngredientDAO ingredientDAO;
    private final TableDAO tableDAO;

    public OrderDAO() throws SQLException {
        this.ingredientDAO = new IngredientDAO();
        this.tableDAO = new TableDAO();
    }

    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        List<Integer> orderIds = new ArrayList<>();
        
        // First, get all orders
        String orderQuery = "SELECT * FROM Orders WHERE is_deleted = FALSE ORDER BY order_datetime DESC";
        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(orderQuery);
                 ResultSet rs = stmt.executeQuery()) {
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
                    orders.add(order);
                    orderIds.add(order.getOrderId());
                }
            }

            if (!orderIds.isEmpty()) {
                // Get all order items in a single query
                String itemQuery = """
                    SELECT oi.*, d.name as dish_name
                    FROM OrderItems oi
                    JOIN Dishes d ON oi.dish_id = d.dish_id
                    WHERE oi.order_id IN (
                """ + String.join(",", Collections.nCopies(orderIds.size(), "?")) + ")";

                Map<Integer, List<OrderItem>> itemsMap = new HashMap<>();
                try (PreparedStatement stmt = conn.prepareStatement(itemQuery)) {
                    for (int i = 0; i < orderIds.size(); i++) {
                        stmt.setInt(i + 1, orderIds.get(i));
                    }
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            OrderItem item = new OrderItem(
                                rs.getInt("order_id"),
                                rs.getInt("dish_id"),
                                rs.getInt("quantity"),
                                rs.getDouble("price_at_time"),
                                rs.getString("dish_name")
                            );
                            itemsMap.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>())
                                   .add(item);
                        }
                    }
                }

                // Get all assigned employees in a single query
                String employeeQuery = """
                    SELECT order_id, employee_id
                    FROM AssignedEmployeesToOrders
                    WHERE order_id IN (
                """ + String.join(",", Collections.nCopies(orderIds.size(), "?")) + ")";

                Map<Integer, List<Integer>> employeesMap = new HashMap<>();
                try (PreparedStatement stmt = conn.prepareStatement(employeeQuery)) {
                    for (int i = 0; i < orderIds.size(); i++) {
                        stmt.setInt(i + 1, orderIds.get(i));
                    }
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            int orderId = rs.getInt("order_id");
                            int employeeId = rs.getInt("employee_id");
                            employeesMap.computeIfAbsent(orderId, k -> new ArrayList<>())
                                      .add(employeeId);
                        }
                    }
                }

                // Assign items and employees to orders
                for (Order order : orders) {
                    order.setItems(itemsMap.getOrDefault(order.getOrderId(), new ArrayList<>()));
                    order.setAssignedEmployees(employeesMap.getOrDefault(order.getOrderId(), new ArrayList<>()));
                }
            }
        }
        return orders;
    }

    private List<OrderItem> getOrderItems(Connection conn, int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();
        String query = """
            SELECT oi.*, d.name as dish_name
            FROM OrderItems oi
            JOIN Dishes d ON oi.dish_id = d.dish_id
            WHERE oi.order_id = ?
        """;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
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
        }
        return items;
    }

    public List<OrderItem> getOrderItems(int orderId) throws SQLException {
        try (Connection conn = getConnection()) {
            return getOrderItems(conn, orderId);
        }
    }

    public List<Integer> getAssignedEmployees(int orderId) throws SQLException {
        try (Connection conn = getConnection()) {
            return getAssignedEmployees(conn, orderId);
        }
    }

    private List<Integer> getAssignedEmployees(Connection conn, int orderId) throws SQLException {
        List<Integer> employeeIds = new ArrayList<>();
        String query = "SELECT employee_id FROM AssignedEmployeesToOrders WHERE order_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    employeeIds.add(rs.getInt("employee_id"));
                }
            }
        }
        return employeeIds;
    }

    public Order getOrderById(int orderId) throws SQLException {
        String query = "SELECT * FROM Orders WHERE order_id = ? AND is_deleted = FALSE";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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
                    order.setItems(getOrderItems(conn, order.getOrderId()));
                    order.setAssignedEmployees(getAssignedEmployees(conn, order.getOrderId()));
                    return order;
                }
            }
        }
        return null;
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

    private boolean checkIngredientAvailability(Order order) throws SQLException {
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
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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
        }
    }

    private Map<Integer, Double> getRequiredIngredientsForItem(OrderItem item) throws SQLException {
        Map<Integer, Double> ingredients = new HashMap<>();
        String query = """
            SELECT di.ingredient_id, di.quantity_needed * ? as required_quantity
            FROM DishIngredients di
            WHERE di.dish_id = ?
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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
        }
        return ingredients;
    }

    private boolean deductIngredients(Order order) throws SQLException {
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

    private boolean addOrderItems(Order order) throws SQLException {
        String query = "INSERT INTO OrderItems (order_id, dish_id, quantity, price_at_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            for (OrderItem item : order.getItems()) {
                pstmt.setInt(1, order.getOrderId());
                pstmt.setInt(2, item.getDishId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setDouble(4, item.getPriceAtTime());
                pstmt.addBatch();
            }
            int[] results = pstmt.executeBatch();
            return results.length == order.getItems().size();
        }
    }

    private boolean assignEmployeesToOrder(Order order) throws SQLException {
        String query = "INSERT INTO AssignedEmployeesToOrders (order_id, employee_id) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            for (Integer employeeId : order.getAssignedEmployees()) {
                pstmt.setInt(1, order.getOrderId());
                pstmt.setInt(2, employeeId);
                pstmt.addBatch();
            }
            int[] results = pstmt.executeBatch();
            return results.length == order.getAssignedEmployees().size();
        }
    }

    public boolean updateOrderStatus(int orderId, String status) throws SQLException {
        String query = "UPDATE Orders SET order_status = ? WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public List<Order> getOrdersByDateRange(String startDate, String endDate) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM Orders WHERE DATE(order_datetime) BETWEEN ? AND ? ORDER BY order_datetime DESC";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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
                    order.setItems(getOrderItems(conn, order.getOrderId()));
                    order.setAssignedEmployees(getAssignedEmployees(conn, order.getOrderId()));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    public List<Order> getOrdersByCustomerId(int customerId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = """
            SELECT * FROM Orders 
            WHERE customer_id = ? AND is_deleted = FALSE 
            ORDER BY order_datetime DESC
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
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
                    order.setItems(getOrderItems(conn, order.getOrderId()));
                    order.setAssignedEmployees(getAssignedEmployees(conn, order.getOrderId()));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    public double calculateOrderTotal(int orderId) throws SQLException {
        List<OrderItem> items = getOrderItems(orderId);
        double total = 0.0;
        for (OrderItem item : items) {
            total += item.getQuantity() * item.getPriceAtTime();
        }
        return total;
    }

    public boolean deleteOrder(int orderId) throws SQLException {
        String updateQuery = "UPDATE Orders SET is_deleted = TRUE WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Order> getDeletedOrders() throws SQLException {
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
        }
        return orders;
    }

    public boolean restoreOrder(int orderId) throws SQLException {
        String query = "UPDATE Orders SET is_deleted = FALSE WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean addOrderItems(int orderId, List<OrderItem> items) throws SQLException {
        String query = "INSERT INTO OrderItems (order_id, dish_id, quantity, price_at_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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

    public boolean assignEmployeesToOrder(int orderId, List<Integer> employeeIds) throws SQLException {
        String query = "INSERT INTO AssignedEmployeesToOrders (order_id, employee_id) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            for (Integer employeeId : employeeIds) {
                pstmt.setInt(1, orderId);
                pstmt.setInt(2, employeeId);
                pstmt.addBatch();
            }
            int[] results = pstmt.executeBatch();
            return results.length == employeeIds.size();
        }
    }

    public List<Order> getAllActiveOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM Orders WHERE is_deleted = FALSE ORDER BY order_datetime DESC";
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
                order.setItems(getOrderItems(conn, order.getOrderId()));
                order.setAssignedEmployees(getAssignedEmployees(conn, order.getOrderId()));
                orders.add(order);
            }
        }
        return orders;
    }

    public boolean updatePaymentStatus(int orderId, String paymentMethod, String paymentStatus) throws SQLException {
        String query = "UPDATE Orders SET payment_method = ?, payment_status = ? WHERE order_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, paymentMethod);
            pstmt.setString(2, paymentStatus);
            pstmt.setInt(3, orderId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean completeOrder(int orderId) throws SQLException {
        try (Connection conn = getConnection()) {
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
        try (Connection conn = getConnection()) {
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

    public List<Order> getCustomerOrders(int customerId) throws SQLException {
        return getOrdersByCustomerId(customerId);
    }
} 
