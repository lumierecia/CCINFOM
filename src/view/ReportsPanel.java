package view;

import controller.RestaurantController;
import model.Order;
import model.OrderItem;
import model.Customer;
import model.Employee;
import model.Inventory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class ReportsPanel extends JPanel {
    private final RestaurantController controller;
    private final JComboBox<String> reportTypeCombo;
    private final JTable reportTable;
    private DefaultTableModel tableModel;
    private final JPanel reportPanel;
    private final JTextField dayField;
    private final JTextField monthField;
    private final JTextField yearField;
    private final JLabel summaryLabel;

    public ReportsPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        // Create control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Report type selector
        String[] reportTypes = {
            "Sales Report",
            "Customer Orders Report",
            "Employee Shifts Report",
            "Profit Margin Report"
        };
        reportTypeCombo = new JComboBox<>(reportTypes);
        
        // Date input fields
        dayField = new JTextField(2);
        monthField = new JTextField(2);
        yearField = new JTextField(4);
        
        JButton generateButton = new JButton("Generate Report");
        
        controlPanel.add(new JLabel("Report Type:"));
        controlPanel.add(reportTypeCombo);
        controlPanel.add(new JLabel("Day (0 to skip):"));
        controlPanel.add(dayField);
        controlPanel.add(new JLabel("Month (0 to skip):"));
        controlPanel.add(monthField);
        controlPanel.add(new JLabel("Year (0 to skip):"));
        controlPanel.add(yearField);
        controlPanel.add(generateButton);

        // Create report panel
        reportPanel = new JPanel(new BorderLayout());
        
        // Create table
        reportTable = new JTable();
        reportTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Create summary label
        summaryLabel = new JLabel();
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Add components to main panel
        add(controlPanel, BorderLayout.NORTH);
        add(reportPanel, BorderLayout.CENTER);
        add(summaryLabel, BorderLayout.SOUTH);

        // Add listeners
        generateButton.addActionListener(e -> generateReport());
        reportTypeCombo.addActionListener(e -> updateReportPanel());

        // Set default values
        dayField.setText("0");
        monthField.setText("0");
        yearField.setText("0");

        // Initialize the report panel
        updateReportPanel();
    }

    private void updateReportPanel() {
        reportPanel.removeAll();
        String selectedReport = (String) reportTypeCombo.getSelectedItem();
        
        switch (selectedReport) {
            case "Sales Report" -> setupSalesReport();
            case "Customer Orders Report" -> setupCustomerOrdersReport();
            case "Employee Shifts Report" -> setupEmployeeShiftsReport();
            case "Profit Margin Report" -> setupProfitMarginReport();
        }
        
        reportPanel.revalidate();
        reportPanel.repaint();
    }

    private String getDatePattern() {
        try {
            int day = Integer.parseInt(dayField.getText().trim());
            int month = Integer.parseInt(monthField.getText().trim());
            int year = Integer.parseInt(yearField.getText().trim());

            if (day < 0 || month < 0 || year < 0) {
                throw new IllegalArgumentException("Values must be non-negative");
            }

            if (month > 12) {
                throw new IllegalArgumentException("Month must be between 0 and 12");
            }

            if (day > 31) {
                throw new IllegalArgumentException("Day must be between 0 and 31");
            }

            String yearStr = (year == 0) ? "%" : String.format("%04d", year);
            String monthStr = (month == 0) ? "%" : String.format("%02d", month);
            String dayStr = (day == 0) ? "%" : String.format("%02d", day);

            return String.format("%s-%s-%s", yearStr, monthStr, dayStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter valid numbers");
        }
    }

    private void setupSalesReport() {
        String[] columns = {"Sales Date", "Total Sales", "Average Sales", "Top Product", "Units Sold"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportTable.setModel(tableModel);
        reportPanel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
    }

    private void setupCustomerOrdersReport() {
        String[] columns = {"Total Orders", "Total Amount Spent", "Most Bought Product", "Most Bought Quantity"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportTable.setModel(tableModel);
        reportPanel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
    }

    private void setupEmployeeShiftsReport() {
        String[] columns = {"Employee ID", "First Name", "Last Name", "Total Shifts", "Total Hours"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportTable.setModel(tableModel);
        reportPanel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
    }

    private void setupProfitMarginReport() {
        String[] columns = {"Product ID", "Order ID", "Order Date", "Total Orders", "Total Quantity", "Revenue", "Cost", "Profit"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reportTable.setModel(tableModel);
        reportPanel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
    }

    private void generateReport() {
        try {
            String datePattern = getDatePattern();
            String selectedReport = (String) reportTypeCombo.getSelectedItem();
            
            tableModel.setRowCount(0);
            
            switch (selectedReport) {
                case "Sales Report" -> controller.generateSalesReport(datePattern, tableModel);
                case "Customer Orders Report" -> controller.generateCustomerOrdersReport(datePattern, tableModel);
                case "Employee Shifts Report" -> controller.generateEmployeeShiftsReport(datePattern, tableModel);
                case "Profit Margin Report" -> controller.generateProfitMarginReport(datePattern, tableModel);
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, 
                e.getMessage(), 
                "Input Error", 
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error generating report: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
} 