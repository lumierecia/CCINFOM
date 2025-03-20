package controller;

import model.*;
import dao.*;
import java.util.List;
import java.util.ArrayList;

public class RestaurantController {
    private RestaurantDAO restaurantDAO;
    private OrderDAO orderDAO;
    private CustomerDAO customerDAO;
    private EmployeeDAO employeeDAO;

    public RestaurantController() {
        this.restaurantDAO = new RestaurantDAO();
        this.orderDAO = new OrderDAO();
        this.customerDAO = new CustomerDAO();
        this.employeeDAO = new EmployeeDAO();
    }

    // Restaurant methods
    public List<Restaurant> getAllRestaurants() {
        return restaurantDAO.getAllRestaurants();
    }

    public Restaurant getRestaurantById(int id) {
        return restaurantDAO.getRestaurantById(id);
    }

    public boolean addRestaurant(String name, String location, String cuisine, double rating) {
        Restaurant restaurant = new Restaurant(0, name, location, cuisine, rating);
        return restaurantDAO.addRestaurant(restaurant);
    }

    public boolean updateRestaurant(int id, String name, String location, String cuisine, double rating) {
        Restaurant restaurant = new Restaurant(id, name, location, cuisine, rating);
        return restaurantDAO.updateRestaurant(restaurant);
    }

    public boolean deleteRestaurant(int id) {
        return restaurantDAO.deleteRestaurant(id);
    }

    public List<Restaurant> searchRestaurants(String searchTerm) {
        return restaurantDAO.searchRestaurants(searchTerm);
    }

    // Order methods
    public List<Order> getAllOrders() {
        return orderDAO.getAllOrders();
    }

    public Order getOrderById(int id) {
        return orderDAO.getOrderById(id);
    }

    public boolean createOrder(Order order, List<OrderItem> items, List<Integer> employeeIds) {
        int orderId = orderDAO.createOrder(order);
        if (orderId == -1) return false;

        order.setOrderId(orderId);
        
        // Add order items
        for (OrderItem item : items) {
            item.setOrderId(orderId);
            if (!orderDAO.addOrderItem(item)) return false;
        }

        // Assign employees
        for (int employeeId : employeeIds) {
            if (!orderDAO.assignEmployeeToOrder(orderId, employeeId)) return false;
        }

        return true;
    }

    public double calculateOrderTotal(int orderId) {
        List<OrderItem> items = orderDAO.getOrderItems(orderId);
        double total = 0.0;
        for (OrderItem item : items) {
            Restaurant product = restaurantDAO.getRestaurantById(item.getProductId());
            if (product != null) {
                total += product.getRating() * item.getQuantity(); // Using rating as price for this example
            }
        }
        return total;
    }

    public boolean processPayment(int orderId, double amount, String paymentMethod) {
        // In a real application, this would integrate with a payment processing system
        return orderDAO.updateOrderStatus(orderId, "Completed");
    }

    public List<Order> getOrdersByDateRange(String startDate, String endDate) {
        try {
            return orderDAO.getOrdersByDateRange(startDate, endDate);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Customer methods
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

    public boolean deleteCustomer(int id) {
        return customerDAO.deleteCustomer(id);
    }

    // Employee methods
    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees();
    }

    public Employee getEmployeeById(int id) {
        return employeeDAO.getEmployeeById(id);
    }

    public boolean addEmployee(Employee employee) {
        return employeeDAO.addEmployee(employee);
    }

    public boolean updateEmployee(Employee employee) {
        return employeeDAO.updateEmployee(employee);
    }

    public boolean deleteEmployee(int id) {
        return employeeDAO.deleteEmployee(id);
    }

    public boolean assignShift(int employeeId, String date, String shiftType) {
        return employeeDAO.assignShift(employeeId, date, shiftType);
    }

    public List<Employee> getEmployeesByShift(String date, String shiftType) {
        return employeeDAO.getEmployeesByShift(date, shiftType);
    }

    public boolean removeShift(int employeeId) {
        return employeeDAO.removeShift(employeeId);
    }

    // Additional business logic methods
    public double calculateAverageRating(List<Restaurant> restaurants) {
        if (restaurants.isEmpty()) {
            return 0.0;
        }
        double sum = restaurants.stream()
                .mapToDouble(Restaurant::getRating)
                .sum();
        return sum / restaurants.size();
    }

    public List<Restaurant> getTopRatedRestaurants(int limit) {
        return getAllRestaurants().stream()
                .sorted((r1, r2) -> Double.compare(r2.getRating(), r1.getRating()))
                .limit(limit)
                .toList();
    }

    public List<Restaurant> getRestaurantsByCuisine(String cuisine) {
        return getAllRestaurants().stream()
                .filter(r -> r.getCuisine().equalsIgnoreCase(cuisine))
                .toList();
    }
} 