package controller;

import dao.*;
import model.*;
import util.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class RestaurantController {
    private final Connection connection;
    private final CustomerDAO customerDAO;
    private final OrderDAO orderDAO;
    private final InventoryDAO inventoryDAO;
    private final EmployeeDAO employeeDAO;
    private final IngredientDAO ingredientDAO;
    private final IngredientBatchDAO ingredientBatchDAO;
    private final SupplierDAO supplierDAO;
    private final DishDAO dishDAO;
    private final LoginManager loginManager;
    private final ShiftManager shiftManager;
    private final ReportManager reportManager;
    private final TableDAO tableDAO;
    private final PaymentDAO paymentDAO;

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
            this.loginManager = new LoginManager(this);
            this.shiftManager = new ShiftManager(this);
            this.reportManager = new ReportManager(this);
            this.tableDAO = new TableDAO();
            this.paymentDAO = new PaymentDAO();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    // Login Management
    public boolean login(String username, String password) throws SQLException {
        return loginManager.login(username, password);
    }

    public void logout() {
        loginManager.logout();
    }

    public boolean isLoggedIn() {
        return loginManager.isLoggedIn();
    }

    public Employee getCurrentUser() {
        return loginManager.getCurrentUser();
    }

    public String getCurrentRole() {
        return loginManager.getCurrentRole();
    }

    public boolean isManager() {
        return loginManager.isManager();
    }

    public boolean checkShiftAccess() throws SQLException {
        return loginManager.checkShiftAccess();
    }

    // Shift Management
    public boolean assignShift(int employeeId, int timeShiftId, java.sql.Date shiftDate) throws SQLException {
        return shiftManager.assignShift(employeeId, timeShiftId, shiftDate);
    }

    public boolean updateShiftStatus(int employeeId, int timeShiftId, java.sql.Date shiftDate, String status) throws SQLException {
        return shiftManager.updateShiftStatus(employeeId, timeShiftId, shiftDate, status);
    }

    public boolean deleteShift(int employeeId, int timeShiftId, java.sql.Date shiftDate) throws SQLException {
        return shiftManager.deleteShift(employeeId, timeShiftId, shiftDate);
    }

    public List<EmployeeShift> getEmployeeShifts(int employeeId, java.sql.Date startDate, java.sql.Date endDate) throws SQLException {
        return shiftManager.getEmployeeShifts(employeeId, startDate, endDate);
    }

    public boolean checkIn(int employeeId, int timeShiftId, java.sql.Date shiftDate) throws SQLException {
        return shiftManager.checkIn(employeeId, timeShiftId, shiftDate);
    }

    public boolean checkOut(int employeeId, int timeShiftId, java.sql.Date shiftDate) throws SQLException {
        return shiftManager.checkOut(employeeId, timeShiftId, shiftDate);
    }

    public List<EmployeeShift> getCurrentShifts() throws SQLException {
        return shiftManager.getCurrentShifts();
    }

    // Enhanced Shift Management
    public boolean assignEmployeeShift(int employeeId, int timeShiftId, java.sql.Date shiftDate) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Check if employee already has a shift on this date
                String checkQuery = """
                    SELECT shift_id FROM EmployeeShifts 
                    WHERE employee_id = ? AND shift_date = ?
                """;
                try (PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
                    stmt.setInt(1, employeeId);
                    stmt.setDate(2, shiftDate);
                    if (stmt.executeQuery().next()) {
                        throw new SQLException("Employee already has a shift assigned for this date");
                    }
                }

                // Assign the shift
                String insertQuery = """
                    INSERT INTO EmployeeShifts 
                    (employee_id, time_shiftid, shift_date, status)
                    VALUES (?, ?, ?, 'Scheduled')
                """;
                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    stmt.setInt(1, employeeId);
                    stmt.setInt(2, timeShiftId);
                    stmt.setDate(3, shiftDate);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Failed to assign shift");
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

    public boolean updateShiftStatus(int employeeId, int timeShiftId, java.sql.Date shiftDate, 
                                  String status, String notes) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Update shift status
                String updateQuery = """
                    UPDATE EmployeeShifts 
                    SET status = ?,
                        check_in = CASE WHEN ? = 'Present' THEN COALESCE(check_in, CURRENT_TIMESTAMP) ELSE check_in END,
                        check_out = CASE WHEN ? = 'Completed' THEN CURRENT_TIMESTAMP ELSE check_out END
                    WHERE employee_id = ? AND time_shiftid = ? AND shift_date = ?
                """;
                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setString(1, status);
                    stmt.setString(2, status);
                    stmt.setString(3, status);
                    stmt.setInt(4, employeeId);
                    stmt.setInt(5, timeShiftId);
                    stmt.setDate(6, shiftDate);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Failed to update shift status");
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

    public List<Map<String, Object>> getEmployeeAttendanceReport(int employeeId, 
                                                              java.sql.Date startDate, 
                                                              java.sql.Date endDate) throws SQLException {
        List<Map<String, Object>> report = new ArrayList<>();
        String query = """
            SELECT es.shift_date, ts.shift_type, ts.time_start, ts.time_end,
                   es.status, es.check_in, es.check_out,
                   TIMESTAMPDIFF(MINUTE, ts.time_start, es.check_in) as minutes_late
            FROM EmployeeShifts es
            JOIN TimeShifts ts ON es.time_shiftid = ts.time_shiftid
            WHERE es.employee_id = ?
            AND es.shift_date BETWEEN ? AND ?
            ORDER BY es.shift_date DESC
        """;
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, employeeId);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("shiftDate", rs.getDate("shift_date"));
                    record.put("shiftType", rs.getString("shift_type"));
                    record.put("timeStart", rs.getTime("time_start"));
                    record.put("timeEnd", rs.getTime("time_end"));
                    record.put("status", rs.getString("status"));
                    record.put("checkIn", rs.getTimestamp("check_in"));
                    record.put("checkOut", rs.getTimestamp("check_out"));
                    record.put("minutesLate", rs.getInt("minutes_late"));
                    report.add(record);
                }
            }
        }
        return report;
    }

    public List<Map<String, Object>> getCurrentShiftEmployees() throws SQLException {
        List<Map<String, Object>> currentEmployees = new ArrayList<>();
        String query = """
            SELECT e.employee_id, e.first_name, e.last_name, r.role_name,
                   ts.shift_type, es.status, es.check_in
            FROM EmployeeShifts es
            JOIN Employees e ON es.employee_id = e.employee_id
            JOIN Roles r ON e.role_id = r.role_id
            JOIN TimeShifts ts ON es.time_shiftid = ts.time_shiftid
            WHERE es.shift_date = CURDATE()
            AND es.status IN ('Scheduled', 'Present', 'Late')
            ORDER BY ts.time_start, e.first_name
        """;
        
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Map<String, Object> employee = new HashMap<>();
                employee.put("employeeId", rs.getInt("employee_id"));
                employee.put("name", rs.getString("first_name") + " " + rs.getString("last_name"));
                employee.put("role", rs.getString("role_name"));
                employee.put("shiftType", rs.getString("shift_type"));
                employee.put("status", rs.getString("status"));
                employee.put("checkIn", rs.getTimestamp("check_in"));
                currentEmployees.add(employee);
            }
        }
        return currentEmployees;
    }

    // Report Management
    public List<Map<String, Object>> getSalesReport(String startDate, String endDate) throws SQLException {
        return reportManager.getSalesReport(startDate, endDate);
    }

    public List<Map<String, Object>> getEmployeePerformanceReport(String startDate, String endDate) throws SQLException {
        return reportManager.getEmployeePerformanceReport(startDate, endDate);
    }

    public List<Map<String, Object>> getCustomerInsightsReport(String startDate, String endDate) throws SQLException {
        return reportManager.getCustomerInsightsReport(startDate, endDate);
    }

    public List<Map<String, Object>> getProfitMarginReport(String startDate, String endDate) throws SQLException {
        return reportManager.getProfitMarginReport(startDate, endDate);
    }

    public List<Map<String, Object>> getInventoryStatusReport() throws SQLException {
        return reportManager.getInventoryStatusReport();
    }

    // Category Management
    public List<String> getAllCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String query = "SELECT category_name FROM Categories WHERE is_deleted = FALSE ORDER BY category_name";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
        }
        return categories;
    }

    public int getCategoryId(String categoryName) throws SQLException {
        String query = "SELECT category_id FROM Categories WHERE category_name = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, categoryName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("category_id");
                }
            }
        }
        return -1;
    }

    // Order Management
    public List<Order> getAllOrders() throws SQLException {
        return orderDAO.getAllOrders();
    }

    public Order getOrderById(int id) throws SQLException {
        return orderDAO.getOrderById(id);
    }

    public boolean createOrder(Order order, List<OrderItem> items, List<Integer> employeeIds) {
        try {
            // Validate table availability for dine-in orders
            if ("Dine-In".equals(order.getOrderType()) && order.getTableId() > 0) {
                if (!tableDAO.isTableAvailable(order.getTableId())) {
                    throw new SQLException("Table is not available");
                }
            }

            order.setItems(items);
            order.setAssignedEmployees(employeeIds);
            int orderId = orderDAO.createOrder(order);
            
            if (orderId > 0) {
                // Update table status for dine-in orders
                if ("Dine-In".equals(order.getOrderType()) && order.getTableId() > 0) {
                    tableDAO.updateTableStatus(order.getTableId(), "Occupied");
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean completeOrder(int orderId) throws SQLException {
        return orderDAO.completeOrder(orderId);
    }

    public boolean cancelOrder(int orderId) throws SQLException {
        return orderDAO.cancelOrder(orderId);
    }

    public List<Order> getOrdersByDateRange(String startDate, String endDate) throws SQLException {
        return orderDAO.getOrdersByDateRange(startDate, endDate);
    }

    public boolean deleteOrder(int orderId) throws SQLException {
        return orderDAO.deleteOrder(orderId);
    }

    public List<Order> getDeletedOrders() throws SQLException {
        return orderDAO.getDeletedOrders();
    }

    public boolean updatePaymentStatus(int orderId, String paymentMethod, String paymentStatus) throws SQLException {
        return orderDAO.updatePaymentStatus(orderId, paymentMethod, paymentStatus);
    }

    // Customer Management
    public List<Customer> getAllCustomers() throws SQLException {
        return customerDAO.getAllCustomers();
    }

    public Customer getCustomerById(int id) throws SQLException {
        return customerDAO.getCustomerById(id);
    }

    public int addCustomer(Customer customer) throws SQLException {
        return customerDAO.addCustomer(customer);
    }

    public boolean updateCustomer(Customer customer) throws SQLException {
        return customerDAO.updateCustomer(customer);
    }

    public boolean deleteCustomer(int customerId) throws SQLException {
        return customerDAO.deleteCustomer(customerId);
    }

    public List<Order> getCustomerOrders(int customerId) throws SQLException {
        return orderDAO.getOrdersByCustomerId(customerId);
    }

    // Employee Management
    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees();
    }

    public Employee getEmployeeById(int employeeId) {
        return employeeDAO.getEmployeeById(employeeId);
    }

    public Employee getEmployeeByName(String firstName, String lastName) {
        return employeeDAO.getEmployeeByName(firstName, lastName);
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

    public boolean assignShift(int employeeId, int timeShiftId) {
        return employeeDAO.assignShift(employeeId, timeShiftId);
    }

    public boolean removeShift(int employeeId) {
        return employeeDAO.removeShift(employeeId);
    }

    public String getCurrentShift(int employeeId) {
        return employeeDAO.getCurrentShift(employeeId);
    }

    // Supplier Management
    public List<Supplier> getAllSuppliers() {
        return supplierDAO.getAllSuppliers();
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
        return supplierDAO.deleteSupplier(supplierId);
    }

    public List<Inventory> getSupplierIngredients(int supplierId) {
        return supplierDAO.getSupplierIngredients(supplierId);
    }

    public Supplier getPrimarySupplierForIngredient(int ingredientId) {
        List<Supplier> suppliers = supplierDAO.getAllSuppliers();
        return suppliers.stream()
                .filter(s -> supplierDAO.getSupplierIngredients(s.getSupplierId()).stream()
                        .anyMatch(i -> i.getProductId() == ingredientId))
                .findFirst()
                .orElse(null);
    }

    // Inventory Management
    public List<Inventory> getAllInventoryItems() throws SQLException {
        return inventoryDAO.getAllInventoryItems();
    }

    public List<Inventory> getInventoryItemsByCategory(String categoryName) throws SQLException {
        return inventoryDAO.getInventoryItemsByCategory(categoryName);
    }

    public Inventory getInventoryItemById(int id) throws SQLException {
        return inventoryDAO.getInventoryItemById(id);
    }

    public boolean addInventoryItem(String name, String categoryName, double makePrice, 
                                  double sellPrice, int quantity, String recipe, int employeeId) throws SQLException {
        return inventoryDAO.addInventoryItem(name, categoryName, makePrice, sellPrice, quantity, recipe, employeeId);
    }

    public boolean updateInventoryItem(int id, String name, String categoryName, 
                                     double makePrice, double sellPrice, 
                                     int quantity, String recipe) throws SQLException {
        return inventoryDAO.updateInventoryItem(id, name, categoryName, makePrice, sellPrice, quantity, recipe);
    }

    public boolean deleteInventoryItem(int id) throws SQLException {
        return inventoryDAO.deleteInventoryItem(id);
    }

    public boolean restockInventory(int productId, int newQuantity, int employeeId) throws SQLException {
        return inventoryDAO.restockInventory(productId, newQuantity, employeeId);
    }

    public boolean updateInventoryStock(int ingredientId, double quantityChange, int employeeId, 
                                     String transactionType, String notes) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Get current stock
                String stockQuery = "SELECT quantity_in_stock FROM Ingredients WHERE ingredient_id = ?";
                double currentStock;
                try (PreparedStatement stmt = conn.prepareStatement(stockQuery)) {
                    stmt.setInt(1, ingredientId);
                    ResultSet rs = stmt.executeQuery();
                    if (!rs.next()) {
                        throw new SQLException("Ingredient not found");
                    }
                    currentStock = rs.getDouble("quantity_in_stock");
                }

                // Update stock
                String updateQuery = """
                    UPDATE Ingredients 
                    SET quantity_in_stock = quantity_in_stock + ?,
                        last_restocked_by = ?,
                        last_restock_date = CURRENT_TIMESTAMP
                    WHERE ingredient_id = ?
                """;
                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setDouble(1, quantityChange);
                    stmt.setInt(2, employeeId);
                    stmt.setInt(3, ingredientId);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Failed to update stock");
                    }
                }

                // Record transaction
                String transactionQuery = """
                    INSERT INTO IngredientTransactions 
                    (ingredient_id, transaction_type, quantity_change, employee_id, notes)
                    VALUES (?, ?, ?, ?, ?)
                """;
                try (PreparedStatement stmt = conn.prepareStatement(transactionQuery)) {
                    stmt.setInt(1, ingredientId);
                    stmt.setString(2, transactionType);
                    stmt.setDouble(3, quantityChange);
                    stmt.setInt(4, employeeId);
                    stmt.setString(5, notes);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Failed to record transaction");
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

    public List<Map<String, Object>> getInventoryAlerts() throws SQLException {
        List<Map<String, Object>> alerts = new ArrayList<>();
        String query = """
            SELECT i.ingredient_id, i.name, i.quantity_in_stock, i.minimum_stock_level,
                   s.name as supplier_name, s.contact_person, s.phone
            FROM Ingredients i
            JOIN IngredientSuppliers is ON i.ingredient_id = is.ingredient_id
            JOIN Suppliers s ON is.supplier_id = s.supplier_id
            WHERE i.quantity_in_stock <= i.minimum_stock_level
            AND i.is_deleted = FALSE
            AND is.is_primary_supplier = TRUE
            ORDER BY (i.quantity_in_stock / i.minimum_stock_level) ASC
        """;
        
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("ingredientId", rs.getInt("ingredient_id"));
                alert.put("name", rs.getString("name"));
                alert.put("currentStock", rs.getDouble("quantity_in_stock"));
                alert.put("minimumStock", rs.getDouble("minimum_stock_level"));
                alert.put("supplierName", rs.getString("supplier_name"));
                alert.put("contactPerson", rs.getString("contact_person"));
                alert.put("phone", rs.getString("phone"));
                alerts.add(alert);
            }
        }
        return alerts;
    }

    public List<Map<String, Object>> getExpiringInventory(int daysThreshold) throws SQLException {
        List<Map<String, Object>> expiringItems = new ArrayList<>();
        String query = """
            SELECT ib.batch_id, i.name, ib.remaining_quantity, ib.expiry_date,
                   s.name as supplier_name
            FROM IngredientBatches ib
            JOIN Ingredients i ON ib.ingredient_id = i.ingredient_id
            JOIN Suppliers s ON ib.supplier_id = s.supplier_id
            WHERE ib.expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY)
            AND ib.status = 'Available'
            ORDER BY ib.expiry_date ASC
        """;
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, daysThreshold);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("batchId", rs.getInt("batch_id"));
                    item.put("name", rs.getString("name"));
                    item.put("remainingQuantity", rs.getDouble("remaining_quantity"));
                    item.put("expiryDate", rs.getDate("expiry_date"));
                    item.put("supplierName", rs.getString("supplier_name"));
                    expiringItems.add(item);
                }
            }
        }
        return expiringItems;
    }

    // Ingredient Management
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

    public List<IngredientBatch> getAllIngredientBatches() throws SQLException {
        return ingredientBatchDAO.getBatchesByIngredient(-1);
    }

    public List<IngredientBatch> getExpiringBatches(int days) throws SQLException {
        return ingredientBatchDAO.getExpiringBatches(days);
    }

    public IngredientBatch getIngredientBatchById(int batchId) throws SQLException {
        return ingredientBatchDAO.getBatchById(batchId);
    }

    public boolean addIngredientBatch(IngredientBatch batch) throws SQLException {
        return ingredientBatchDAO.addBatch(batch);
    }

    public boolean updateIngredientBatch(IngredientBatch batch) throws SQLException {
        return ingredientBatchDAO.updateBatchQuantity(batch.getBatchId(), 
            batch.getRemainingQuantity() - batch.getQuantity());
    }

    // Dish Management
    public List<Dish> getAllDishes() throws SQLException {
        return dishDAO.getAllDishes();
    }

    public List<Dish> getDishesByCategory(String category) throws SQLException {
        return dishDAO.getDishesByCategory(category);
    }

    public Dish getDishById(int dishId) throws SQLException {
        return dishDAO.getDishById(dishId);
    }

    public boolean addDish(Dish dish) throws SQLException {
        return dishDAO.createDish(dish) > 0;
    }

    public boolean updateDish(Dish dish) throws SQLException {
        return dishDAO.updateDish(dish);
    }

    public boolean deleteDish(int dishId) throws SQLException {
        return dishDAO.deleteDish(dishId);
    }

    public List<DishIngredient> getDishIngredients(int dishId) throws SQLException {
        return dishDAO.getDishIngredients(dishId);
    }

    // Deleted Records Management
    public List<Customer> getDeletedCustomers() throws SQLException {
        return customerDAO.getDeletedCustomers();
    }

    public boolean restoreCustomer(int customerId) throws SQLException {
        return customerDAO.restoreCustomer(customerId);
    }

    public boolean restoreOrder(int orderId) throws SQLException {
        return orderDAO.restoreOrder(orderId);
    }

    public boolean restoreEmployee(int employeeId) {
        return employeeDAO.restoreEmployee(employeeId);
    }

    public boolean restoreSupplier(int supplierId) {
        return supplierDAO.restoreSupplier(supplierId);
    }

    public boolean restoreInventoryItem(int productId) throws SQLException {
        return inventoryDAO.restoreInventoryItem(productId);
    }

    // Unit Management
    public List<String> getAllUnits() throws SQLException {
        List<String> units = new ArrayList<>();
        String query = "SELECT unit_name FROM Units WHERE is_deleted = FALSE ORDER BY unit_name";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                units.add(rs.getString("unit_name"));
            }
        }
        return units;
    }

    public int getUnitId(String unitName) throws SQLException {
        String query = "SELECT unit_id FROM Units WHERE unit_name = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, unitName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("unit_id");
                }
            }
        }
        return -1;
    }

    // Table Management
    public int addTable(int tableNumber, int capacity) throws SQLException {
        Table table = new Table(0, tableNumber, capacity, "Available", false);
        return tableDAO.createTable(table);
    }

    public Table getTable(int tableId) throws SQLException {
        return tableDAO.getTableById(tableId);
    }

    public List<Table> getAllTables() throws SQLException {
        return tableDAO.getAllTables();
    }

    public List<Table> getAvailableTables() throws SQLException {
        return tableDAO.getAvailableTables();
    }

    public boolean updateTable(Table table) throws SQLException {
        return tableDAO.updateTable(table);
    }

    public boolean updateTableStatus(int tableId, String status) throws SQLException {
        return tableDAO.updateTableStatus(tableId, status);
    }

    public boolean deleteTable(int tableId) throws SQLException {
        return tableDAO.deleteTable(tableId);
    }

    public boolean isTableAvailable(int tableId) throws SQLException {
        return tableDAO.isTableAvailable(tableId);
    }

    // Payment Processing
    public boolean processPayment(int orderId, double amountReceived, String paymentMethod) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // First check if the order exists and is pending payment
                String checkQuery = """
                    SELECT payment_status, order_status, total_amount 
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
                    double totalAmount = rs.getDouble("total_amount");
                    if (amountReceived < totalAmount) {
                        throw new SQLException("Insufficient payment amount");
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

                // If it's a dine-in order, update table status
                Order order = orderDAO.getOrderById(orderId);
                if ("Dine-In".equals(order.getOrderType()) && order.getTableId() > 0) {
                    tableDAO.updateTableStatus(order.getTableId(), "Available");
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public boolean refundPayment(int orderId, String reason) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Check if order exists and is paid
                String checkQuery = "SELECT payment_status FROM Orders WHERE order_id = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                    checkStmt.setInt(1, orderId);
                    ResultSet rs = checkStmt.executeQuery();
                    if (!rs.next() || !"Paid".equals(rs.getString("payment_status"))) {
                        throw new SQLException("Order is not eligible for refund");
                    }
                }

                // Update order payment status
                String updateQuery = """
                    UPDATE Orders 
                    SET payment_status = 'Refunded',
                        payment_method = NULL
                    WHERE order_id = ?
                """;
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, orderId);
                    if (updateStmt.executeUpdate() != 1) {
                        throw new SQLException("Failed to process refund");
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

    // Customer Loyalty Program
    public boolean addLoyaltyPoints(int customerId, int points, int orderId, String notes) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Add points to customer's balance
                String updateQuery = """
                    UPDATE CustomerLoyalty 
                    SET points_balance = points_balance + ?,
                        points_earned_lifetime = points_earned_lifetime + ?,
                        last_points_earned = CURRENT_TIMESTAMP
                    WHERE customer_id = ?
                """;
                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setInt(1, points);
                    stmt.setInt(2, points);
                    stmt.setInt(3, customerId);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Failed to update points balance");
                    }
                }

                // Record the transaction
                String transactionQuery = """
                    INSERT INTO LoyaltyTransactions 
                    (customer_id, transaction_type, points_amount, order_id, notes)
                    VALUES (?, 'Earn', ?, ?, ?)
                """;
                try (PreparedStatement stmt = conn.prepareStatement(transactionQuery)) {
                    stmt.setInt(1, customerId);
                    stmt.setInt(2, points);
                    stmt.setInt(3, orderId);
                    stmt.setString(4, notes);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Failed to record transaction");
                    }
                }

                // Check and update tier if necessary
                updateCustomerTier(customerId);

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public boolean redeemReward(int customerId, int rewardId) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Get reward details
                String rewardDetailsQuery = "SELECT points_cost, tier_requirement FROM LoyaltyRewards WHERE reward_id = ?";
                int pointsCost;
                String tierRequirement;
                try (PreparedStatement stmt = conn.prepareStatement(rewardDetailsQuery)) {
                    stmt.setInt(1, rewardId);
                    ResultSet rs = stmt.executeQuery();
                    if (!rs.next()) {
                        throw new SQLException("Reward not found");
                    }
                    pointsCost = rs.getInt("points_cost");
                    tierRequirement = rs.getString("tier_requirement");
                }

                // Check customer's tier and points
                String customerQuery = "SELECT points_balance, tier FROM CustomerLoyalty WHERE customer_id = ?";
                int pointsBalance;
                String currentTier;
                try (PreparedStatement stmt = conn.prepareStatement(customerQuery)) {
                    stmt.setInt(1, customerId);
                    ResultSet rs = stmt.executeQuery();
                    if (!rs.next()) {
                        throw new SQLException("Customer not found");
                    }
                    pointsBalance = rs.getInt("points_balance");
                    currentTier = rs.getString("tier");
                }

                // Validate tier requirement
                if (!isTierSufficient(currentTier, tierRequirement)) {
                    throw new SQLException("Customer tier is insufficient for this reward");
                }

                // Check points balance
                if (pointsBalance < pointsCost) {
                    throw new SQLException("Insufficient points balance");
                }

                // Deduct points
                String updateQuery = """
                    UPDATE CustomerLoyalty 
                    SET points_balance = points_balance - ?,
                        points_redeemed_lifetime = points_redeemed_lifetime + ?,
                        last_points_redeemed = CURRENT_TIMESTAMP
                    WHERE customer_id = ?
                """;
                try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
                    stmt.setInt(1, pointsCost);
                    stmt.setInt(2, pointsCost);
                    stmt.setInt(3, customerId);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Failed to update points balance");
                    }
                }

                // Record the transaction
                String transactionQuery = """
                    INSERT INTO LoyaltyTransactions 
                    (customer_id, transaction_type, points_amount, notes)
                    VALUES (?, 'Redeem', ?, 'Redeemed reward')
                """;
                try (PreparedStatement stmt = conn.prepareStatement(transactionQuery)) {
                    stmt.setInt(1, customerId);
                    stmt.setInt(2, pointsCost);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Failed to record transaction");
                    }
                }

                // Add reward to customer's available rewards
                String customerRewardQuery = """
                    INSERT INTO CustomerRewards 
                    (customer_id, reward_id, expiry_date)
                    VALUES (?, ?, DATE_ADD(CURDATE(), INTERVAL 30 DAY))
                """;
                try (PreparedStatement stmt = conn.prepareStatement(customerRewardQuery)) {
                    stmt.setInt(1, customerId);
                    stmt.setInt(2, rewardId);
                    if (stmt.executeUpdate() != 1) {
                        throw new SQLException("Failed to add reward to customer");
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

    private void updateCustomerTier(int customerId) throws SQLException {
        String query = """
            UPDATE CustomerLoyalty cl
            SET tier = CASE
                WHEN points_earned_lifetime >= 10000 THEN 'Platinum'
                WHEN points_earned_lifetime >= 5000 THEN 'Gold'
                WHEN points_earned_lifetime >= 2000 THEN 'Silver'
                ELSE 'Bronze'
            END
            WHERE customer_id = ?
        """;
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
        }
    }

    private boolean isTierSufficient(String currentTier, String requiredTier) {
        Map<String, Integer> tierLevels = Map.of(
            "Bronze", 1,
            "Silver", 2,
            "Gold", 3,
            "Platinum", 4
        );
        return tierLevels.get(currentTier) >= tierLevels.get(requiredTier);
    }

    public List<Map<String, Object>> getCustomerLoyaltyStatus(int customerId) throws SQLException {
        List<Map<String, Object>> status = new ArrayList<>();
        String query = """
            SELECT cl.*, 
                   COUNT(DISTINCT cr.reward_id) as available_rewards,
                   COUNT(DISTINCT lt.transaction_id) as total_transactions
            FROM CustomerLoyalty cl
            LEFT JOIN CustomerRewards cr ON cl.customer_id = cr.customer_id AND cr.status = 'Available'
            LEFT JOIN LoyaltyTransactions lt ON cl.customer_id = lt.customer_id
            WHERE cl.customer_id = ?
            GROUP BY cl.customer_id
        """;
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("pointsBalance", rs.getInt("points_balance"));
                    data.put("tier", rs.getString("tier"));
                    data.put("pointsEarnedLifetime", rs.getInt("points_earned_lifetime"));
                    data.put("pointsRedeemedLifetime", rs.getInt("points_redeemed_lifetime"));
                    data.put("lastPointsEarned", rs.getTimestamp("last_points_earned"));
                    data.put("lastPointsRedeemed", rs.getTimestamp("last_points_redeemed"));
                    data.put("availableRewards", rs.getInt("available_rewards"));
                    data.put("totalTransactions", rs.getInt("total_transactions"));
                    status.add(data);
                }
            }
        }
        return status;
    }

    public List<Map<String, Object>> getAvailableRewards(int customerId) throws SQLException {
        List<Map<String, Object>> rewards = new ArrayList<>();
        String query = """
            SELECT r.*, cr.status, cr.expiry_date
            FROM LoyaltyRewards r
            LEFT JOIN CustomerRewards cr ON r.reward_id = cr.reward_id AND cr.customer_id = ?
            WHERE r.is_active = TRUE
            ORDER BY r.points_cost
        """;
        
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> reward = new HashMap<>();
                    reward.put("rewardId", rs.getInt("reward_id"));
                    reward.put("name", rs.getString("name"));
                    reward.put("description", rs.getString("description"));
                    reward.put("pointsCost", rs.getInt("points_cost"));
                    reward.put("tierRequirement", rs.getString("tier_requirement"));
                    reward.put("status", rs.getString("status"));
                    reward.put("expiryDate", rs.getDate("expiry_date"));
                    rewards.add(reward);
                }
            }
        }
        return rewards;
    }

    // DAO Getters
    public OrderDAO getOrderDAO() {
        return orderDAO;
    }

    public EmployeeDAO getEmployeeDAO() {
        return employeeDAO;
    }

    public CustomerDAO getCustomerDAO() {
        return customerDAO;
    }

    public PaymentDAO getPaymentDAO() {
        return paymentDAO;
    }

    public DishDAO getDishDAO() {
        return dishDAO;
    }

    public IngredientDAO getIngredientDAO() {
        return ingredientDAO;
    }

    public double calculateOrderTotal(int orderId) throws SQLException {
        Order order = orderDAO.getOrderById(orderId);
        if (order == null) {
            throw new SQLException("Order not found");
        }

        double total = 0.0;
        for (OrderItem item : order.getItems()) {
            total += item.getQuantity() * item.getPriceAtTime();
        }
        return total;
    }

    public List<Inventory> getLowStockItems(int threshold) throws SQLException {
        return inventoryDAO.getLowStockItems(threshold);
    }
} 
