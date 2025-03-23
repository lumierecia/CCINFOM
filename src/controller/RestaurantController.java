package controller;

import dao.OrderDAO;
import dao.CustomerDAO;
import dao.EmployeeDAO;
import dao.IngredientDAO;
import dao.SupplierDAO;
import dao.InventoryDAO;
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

public class RestaurantController {
    private final Connection connection;
    private final CustomerDAO customerDAO;
    private final OrderDAO orderDAO;
    private final InventoryDAO inventoryDAO;
    private final EmployeeDAO employeeDAO;
    private final IngredientDAO ingredientDAO;
    private SupplierDAO supplierDAO;

    public RestaurantController() {
        try {
            this.connection = DatabaseConnection.getConnection();
            this.customerDAO = new CustomerDAO();
            this.orderDAO = new OrderDAO();
            this.inventoryDAO = new InventoryDAO();
            this.employeeDAO = new EmployeeDAO();
            this.ingredientDAO = new IngredientDAO();
            this.supplierDAO = new SupplierDAO();
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
        String query = "SELECT category_name FROM Categories ORDER BY category_name";
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to fetch categories: " + e.getMessage(),
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
                SUM(oi.quantity * i.sell_price) AS total_sales,
                AVG(oi.quantity * i.sell_price) AS average_sales,
                i.product_name AS top_product,
                SUM(oi.quantity) AS product_sold
            FROM Orders o
            JOIN OrderItems oi ON o.order_id = oi.order_id
            JOIN InventoryItems i ON oi.product_id = i.product_id
            WHERE DATE(o.order_datetime) LIKE ?
            GROUP BY sales_date, i.product_name
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
                SUM(oi.quantity * i.sell_price) AS total_amount_spent,
                i.product_name AS most_bought_product,
                SUM(oi.quantity) AS most_bought_quantity
            FROM Orders o
            JOIN OrderItems oi ON o.order_id = oi.order_id
            JOIN InventoryItems i ON oi.product_id = i.product_id
            WHERE DATE(o.order_datetime) LIKE ?
            GROUP BY i.product_name
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
                SUM(oi.quantity) * i.sell_price AS total_revenue,
                SUM(oi.quantity) * i.make_price AS total_cost,
                (SUM(oi.quantity) * i.sell_price) - (SUM(oi.quantity) * i.make_price) AS total_profit
            FROM InventoryItems i
            JOIN OrderItems oi ON i.product_id = oi.product_id
            JOIN Orders o ON o.order_id = oi.order_id
            WHERE DATE(o.order_datetime) LIKE ?
            GROUP BY i.product_id, o.order_id, o.order_datetime
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
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT i.*, u.unit_name FROM Ingredients i " +
                      "JOIN Units u ON i.unit_id = u.unit_id " +
                      "WHERE i.is_deleted = FALSE";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setIngredientId(rs.getInt("ingredient_id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setUnitId(rs.getInt("unit_id"));
                ingredient.setUnitName(rs.getString("unit_name"));
                ingredient.setQuantityInStock(rs.getDouble("quantity_in_stock"));
                ingredient.setMinimumStockLevel(rs.getDouble("minimum_stock_level"));
                ingredient.setCostPerUnit(rs.getDouble("cost_per_unit"));
                Timestamp timestamp = rs.getTimestamp("last_restock_date");
                if (timestamp != null) {
                    ingredient.setLastRestockDate(new Date(timestamp.getTime()));
                }
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients;
    }

    public List<Ingredient> getLowStockIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String query = "SELECT i.*, u.unit_name FROM Ingredients i " +
                      "JOIN Units u ON i.unit_id = u.unit_id " +
                      "WHERE i.quantity_in_stock <= i.minimum_stock_level " +
                      "AND i.is_deleted = FALSE";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setIngredientId(rs.getInt("ingredient_id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setUnitId(rs.getInt("unit_id"));
                ingredient.setUnitName(rs.getString("unit_name"));
                ingredient.setQuantityInStock(rs.getDouble("quantity_in_stock"));
                ingredient.setMinimumStockLevel(rs.getDouble("minimum_stock_level"));
                ingredient.setCostPerUnit(rs.getDouble("cost_per_unit"));
                Timestamp timestamp = rs.getTimestamp("last_restock_date");
                if (timestamp != null) {
                    ingredient.setLastRestockDate(new Date(timestamp.getTime()));
                }
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients;
    }

    public List<IngredientBatch> getAllBatches() {
        List<IngredientBatch> batches = new ArrayList<>();
        String query = "SELECT b.*, i.name as ingredient_name, s.name as supplier_name " +
                      "FROM IngredientBatches b " +
                      "JOIN Ingredients i ON b.ingredient_id = i.ingredient_id " +
                      "JOIN Suppliers s ON b.supplier_id = s.supplier_id " +
                      "WHERE b.status != 'Depleted'";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                IngredientBatch batch = new IngredientBatch();
                batch.setBatchId(rs.getInt("batch_id"));
                batch.setIngredientId(rs.getInt("ingredient_id"));
                batch.setIngredientName(rs.getString("ingredient_name"));
                batch.setSupplierId(rs.getInt("supplier_id"));
                batch.setSupplierName(rs.getString("supplier_name"));
                batch.setQuantity(rs.getDouble("quantity"));
                batch.setRemainingQuantity(rs.getDouble("remaining_quantity"));
                
                Timestamp purchaseTimestamp = rs.getTimestamp("purchase_date");
                if (purchaseTimestamp != null) {
                    batch.setPurchaseDate(new Date(purchaseTimestamp.getTime()));
                }
                
                Date expiryDate = rs.getDate("expiry_date");
                if (expiryDate != null) {
                    batch.setExpiryDate(new Date(expiryDate.getTime()));
                }
                
                batch.setPurchasePrice(rs.getDouble("purchase_price"));
                batch.setStatus(rs.getString("status"));
                batches.add(batch);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return batches;
    }

    public List<IngredientBatch> getExpiringBatches(int daysThreshold) {
        List<IngredientBatch> batches = new ArrayList<>();
        String query = "SELECT b.*, i.name as ingredient_name, s.name as supplier_name " +
                      "FROM IngredientBatches b " +
                      "JOIN Ingredients i ON b.ingredient_id = i.ingredient_id " +
                      "JOIN Suppliers s ON b.supplier_id = s.supplier_id " +
                      "WHERE b.expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                      "AND b.status != 'Depleted' AND b.remaining_quantity > 0";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, daysThreshold);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                IngredientBatch batch = new IngredientBatch();
                batch.setBatchId(rs.getInt("batch_id"));
                batch.setIngredientId(rs.getInt("ingredient_id"));
                batch.setIngredientName(rs.getString("ingredient_name"));
                batch.setSupplierId(rs.getInt("supplier_id"));
                batch.setSupplierName(rs.getString("supplier_name"));
                batch.setQuantity(rs.getDouble("quantity"));
                batch.setRemainingQuantity(rs.getDouble("remaining_quantity"));
                
                Timestamp purchaseTimestamp = rs.getTimestamp("purchase_date");
                if (purchaseTimestamp != null) {
                    batch.setPurchaseDate(new Date(purchaseTimestamp.getTime()));
                }
                
                Date expiryDate = rs.getDate("expiry_date");
                if (expiryDate != null) {
                    batch.setExpiryDate(new Date(expiryDate.getTime()));
                }
                
                batch.setPurchasePrice(rs.getDouble("purchase_price"));
                batch.setStatus(rs.getString("status"));
                batches.add(batch);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return batches;
    }

    public void addIngredient(Ingredient ingredient) throws SQLException {
        String query = "INSERT INTO Ingredients (name, unit_id, quantity_in_stock, minimum_stock_level, cost_per_unit, last_restock_date, last_restocked_by, is_deleted) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, ingredient.getName());
            stmt.setInt(2, ingredient.getUnitId());
            stmt.setDouble(3, ingredient.getQuantityInStock());
            stmt.setDouble(4, ingredient.getMinimumStockLevel());
            stmt.setDouble(5, ingredient.getCostPerUnit());
            stmt.setTimestamp(6, new Timestamp(ingredient.getLastRestockDate().getTime()));
            stmt.setInt(7, ingredient.getLastRestockedBy());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    ingredient.setIngredientId(rs.getInt(1));
                }
            }
        }
    }

    public void updateIngredient(Ingredient ingredient) throws SQLException {
        String query = "UPDATE Ingredients SET name = ?, unit_id = ?, quantity_in_stock = ?, " +
                      "minimum_stock_level = ?, cost_per_unit = ?, last_restock_date = ?, " +
                      "last_restocked_by = ? WHERE ingredient_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, ingredient.getName());
            stmt.setInt(2, ingredient.getUnitId());
            stmt.setDouble(3, ingredient.getQuantityInStock());
            stmt.setDouble(4, ingredient.getMinimumStockLevel());
            stmt.setDouble(5, ingredient.getCostPerUnit());
            stmt.setTimestamp(6, new Timestamp(ingredient.getLastRestockDate().getTime()));
            stmt.setInt(7, ingredient.getLastRestockedBy());
            stmt.setInt(8, ingredient.getIngredientId());
            
            stmt.executeUpdate();
        }
    }

