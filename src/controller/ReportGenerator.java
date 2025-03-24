package controller;

import dao.*;
import model.*;

import java.sql.Date;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {
    private final OrderDAO orderDAO;
    private final EmployeeDAO employeeDAO;
    private final CustomerDAO customerDAO;
    private final PaymentDAO paymentDAO;
    private final DishDAO dishDAO;
    private final IngredientDAO ingredientDAO;

    public ReportGenerator(RestaurantController controller) throws SQLException {
        this.orderDAO = controller.getOrderDAO();
        this.employeeDAO = controller.getEmployeeDAO();
        this.customerDAO = controller.getCustomerDAO();
        this.paymentDAO = controller.getPaymentDAO();
        this.dishDAO = controller.getDishDAO();
        this.ingredientDAO = controller.getIngredientDAO();
    }

    public String generateSalesReport(Date startDate, Date endDate) throws SQLException {
        List<Order> orders = orderDAO.getOrdersByDateRange(startDate.toString(), endDate.toString());
        
        double totalSales = orders.stream()
            .mapToDouble(Order::getTotalAmount)
            .sum();
        
        double averageOrderValue = totalSales / orders.size();
        
        // Get top selling products
        Map<Integer, Integer> productSales = new HashMap<>();
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                productSales.merge(item.getDishId(), item.getQuantity(), Integer::sum);
            }
        }
        
        List<Map.Entry<Integer, Integer>> topProducts = productSales.entrySet().stream()
            .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
            .limit(5)
            .collect(Collectors.toList());

        StringBuilder report = new StringBuilder();
        report.append("Sales Report for Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");
        report.append("Summary:\n");
        report.append(String.format("Total Sales: $%.2f\n", totalSales));
        report.append(String.format("Number of Orders: %d\n", orders.size()));
        report.append(String.format("Average Order Value: $%.2f\n", averageOrderValue));
        
        report.append("\nTop Selling Products:\n");
        for (Map.Entry<Integer, Integer> entry : topProducts) {
            Dish dish = dishDAO.getDishById(entry.getKey());
            report.append(String.format("%s: %d units sold\n", dish.getName(), entry.getValue()));
        }

        return report.toString();
    }

    public String generateEmployeePerformanceReport(Date startDate, Date endDate) throws SQLException {
        List<Order> orders = orderDAO.getOrdersByDateRange(startDate.toString(), endDate.toString());
        Map<Integer, Integer> ordersPerEmployee = new HashMap<>();
        Map<Integer, Double> totalSalesPerEmployee = new HashMap<>();
        
        for (Order order : orders) {
            for (Integer employeeId : order.getAssignedEmployees()) {
                ordersPerEmployee.merge(employeeId, 1, Integer::sum);
                totalSalesPerEmployee.merge(employeeId, order.getTotalAmount(), Double::sum);
            }
        }

        StringBuilder report = new StringBuilder();
        report.append("Employee Performance Report for Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");
        
        for (Map.Entry<Integer, Integer> entry : ordersPerEmployee.entrySet()) {
            Employee employee = employeeDAO.getEmployeeById(entry.getKey());
            report.append(String.format("Employee: %s %s\n", employee.getFirstName(), employee.getLastName()));
            report.append(String.format("Orders Taken: %d\n", entry.getValue()));
            report.append(String.format("Total Sales: $%.2f\n", totalSalesPerEmployee.get(entry.getKey())));
            report.append(String.format("Average Sales per Order: $%.2f\n", 
                totalSalesPerEmployee.get(entry.getKey()) / entry.getValue()));
            report.append("\n");
        }

        return report.toString();
    }

    public String generateCustomerInsightsReport(Date startDate, Date endDate) throws SQLException {
        List<Order> orders = orderDAO.getOrdersByDateRange(startDate.toString(), endDate.toString());
        Map<Integer, Integer> customerOrderCount = new HashMap<>();
        Map<Integer, Double> customerTotalSpent = new HashMap<>();
        
        for (Order order : orders) {
            customerOrderCount.merge(order.getCustomerId(), 1, Integer::sum);
            customerTotalSpent.merge(order.getCustomerId(), order.getTotalAmount(), Double::sum);
        }

        int returningCustomers = (int) customerOrderCount.values().stream().filter(count -> count > 1).count();
        int newCustomers = (int) customerOrderCount.values().stream().filter(count -> count == 1).count();
        
        double averageSpending = customerTotalSpent.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);

        // Analyze order times
        Map<Integer, Integer> ordersByHour = new HashMap<>();
        for (Order order : orders) {
            int hour = order.getOrderDateTime().getHours();
            ordersByHour.merge(hour, 1, Integer::sum);
        }

        int peakHour = ordersByHour.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(-1);

        StringBuilder report = new StringBuilder();
        report.append("Customer Insights Report for Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");
        report.append("Summary:\n");
        report.append(String.format("Total Customers: %d\n", customerOrderCount.size()));
        report.append(String.format("Returning Customers: %d\n", returningCustomers));
        report.append(String.format("New Customers: %d\n", newCustomers));
        report.append(String.format("Average Customer Spending: $%.2f\n", averageSpending));
        report.append(String.format("Peak Ordering Hour: %02d:00\n", peakHour));
        
        report.append("\nCustomer Loyalty Analysis:\n");
        report.append(String.format("Returning Customer Rate: %.1f%%\n", 
            (returningCustomers * 100.0) / customerOrderCount.size()));

        return report.toString();
    }

    public String generateProfitMarginReport(Date startDate, Date endDate) throws SQLException {
        List<Order> orders = orderDAO.getOrdersByDateRange(startDate.toString(), endDate.toString());
        Map<Integer, Double> dishCosts = new HashMap<>();
        Map<Integer, Double> dishRevenues = new HashMap<>();
        
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                Dish dish = dishDAO.getDishById(item.getDishId());
                double cost = calculateDishCost(dish);
                double revenue = item.getPriceAtTime() * item.getQuantity();
                
                dishCosts.merge(item.getDishId(), cost * item.getQuantity(), Double::sum);
                dishRevenues.merge(item.getDishId(), revenue, Double::sum);
            }
        }

        StringBuilder report = new StringBuilder();
        report.append("Profit Margin Report for Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");
        
        double totalRevenue = dishRevenues.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalCost = dishCosts.values().stream().mapToDouble(Double::doubleValue).sum();
        double totalProfit = totalRevenue - totalCost;
        double overallMargin = (totalProfit / totalRevenue) * 100;

        report.append("Overall Performance:\n");
        report.append(String.format("Total Revenue: $%.2f\n", totalRevenue));
        report.append(String.format("Total Cost: $%.2f\n", totalCost));
        report.append(String.format("Total Profit: $%.2f\n", totalProfit));
        report.append(String.format("Overall Profit Margin: %.1f%%\n", overallMargin));
        
        report.append("\nPer-Dish Analysis:\n");
        for (Map.Entry<Integer, Double> entry : dishRevenues.entrySet()) {
            int dishId = entry.getKey();
            Dish dish = dishDAO.getDishById(dishId);
            double revenue = entry.getValue();
            double cost = dishCosts.get(dishId);
            double profit = revenue - cost;
            double margin = (profit / revenue) * 100;
            
            report.append(String.format("\n%s:\n", dish.getName()));
            report.append(String.format("Revenue: $%.2f\n", revenue));
            report.append(String.format("Cost: $%.2f\n", cost));
            report.append(String.format("Profit: $%.2f\n", profit));
            report.append(String.format("Margin: %.1f%%\n", margin));
        }

        return report.toString();
    }

    private double calculateDishCost(Dish dish) throws SQLException {
        double totalCost = 0.0;
        List<DishIngredient> ingredients = dishDAO.getDishIngredients(dish.getDishId());
        
        for (DishIngredient di : ingredients) {
            Ingredient ingredient = ingredientDAO.getIngredientById(di.getIngredientId());
            double unitCost = ingredient.getCostPerUnit();
            totalCost += unitCost * di.getQuantityNeeded();
        }
        
        return totalCost;
    }
} 