package controller;

import dao.OrderDAO;
import dao.CustomerDAO;
import dao.EmployeeDAO;
import dao.IngredientDAO;
import dao.SupplierDAO;
import dao.InventoryDAO;
import dao.DishDAO;
import dao.IngredientBatchDAO;
import model.Order;
import model.Customer;
import model.Employee;
import model.Inventory;
import model.OrderItem;
import model.Supplier;
import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import model.Ingredient;
import model.IngredientBatch;
import model.Dish;
import java.util.Map;

public class RestaurantController {
    private final Connection connection;
    private final CustomerDAO customerDAO;
    private final OrderDAO orderDAO;
    private final InventoryDAO inventoryDAO;
    private final EmployeeDAO employeeDAO;
    private final IngredientDAO ingredientDAO;
    private final IngredientBatchDAO ingredientBatchDAO;
    private SupplierDAO supplierDAO;
    private final DishDAO dishDAO;

    public RestaurantController() {
        try {
            this.connection = DatabaseConnection.getConnection();
            this.customerDAO = new CustomerDAO();
            this.orderDAO = new OrderDAO();
            this.inventoryDAO = new InventoryDAO();
            this.employeeDAO = new EmployeeDAO();
            this.ingredientDAO = new IngredientDAO();
            this.ingredientBatchDAO = new IngredientBatchDAO();
            this.supplierDAO = new SupplierDAO();
            this.dishDAO = new DishDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    // Category Management Methods
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String query = "SELECT category_name FROM Categories WHERE is_deleted = FALSE ORDER BY category_name";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error fetching categories: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return categories;
    }

    private int getCategoryId(String categoryName) {
        String query = "SELECT category_id FROM Categories WHERE category_name = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, categoryName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("category_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to get category ID: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return -1;
    }

    // Inventory Management Methods
    public List<Inventory> getAllInventoryItems() {
        List<Inventory> items = new ArrayList<>();
        String query = """
            SELECT i.*, c.category_name,
                   CASE 
                       WHEN i.quantity = 0 THEN 'Unavailable'
                       ELSE 'Available'
                   END as status
            FROM InventoryItems i 
            JOIN Categories c ON i.category_id = c.category_id 
            ORDER BY i.product_name
        """;
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Inventory item = new Inventory(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("make_price"),
                    rs.getDouble("sell_price"),
                    rs.getString("status")
                );
                item.setRecipeInstructions(rs.getString("recipe_instructions"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to fetch inventory items: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return items;
    }

    public List<Inventory> getInventoryItemsByCategory(String categoryName) {
        List<Inventory> items = new ArrayList<>();
        String query = """
            SELECT i.*, c.category_name,
                   CASE 
                       WHEN i.quantity = 0 THEN 'Unavailable'
                       ELSE 'Available'
                   END as status
            FROM InventoryItems i 
            JOIN Categories c ON i.category_id = c.category_id 
            WHERE c.category_name = ? 
            ORDER BY i.product_name
        """;
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, categoryName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Inventory item = new Inventory(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("category_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("make_price"),
                        rs.getDouble("sell_price"),
                        rs.getString("status")
                    );
                    item.setRecipeInstructions(rs.getString("recipe_instructions"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to fetch inventory items by category: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return items;
    }

    public Inventory getInventoryItemById(int id) {
        String query = """
            SELECT i.*, c.category_name 
            FROM InventoryItems i 
            JOIN Categories c ON i.category_id = c.category_id 
            WHERE i.product_id = ?
        """;
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Inventory item = new Inventory(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("category_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("make_price"),
                        rs.getDouble("sell_price")
                    );
                    item.setRecipeInstructions(rs.getString("recipe_instructions"));
                    return item;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to fetch inventory item: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    public boolean addInventoryItem(String name, String categoryName, double makePrice, 
                                  double sellPrice, int quantity, String recipe) {
        return addInventoryItem(name, categoryName, makePrice, sellPrice, quantity, recipe, 1); // Default to admin ID 1
    }

    public boolean addInventoryItem(String name, String categoryName, double makePrice, 
                                  double sellPrice, int quantity, String recipe, int employeeId) {
        int categoryId = getCategoryId(categoryName);
        if (categoryId == -1) {
            JOptionPane.showMessageDialog(null,
                    "Invalid category: " + categoryName,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String query = """
            INSERT INTO InventoryItems (
                product_name, category_id, make_price, sell_price, quantity, 
                status, recipe_instructions, last_restocked_by
            ) VALUES (?, ?, ?, ?, ?, 'Available', ?, ?)
        """;
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setInt(2, categoryId);
            stmt.setDouble(3, makePrice);
            stmt.setDouble(4, sellPrice);
            stmt.setInt(5, quantity);
            stmt.setString(6, recipe);
            stmt.setInt(7, employeeId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to add inventory item: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean updateInventoryItem(int id, String name, String categoryName, 
                                     double makePrice, double sellPrice, 
                                     int quantity, String recipe) {
        int categoryId = getCategoryId(categoryName);
        if (categoryId == -1) {
            JOptionPane.showMessageDialog(null,
                    "Invalid category: " + categoryName,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Validate input values
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Product name cannot be empty.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (makePrice <= 0) {
            JOptionPane.showMessageDialog(null,
                    "Make price must be greater than 0.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (sellPrice <= 0) {
            JOptionPane.showMessageDialog(null,
                    "Sell price must be greater than 0.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (sellPrice <= makePrice) {
            JOptionPane.showMessageDialog(null,
                    "Sell price must be greater than make price.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (quantity < 0) {
            JOptionPane.showMessageDialog(null,
                    "Quantity cannot be negative.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String query = """
            UPDATE InventoryItems 
            SET product_name = ?, category_id = ?, make_price = ?, 
                sell_price = ?, quantity = ?, recipe_instructions = ?,
                status = CASE 
                    WHEN ? = 0 THEN 'Unavailable'
                    ELSE 'Available'
                END
            WHERE product_id = ?
        """;
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setInt(2, categoryId);
            stmt.setDouble(3, makePrice);
            stmt.setDouble(4, sellPrice);
            stmt.setInt(5, quantity);
            stmt.setString(6, recipe);
            stmt.setInt(7, quantity);
            stmt.setInt(8, id);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            String errorMessage = "Failed to update inventory item: ";
            if (e.getMessage().contains("inventoryitems_chk_3")) {
                errorMessage += "Invalid price values. Make price and sell price must be greater than 0.";
            } else if (e.getMessage().contains("Duplicate entry")) {
                errorMessage += "A product with this name already exists.";
            } else {
                errorMessage += e.getMessage();
            }
            
            JOptionPane.showMessageDialog(null,
                    errorMessage,
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean deleteInventoryItem(int id) {
        String query = "DELETE FROM InventoryItems WHERE product_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to delete inventory item: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    // Delegate to DAOs
    public List<Order> getAllOrders() {
        return orderDAO.getAllOrders();
    }

    public Order getOrderById(int id) {
        return orderDAO.getOrderById(id);
    }

    public boolean createOrder(Order order, List<OrderItem> items, List<Integer> employeeIds) {
        order.setItems(items);
        order.setAssignedEmployees(employeeIds);
        return orderDAO.createOrder(order) > 0;
    }

    public List<Order> getOrdersByDateRange(String startDate, String endDate) {
        return orderDAO.getOrdersByDateRange(startDate, endDate);
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.getAllCustomers();
    }

    public Customer getCustomerById(int id) {
        return customerDAO.getCustomerById(id);
    }

    public int addCustomer(Customer customer) {
        return customerDAO.addCustomer(customer);
    }

    public boolean updateCustomer(Customer customer) {
        return customerDAO.updateCustomer(customer);
    }

    public boolean deleteCustomer(int customerId) {
        return customerDAO.deleteCustomer(customerId);
    }

    public boolean addEmployee(Employee employee) {
        return employeeDAO.addEmployee(employee);
    }

    public boolean updateEmployee(Employee employee) {
        return employeeDAO.updateEmployee(employee);
    }

    public boolean deleteEmployee(int employeeId) {
        return employeeDAO.deleteEmployee(employeeId);
    }

    public Employee getEmployeeById(int employeeId) {
        return employeeDAO.getEmployeeById(employeeId);
    }

    public Employee getEmployeeByName(String firstName, String lastName) {
        return employeeDAO.getEmployeeByName(firstName, lastName);
    }

    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees();
    }

    public boolean assignShift(int employeeId, int timeShiftId) {
        return employeeDAO.assignShift(employeeId, timeShiftId);
    }

    public boolean removeShift(int employeeId) {
        return employeeDAO.removeShift(employeeId);
    }

    public String getCurrentShift(int employeeId) {
        return employeeDAO.getCurrentShift(employeeId);
    }

    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String query = "SELECT supplier_id, name, contact_person, phone, email, address, is_deleted " +
                      "FROM Suppliers ORDER BY name";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Supplier supplier = new Supplier();
                supplier.setSupplierId(rs.getInt("supplier_id"));
                supplier.setName(rs.getString("name"));
                supplier.setContactPerson(rs.getString("contact_person"));
                supplier.setPhone(rs.getString("phone"));
                supplier.setEmail(rs.getString("email"));
                supplier.setAddress(rs.getString("address"));
                supplier.setDeleted(rs.getBoolean("is_deleted"));
                suppliers.add(supplier);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch suppliers: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        
        return suppliers;
    }

    public Supplier getSupplierById(int id) {
        return supplierDAO.getSupplierById(id);
    }

    public boolean addSupplier(Supplier supplier) {
        return supplierDAO.addSupplier(supplier);
    }

    public boolean updateSupplier(Supplier supplier) {
        return supplierDAO.updateSupplier(supplier);
    }

    public boolean deleteSupplier(int supplierId) {
        String query = "UPDATE Suppliers SET is_deleted = 1 WHERE supplier_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, supplierId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to delete supplier: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public List<Inventory> getSupplierIngredients(int supplierId) {
        return supplierDAO.getSupplierIngredients(supplierId);
    }

    // Customer Order Methods
    public List<Order> getCustomerOrders(int customerId) {
        return orderDAO.getOrdersByCustomerId(customerId);
    }

    public double calculateOrderTotal(int orderId) {
        List<OrderItem> items = orderDAO.getOrderItems(orderId);
        double total = 0.0;
        for (OrderItem item : items) {
            total += item.getQuantity() * item.getPriceAtTime();
        }
        return total;
    }

    public boolean processPayment(int orderId, double amountReceived, String paymentMethod) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // First check if the order exists and is pending payment
                String checkQuery = """
                    SELECT payment_status, order_status 
                    FROM Orders 
                    WHERE order_id = ?
                """;
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setInt(1, orderId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (!rs.next()) {
                        throw new SQLException("Order not found");
                    }
                    String currentPaymentStatus = rs.getString("payment_status");
                    if (!"Pending".equals(currentPaymentStatus)) {
                        throw new SQLException("Order is not pending payment");
                    }
                }

                // Update order with payment information
                String orderQuery = """
                    UPDATE Orders 
                    SET order_status = 'Completed', 
                        payment_status = 'Paid', 
                        payment_method = ?, 
                        total_amount = ? 
                    WHERE order_id = ?
                """;
                try (PreparedStatement orderStmt = conn.prepareStatement(orderQuery)) {
                    orderStmt.setString(1, paymentMethod);
                    orderStmt.setDouble(2, amountReceived);
                    orderStmt.setInt(3, orderId);
                    int updated = orderStmt.executeUpdate();
                    if (updated != 1) {
                        throw new SQLException("Failed to update order payment status");
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Failed to process payment: " + e.getMessage(),
                    "Payment Error",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Database connection error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public void generateSalesReport(String datePattern, javax.swing.table.DefaultTableModel tableModel) {
        String query = """
            SELECT
                DATE(o.order_datetime) AS sales_date,
                SUM(oi.quantity * oi.price_at_time) AS total_sales,
                AVG(oi.quantity * oi.price_at_time) AS average_sales,
                d.name AS top_product,
                SUM(oi.quantity) AS product_sold
            FROM Orders o
            JOIN OrderItems oi ON o.order_id = oi.order_id
            JOIN Dishes d ON oi.dish_id = d.dish_id
            WHERE DATE(o.order_datetime) LIKE ? AND d.is_deleted = FALSE
            GROUP BY sales_date, d.name
            ORDER BY sales_date, product_sold DESC
        """;

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, datePattern);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getString("sales_date"),
                    rs.getDouble("total_sales"),
                    rs.getDouble("average_sales"),
                    rs.getString("top_product"),
                    rs.getInt("product_sold")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error generating sales report: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void generateCustomerOrdersReport(String datePattern, javax.swing.table.DefaultTableModel tableModel) {
        String query = """
            SELECT
                COUNT(DISTINCT o.order_id) AS total_orders,
                SUM(oi.quantity * oi.price_at_time) AS total_amount_spent,
                d.name AS most_bought_product,
                SUM(oi.quantity) AS most_bought_quantity
            FROM Orders o
            JOIN OrderItems oi ON o.order_id = oi.order_id
            JOIN Dishes d ON oi.dish_id = d.dish_id
            WHERE DATE(o.order_datetime) LIKE ? AND d.is_deleted = FALSE
            GROUP BY d.name
            ORDER BY most_bought_quantity DESC
            LIMIT 1
        """;

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, datePattern);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("total_orders"),
                    rs.getDouble("total_amount_spent"),
                    rs.getString("most_bought_product"),
                    rs.getInt("most_bought_quantity")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error generating customer orders report: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void generateEmployeeShiftsReport(String datePattern, javax.swing.table.DefaultTableModel tableModel) {
        String query = """
            SELECT
                e.first_name,
                e.last_name,
                COUNT(DISTINCT a.order_id) AS num_of_shifts,
                TIMESTAMPDIFF(
                    HOUR,
                    ts.time_start,
                    CASE
                        WHEN ts.time_end < ts.time_start THEN ADDTIME(ts.time_end, '24:00:00')
                        ELSE ts.time_end
                    END
                ) * COUNT(DISTINCT DATE(o.order_datetime)) AS total_hours_worked
            FROM Employees e
            JOIN AssignedEmployeesToOrders a ON e.employee_id = a.employee_id
            JOIN Orders o ON a.order_id = o.order_id
            JOIN TimeShifts ts ON e.time_shiftid = ts.time_shiftid
            WHERE o.order_datetime LIKE ?
            GROUP BY e.employee_id, e.first_name, e.last_name
            ORDER BY total_hours_worked DESC
        """;

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, datePattern);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getInt("num_of_shifts"),
                    rs.getInt("total_hours_worked")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error generating employee shifts report: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public void generateProfitMarginReport(String datePattern, javax.swing.table.DefaultTableModel tableModel) {
        String query = """
            SELECT
                o.order_datetime,
                COUNT(*) AS total_orders_with_item,
                SUM(oi.quantity) AS total_amount_ordered,
                SUM(oi.quantity * oi.price_at_time) AS total_revenue,
                SUM(oi.quantity * d.selling_price) AS total_cost,
                SUM(oi.quantity * (oi.price_at_time - d.selling_price)) AS total_profit
            FROM Dishes d
            JOIN OrderItems oi ON d.dish_id = oi.dish_id
            JOIN Orders o ON o.order_id = oi.order_id
            WHERE DATE(o.order_datetime) LIKE ? AND d.is_deleted = FALSE
            GROUP BY d.dish_id, o.order_id, o.order_datetime
            ORDER BY total_profit DESC
        """;

        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, datePattern);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getString("order_datetime"),
                    rs.getInt("total_orders_with_item"),
                    rs.getInt("total_amount_ordered"),
                    rs.getDouble("total_revenue"),
                    rs.getDouble("total_cost"),
                    rs.getDouble("total_profit")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Error generating profit margin report: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public List<Inventory> getLowStockItems(int maxQuantity) {
        List<Inventory> items = new ArrayList<>();
        String query = """
            SELECT i.*, c.category_name 
            FROM InventoryItems i 
            JOIN Categories c ON i.category_id = c.category_id 
            WHERE i.quantity <= ? 
            ORDER BY i.quantity ASC
        """;
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, maxQuantity);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new Inventory(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getString("category_name"),
                        rs.getInt("quantity"),
                        rs.getDouble("make_price"),
                        rs.getDouble("sell_price")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Failed to fetch low stock items: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return items;
    }

    public boolean restockInventory(int productId, int newQuantity, int employeeId) throws SQLException {
        // First check if the employee exists and is authorized
        Employee employee = getEmployeeById(employeeId);
        if (employee == null) {
            throw new SQLException("Invalid employee ID");
        }

        // Get current quantity to verify increase
        Inventory item = getInventoryItemById(productId);
        if (item == null) {
            throw new SQLException("Invalid product ID");
        }

        if (newQuantity <= item.getQuantity()) {
            throw new SQLException("New quantity must be greater than current quantity");
        }

        return inventoryDAO.updateStock(productId, newQuantity);
    }

    public boolean deleteOrder(int orderId) {
        return orderDAO.deleteOrder(orderId);
    }

    // Methods for handling deleted records
    public List<Customer> getDeletedCustomers() {
        return customerDAO.getDeletedCustomers();
    }

    public List<Order> getDeletedOrders() {
        return orderDAO.getDeletedOrders();
    }

    public List<Employee> getDeletedEmployees() {
        return employeeDAO.getDeletedEmployees();
    }

    public List<Supplier> getDeletedSuppliers() {
        return supplierDAO.getDeletedSuppliers();
    }

    public List<Inventory> getDeletedInventoryItems() {
        return inventoryDAO.getDeletedInventoryItems();
    }

    public boolean restoreCustomer(int customerId) {
        return customerDAO.restoreCustomer(customerId);
    }

    public boolean restoreOrder(int orderId) {
        return orderDAO.restoreOrder(orderId);
    }

    public boolean restoreEmployee(int employeeId) {
        return employeeDAO.restoreEmployee(employeeId);
    }

    public boolean restoreSupplier(int supplierId) {
        return supplierDAO.restoreSupplier(supplierId);
    }

    public boolean restoreInventoryItem(int productId) {
        return inventoryDAO.restoreInventoryItem(productId);
    }

    // Ingredient Management Methods
    public List<Ingredient> getAllIngredients() {
        return ingredientDAO.getAllIngredients();
    }

    public List<Ingredient> getLowStockIngredients() {
        return ingredientDAO.getLowStockIngredients();
    }

    public Ingredient getIngredientById(int ingredientId) {
        return ingredientDAO.getIngredientById(ingredientId);
    }

    public boolean addIngredient(Ingredient ingredient) {
        return ingredientDAO.addIngredient(ingredient);
    }

    public boolean updateIngredient(Ingredient ingredient) {
        return ingredientDAO.updateIngredient(ingredient);
    }

    public boolean deleteIngredient(int ingredientId) {
        return ingredientDAO.deleteIngredient(ingredientId);
    }

    public List<IngredientBatch> getAllIngredientBatches() {
        return ingredientBatchDAO.getBatchesByIngredient(-1); // Get all batches
    }

    public List<IngredientBatch> getExpiringBatches(int days) {
        return ingredientBatchDAO.getExpiringBatches(days);
    }

    public IngredientBatch getIngredientBatchById(int batchId) {
        List<IngredientBatch> batches = ingredientBatchDAO.getBatchesByIngredient(-1);
        return batches.stream()
                .filter(b -> b.getBatchId() == batchId)
                .findFirst()
                .orElse(null);
    }

    public boolean addIngredientBatch(IngredientBatch batch) {
        return ingredientBatchDAO.addBatch(batch);
    }

    public boolean updateIngredientBatch(IngredientBatch batch) {
        return ingredientBatchDAO.updateBatchQuantity(batch.getBatchId(), 
            batch.getRemainingQuantity() - batch.getQuantity());
    }

    public Supplier getPrimarySupplierForIngredient(int ingredientId) {
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();
        return suppliers.stream()
                .filter(s -> supplierDAO.getSupplierIngredients(s.getSupplierId()).stream()
                        .anyMatch(i -> i.getProductId() == ingredientId))
                .findFirst()
                .orElse(null);
    }

    // Dish-related methods
    public List<Dish> getAllDishes() {
        return dishDAO.getAllDishes();
    }

    public List<Dish> getDishesByCategory(String category) {
        return dishDAO.getDishesByCategory(category);
    }

    public Dish getDishById(int dishId) {
        return dishDAO.getDishById(dishId);
    }

    public boolean addDish(Dish dish) {
        return dishDAO.addDish(dish);
    }

    public boolean updateDish(Dish dish) {
        return dishDAO.updateDish(dish);
    }

    public boolean deleteDish(int dishId) {
        return dishDAO.deleteDish(dishId);
    }

    public Map<Integer, Double> getDishIngredients(int dishId) {
        return dishDAO.getDishIngredients(dishId);
    }
} 
