package view;

import controller.RestaurantController;
import model.*;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReportsPanel extends JPanel {
    private JComboBox<String> reportTypeComboBox;
    private JButton generateButton;
    private JTextArea reportArea;
    private JPanel chartPanel;
    private RestaurantController controller;
    private JTextField startDateField;
    private JTextField endDateField;

    public ReportsPanel() {
        controller = new RestaurantController();
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Create controls panel
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportTypeComboBox = new JComboBox<>(new String[]{
            "Daily Revenue",
            "Monthly Revenue",
            "Popular Dishes",
            "Customer Feedback",
            "Inventory Status",
            "Employee Shifts"
        });
        generateButton = new JButton("Generate Report");
        startDateField = new JTextField(10);
        endDateField = new JTextField(10);
        
        controlsPanel.add(new JLabel("Report Type: "));
        controlsPanel.add(reportTypeComboBox);
        controlsPanel.add(new JLabel("Start Date (YYYY-MM-DD): "));
        controlsPanel.add(startDateField);
        controlsPanel.add(new JLabel("End Date (YYYY-MM-DD): "));
        controlsPanel.add(endDateField);
        controlsPanel.add(generateButton);

        // Create split pane for report and chart
        chartPanel = new JPanel();
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createTitledBorder("Chart View"));

        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Report Details"));

        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            chartPanel,
            scrollPane
        );
        splitPane.setResizeWeight(0.5);

        // Add components to panel
        add(controlsPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Add listeners
        generateButton.addActionListener(e -> generateReport());
        reportTypeComboBox.addActionListener(e -> updateChartPanel());
    }

    private void generateReport() {
        String reportType = (String) reportTypeComboBox.getSelectedItem();
        String startDate = startDateField.getText();
        String endDate = endDateField.getText();

        if (startDate.isEmpty() || endDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both start and end dates.");
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            if (start.after(end)) {
                JOptionPane.showMessageDialog(this, "Start date must be before end date.");
                return;
            }

            StringBuilder report = new StringBuilder();
            report.append("Report Type: ").append(reportType).append("\n");
            report.append("Period: ").append(startDate).append(" to ").append(endDate).append("\n\n");

            switch (reportType) {
                case "Daily Revenue" -> generateDailyRevenueReport(report, startDate, endDate);
                case "Monthly Revenue" -> generateMonthlyRevenueReport(report, startDate, endDate);
                case "Popular Dishes" -> generatePopularDishesReport(report, startDate, endDate);
                case "Customer Feedback" -> generateCustomerFeedbackReport(report, startDate, endDate);
                case "Inventory Status" -> generateInventoryStatusReport(report);
                case "Employee Shifts" -> generateEmployeeShiftsReport(report, startDate, endDate);
            }

            reportArea.setText(report.toString());
            updateChartPanel();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD format.");
        }
    }

    private void generateDailyRevenueReport(StringBuilder report, String startDate, String endDate) {
        report.append("Daily Revenue Report\n");
        report.append("===================\n\n");
        
        List<Order> orders = controller.getOrdersByDateRange(startDate, endDate);
        double totalRevenue = 0.0;
        
        for (Order order : orders) {
            double orderTotal = controller.calculateOrderTotal(order.getOrderId());
            totalRevenue += orderTotal;
            report.append(String.format("Order #%d: $%.2f\n", order.getOrderId(), orderTotal));
        }
        
        report.append("\nTotal Revenue: $").append(String.format("%.2f", totalRevenue));
    }

    private void generateMonthlyRevenueReport(StringBuilder report, String startDate, String endDate) {
        report.append("Monthly Revenue Report\n");
        report.append("=====================\n\n");
        
        // Group orders by month and calculate totals
        // This would typically involve SQL aggregation in a real application
    }

    private void generatePopularDishesReport(StringBuilder report, String startDate, String endDate) {
        report.append("Popular Dishes Report\n");
        report.append("====================\n\n");
        
        List<Restaurant> topDishes = controller.getTopRatedRestaurants(10);
        for (Restaurant dish : topDishes) {
            report.append(String.format("%s - Rating: %.1f\n", 
                dish.getName(), dish.getRating()));
        }
    }

    private void generateCustomerFeedbackReport(StringBuilder report, String startDate, String endDate) {
        report.append("Customer Feedback Report\n");
        report.append("=======================\n\n");
        
        // This would typically involve a feedback/ratings table in the database
    }

    private void generateInventoryStatusReport(StringBuilder report) {
        report.append("Inventory Status Report\n");
        report.append("=====================\n\n");
        
        List<Restaurant> inventory = controller.getAllRestaurants();
        for (Restaurant item : inventory) {
            report.append(String.format("%-30s | Rating: %.1f\n",
                item.getName(), item.getRating()));
        }
    }

    private void generateEmployeeShiftsReport(StringBuilder report, String startDate, String endDate) {
        report.append("Employee Shifts Report\n");
        report.append("=====================\n\n");
        
        List<Employee> employees = controller.getAllEmployees();
        for (Employee employee : employees) {
            report.append(String.format("%s %s - %s\n",
                employee.getFirstName(),
                employee.getLastName(),
                employee.getPosition()));
        }
    }

    private void updateChartPanel() {
        chartPanel.removeAll();
        String reportType = (String) reportTypeComboBox.getSelectedItem();
        
        // Create a panel to show chart information
        JPanel infoPanel = new JPanel(new BorderLayout());
        
        // Add chart title
        JLabel titleLabel = new JLabel("Visual Report: " + reportType);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Add chart description
        String description = switch (reportType) {
            case "Daily Revenue" -> 
                "Bar chart showing revenue trends over the selected period, with daily totals and average lines.";
            case "Monthly Revenue" -> 
                "Line chart comparing monthly revenue trends, with year-over-year comparison capability.";
            case "Popular Dishes" -> 
                "Pie chart showing the distribution of most ordered dishes and their contribution to revenue.";
            case "Customer Feedback" -> 
                "Scatter plot showing customer ratings and feedback trends over time.";
            case "Inventory Status" -> 
                "Stacked bar chart showing current stock levels and reorder points for each item.";
            case "Employee Shifts" -> 
                "Gantt chart showing employee shift schedules and coverage analysis.";
            default -> 
                "Select a report type to view its corresponding chart visualization.";
        };
        
        JTextArea descLabel = new JTextArea(description);
        descLabel.setWrapStyleWord(true);
        descLabel.setLineWrap(true);
        descLabel.setOpaque(false);
        descLabel.setEditable(false);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.add(descLabel, BorderLayout.CENTER);
        
        // Add placeholder for actual chart
        JPanel chartPlaceholder = new JPanel(new BorderLayout());
        chartPlaceholder.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        chartPlaceholder.setPreferredSize(new Dimension(400, 300));
        
        JLabel placeholderLabel = new JLabel("Chart Visualization Area");
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        chartPlaceholder.add(placeholderLabel, BorderLayout.CENTER);
        
        infoPanel.add(chartPlaceholder, BorderLayout.SOUTH);
        
        // Add note about future implementation
        JLabel noteLabel = new JLabel("Note: Charts will be implemented using JFreeChart library");
        noteLabel.setHorizontalAlignment(SwingConstants.CENTER);
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        infoPanel.add(noteLabel, BorderLayout.SOUTH);
        
        chartPanel.add(infoPanel);
        chartPanel.revalidate();
        chartPanel.repaint();
    }
} 