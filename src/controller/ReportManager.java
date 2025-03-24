package controller;

import util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ReportManager {
    private final RestaurantController controller;

    public ReportManager(RestaurantController controller) {
        this.controller = controller;
    }

    // Sales Report
    public List<Map<String, Object>> getSalesReport(String startDate, String endDate) throws SQLException {
        List<Map<String, Object>> report = new ArrayList<>();
        String query = """
            SELECT 
                DATE_FORMAT(o.order_datetime, '%Y-%m') as month,
                COUNT(DISTINCT o.order_id) as total_orders,
                SUM(o.total_amount) as total_sales,
                AVG(o.total_amount) as average_sale,
                COUNT(DISTINCT o.customer_id) as unique_customers
            FROM Orders o
            WHERE o.is_deleted = FALSE
            AND o.order_datetime BETWEEN ? AND ?
            GROUP BY DATE_FORMAT(o.order_datetime, '%Y-%m')
            ORDER BY month
        """;

        try (Connection conn = controller.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("month", rs.getString("month"));
                    row.put("total_orders", rs.getInt("total_orders"));
                    row.put("total_sales", rs.getDouble("total_sales"));
                    row.put("average_sale", rs.getDouble("average_sale"));
                    row.put("unique_customers", rs.getInt("unique_customers"));
                    report.add(row);
                }
            }
        }
        return report;
    }

    // Employee Performance Report
    public List<Map<String, Object>> getEmployeePerformanceReport(String startDate, String endDate) throws SQLException {
        List<Map<String, Object>> report = new ArrayList<>();
        String query = """
            SELECT 
                e.employee_id,
                CONCAT(e.first_name, ' ', e.last_name) as employee_name,
                COUNT(DISTINCT o.order_id) as orders_handled,
                SUM(o.total_amount) as total_sales,
                COUNT(DISTINCT es.shift_id) as shifts_worked,
                COUNT(CASE WHEN es.status = 'Late' THEN 1 END) as late_shifts
            FROM Employees e
            LEFT JOIN AssignedEmployeesToOrders aeto ON e.employee_id = aeto.employee_id
            LEFT JOIN Orders o ON aeto.order_id = o.order_id
            LEFT JOIN EmployeeShifts es ON e.employee_id = es.employee_id
            WHERE e.is_deleted = FALSE
            AND (o.order_datetime BETWEEN ? AND ? OR o.order_datetime IS NULL)
            AND (es.shift_date BETWEEN ? AND ? OR es.shift_date IS NULL)
            GROUP BY e.employee_id
            ORDER BY total_sales DESC
        """;

        try (Connection conn = controller.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);
            stmt.setString(3, startDate);
            stmt.setString(4, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("employee_id", rs.getInt("employee_id"));
                    row.put("employee_name", rs.getString("employee_name"));
                    row.put("orders_handled", rs.getInt("orders_handled"));
                    row.put("total_sales", rs.getDouble("total_sales"));
                    row.put("shifts_worked", rs.getInt("shifts_worked"));
                    row.put("late_shifts", rs.getInt("late_shifts"));
                    report.add(row);
                }
            }
        }
        return report;
    }

    // Customer Insights Report
    public List<Map<String, Object>> getCustomerInsightsReport(String startDate, String endDate) throws SQLException {
        List<Map<String, Object>> report = new ArrayList<>();
        String query = """
            SELECT 
                DATE_FORMAT(o.order_datetime, '%Y-%m') as month,
                COUNT(DISTINCT CASE WHEN o.customer_id IN (
                    SELECT customer_id 
                    FROM Orders 
                    GROUP BY customer_id 
                    HAVING COUNT(*) > 1
                ) THEN o.customer_id END) as returning_customers,
                COUNT(DISTINCT CASE WHEN o.customer_id NOT IN (
                    SELECT customer_id 
                    FROM Orders 
                    GROUP BY customer_id 
                    HAVING COUNT(*) > 1
                ) THEN o.customer_id END) as new_customers,
                AVG(o.total_amount) as average_spending,
                HOUR(o.order_datetime) as order_hour,
                COUNT(*) as orders_per_hour
            FROM Orders o
            WHERE o.is_deleted = FALSE
            AND o.order_datetime BETWEEN ? AND ?
            GROUP BY DATE_FORMAT(o.order_datetime, '%Y-%m'), HOUR(o.order_datetime)
            ORDER BY month, order_hour
        """;

        try (Connection conn = controller.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("month", rs.getString("month"));
                    row.put("returning_customers", rs.getInt("returning_customers"));
                    row.put("new_customers", rs.getInt("new_customers"));
                    row.put("average_spending", rs.getDouble("average_spending"));
                    row.put("order_hour", rs.getInt("order_hour"));
                    row.put("orders_per_hour", rs.getInt("orders_per_hour"));
                    report.add(row);
                }
            }
        }
        return report;
    }

    // Profit Margin Report
    public List<Map<String, Object>> getProfitMarginReport(String startDate, String endDate) throws SQLException {
        List<Map<String, Object>> report = new ArrayList<>();
        String query = """
            SELECT 
                d.dish_id,
                d.name as dish_name,
                d.selling_price,
                SUM(di.quantity_needed * i.cost_per_unit) as total_cost,
                (d.selling_price - SUM(di.quantity_needed * i.cost_per_unit)) as profit,
                ((d.selling_price - SUM(di.quantity_needed * i.cost_per_unit)) / d.selling_price * 100) as profit_margin,
                COUNT(oi.order_id) as times_ordered,
                SUM(oi.quantity) as total_quantity_sold
            FROM Dishes d
            JOIN DishIngredients di ON d.dish_id = di.dish_id
            JOIN Ingredients i ON di.ingredient_id = i.ingredient_id
            LEFT JOIN OrderItems oi ON d.dish_id = oi.dish_id
            LEFT JOIN Orders o ON oi.order_id = o.order_id
            WHERE d.is_deleted = FALSE
            AND (o.order_datetime BETWEEN ? AND ? OR o.order_datetime IS NULL)
            GROUP BY d.dish_id
            ORDER BY profit_margin DESC
        """;

        try (Connection conn = controller.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, startDate);
            stmt.setString(2, endDate);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("dish_id", rs.getInt("dish_id"));
                    row.put("dish_name", rs.getString("dish_name"));
                    row.put("selling_price", rs.getDouble("selling_price"));
                    row.put("total_cost", rs.getDouble("total_cost"));
                    row.put("profit", rs.getDouble("profit"));
                    row.put("profit_margin", rs.getDouble("profit_margin"));
                    row.put("times_ordered", rs.getInt("times_ordered"));
                    row.put("total_quantity_sold", rs.getInt("total_quantity_sold"));
                    report.add(row);
                }
            }
        }
        return report;
    }

    // Inventory Status Report
    public List<Map<String, Object>> getInventoryStatusReport() throws SQLException {
        List<Map<String, Object>> report = new ArrayList<>();
        String query = """
            SELECT 
                i.ingredient_id,
                i.name as ingredient_name,
                i.quantity_in_stock,
                i.minimum_stock_level,
                i.cost_per_unit,
                u.unit_name,
                CASE 
                    WHEN i.quantity_in_stock <= i.minimum_stock_level THEN 'Low Stock'
                    WHEN i.quantity_in_stock = 0 THEN 'Out of Stock'
                    ELSE 'In Stock'
                END as stock_status,
                s.name as primary_supplier
            FROM Ingredients i
            JOIN Units u ON i.unit_id = u.unit_id
            LEFT JOIN IngredientSuppliers isup ON i.ingredient_id = isup.ingredient_id AND isup.is_primary_supplier = TRUE
            LEFT JOIN Suppliers s ON isup.supplier_id = s.supplier_id
            WHERE i.is_deleted = FALSE
            ORDER BY stock_status, ingredient_name
        """;

        try (Connection conn = controller.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("ingredient_id", rs.getInt("ingredient_id"));
                row.put("ingredient_name", rs.getString("ingredient_name"));
                row.put("quantity_in_stock", rs.getDouble("quantity_in_stock"));
                row.put("minimum_stock_level", rs.getDouble("minimum_stock_level"));
                row.put("cost_per_unit", rs.getDouble("cost_per_unit"));
                row.put("unit_name", rs.getString("unit_name"));
                row.put("stock_status", rs.getString("stock_status"));
                row.put("primary_supplier", rs.getString("primary_supplier"));
                report.add(row);
            }
        }
        return report;
    }
} 