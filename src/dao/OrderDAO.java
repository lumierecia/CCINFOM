package dao;

import model.Order;
import model.OrderItem;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class OrderDAO {
    private Connection connection;
    private IngredientDAO ingredientDAO;

    public OrderDAO() {
        this.connection = DatabaseConnection.getConnection();
        this.ingredientDAO = new IngredientDAO();
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM Orders ORDER BY order_datetime DESC";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Order order = new Order(
                    rs.getInt("order_id"),
                    rs.getInt("customer_id"),
                    rs.getTimestamp("order_datetime"),
                    rs.getString("order_type"),
                    rs.getString("order_status")
                );
                loadOrderItems(order);
                loadAssignedEmployees(order);
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public Order getOrderById(int orderId) {
        String query = "SELECT * FROM Orders WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Order order = new Order(
                    rs.getInt("order_id"),
                    rs.getInt("customer_id"),
                    rs.getTimestamp("order_datetime"),
                    rs.getString("order_type"),
                    rs.getString("order_status")
                );
                loadOrderItems(order);
                loadAssignedEmployees(order);
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadOrderItems(Order order) {
        String query = "SELECT * FROM Order_Item WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, order.getOrderId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                OrderItem item = new OrderItem(
                    rs.getInt("order_item_id"),
                    rs.getInt("order_id"),
                    rs.getInt("product_id"),
                    rs.getInt("quantity")
                );
                order.getOrderItems().add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAssignedEmployees(Order order) {
        String query = "SELECT employee_id FROM Assigned_Employee_to_Order WHERE order_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, order.getOrderId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                order.getAssignedEmployees().add(rs.getInt("employee_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int createOrder(Order order) {
        // First check if we have enough ingredients for all items
        if (!checkIngredientAvailability(order)) {
            return -1; // Not enough ingredients
        }

        String query = "INSERT INTO Orders (customer_id, order_type, order_status) VALUES (?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            
            try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, order.getCustomerId());
                pstmt.setString(2, order.getOrderType());
                pstmt.setString(3, "In Progress");
                
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    order.setOrderId(orderId);
                    
                    // Add order items
                    if (!addOrderItems(order)) {
                        connection.rollback();
                        return -1;
                    }
                    
                    // Assign employees
                    if (!assignEmployeesToOrder(order)) {
                        connection.rollback();
                        return -1;
                    }
                    
                    // Deduct ingredients
                    if (!deductIngredients(order)) {
                        connection.rollback();
                        return -1;
                    }
                    
                    connection.commit();
                    return orderId;
                }
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    private boolean checkIngredientAvailability(Order order) {
        Map<Integer, Double> requiredIngredients = new HashMap<>();
        
        // Calculate total required ingredients
        for (OrderItem item : order.getOrderItems()) {
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
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    double stock = rs.getDouble("quantity_in_stock");
                    if (stock < entry.getValue()) {
                        return false;
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
            WHERE di.product_id = ?
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, item.getQuantity());
            pstmt.setInt(2, item.getProductId());
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ingredients.put(
                    rs.getInt("ingredient_id"),
                    rs.getDouble("required_quantity")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients;
    }

    private boolean deductIngredients(Order order) {
        Map<Integer, Double> requiredIngredients = new HashMap<>();
        
        // Calculate total required ingredients
        for (OrderItem item : order.getOrderItems()) {
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
        String query = "INSERT INTO Order_Item (order_id, product_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            for (OrderItem item : order.getOrderItems()) {
                pstmt.setInt(1, order.getOrderId());
                pstmt.setInt(2, item.getProductId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.addBatch();
            }
            int[] results = pstmt.executeBatch();
            return results.length == order.getOrderItems().size();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean assignEmployeesToOrder(Order order) {
        String query = "INSERT INTO Assigned_Employee_to_Order (order_id, employee_id) VALUES (?, ?)";
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
        String sql = "SELECT * FROM Orders WHERE DATE(order_datetime) BETWEEN ? AND ? ORDER BY order_datetime";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                        rs.getInt("order_id"),
                        rs.getInt("customer_id"),
                        rs.getTimestamp("order_datetime"),
                        rs.getString("order_type"),
                        rs.getString("order_status")
                    );
                    loadOrderItems(order);
                    loadAssignedEmployees(order);
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return orders;
    }
} 