    public void deleteIngredient(int ingredientId) throws SQLException {
        String query = "UPDATE Ingredients SET is_deleted = 1 WHERE ingredient_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, ingredientId);
            stmt.executeUpdate();
        }
    }

    public void addIngredientBatch(IngredientBatch batch) throws SQLException {
        String query = "INSERT INTO IngredientBatches (ingredient_id, supplier_id, quantity, purchase_date, " +
                      "expiry_date, purchase_price, remaining_quantity, status) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, batch.getIngredientId());
            stmt.setInt(2, batch.getSupplierId());
            stmt.setDouble(3, batch.getQuantity());
            stmt.setTimestamp(4, new Timestamp(batch.getPurchaseDate().getTime()));
            stmt.setTimestamp(5, new Timestamp(batch.getExpiryDate().getTime()));
            stmt.setDouble(6, batch.getPurchasePrice());
            stmt.setDouble(7, batch.getRemainingQuantity());
            stmt.setString(8, batch.getStatus());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    batch.setBatchId(rs.getInt(1));
                }
            }

            // Update ingredient stock level
            updateIngredientStock(batch.getIngredientId());
        }
    }

    public void updateIngredientBatch(IngredientBatch batch) throws SQLException {
        String query = "UPDATE IngredientBatches SET quantity = ?, purchase_date = ?, expiry_date = ?, " +
                      "purchase_price = ?, remaining_quantity = ?, status = ? WHERE batch_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDouble(1, batch.getQuantity());
            stmt.setTimestamp(2, new Timestamp(batch.getPurchaseDate().getTime()));
            stmt.setTimestamp(3, new Timestamp(batch.getExpiryDate().getTime()));
            stmt.setDouble(4, batch.getPurchasePrice());
            stmt.setDouble(5, batch.getRemainingQuantity());
            stmt.setString(6, batch.getStatus());
            stmt.setInt(7, batch.getBatchId());
            
            stmt.executeUpdate();

            // Update ingredient stock level
            updateIngredientStock(batch.getIngredientId());
        }
    }

    private void updateIngredientStock(int ingredientId) throws SQLException {
        String query = "UPDATE Ingredients i " +
                      "SET quantity_in_stock = (SELECT COALESCE(SUM(remaining_quantity), 0) " +
                      "                        FROM IngredientBatches b " +
                      "                        WHERE b.ingredient_id = i.ingredient_id " +
                      "                        AND b.status = 'ACTIVE'), " +
                      "    last_restock_date = CURRENT_TIMESTAMP " +
                      "WHERE i.ingredient_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, ingredientId);
            stmt.executeUpdate();
        }
    }
} 