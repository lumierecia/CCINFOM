package view;

import controller.RestaurantController;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TransactionsPanel extends JPanel {
    private JTable transactionsTable;
    private JButton newTransactionButton;
    private JButton processPaymentButton;
    private JButton manageShiftsButton;
    private JComboBox<String> filterComboBox;
    private JTextField dateField;
    private DefaultTableModel tableModel;
    private RestaurantController controller;

    public TransactionsPanel() {
        controller = new RestaurantController();
        setLayout(new BorderLayout());
        initComponents();
        loadTransactions();
    }

    private void initComponents() {
        // Create toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        newTransactionButton = new JButton("New Order");
        processPaymentButton = new JButton("Process Payment");
        manageShiftsButton = new JButton("Manage Shifts");
        filterComboBox = new JComboBox<>(new String[]{"All", "Today", "This Week", "This Month"});
        
        // Create date field with format guidance
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        dateField = new JTextField(10);
        dateField.setToolTipText("Enter date in YYYY-MM-DD format");
        // Add a placeholder text
        dateField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (dateField.getText().equals("YYYY-MM-DD")) {
                    dateField.setText("");
                    dateField.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (dateField.getText().isEmpty()) {
                    dateField.setText("YYYY-MM-DD");
                    dateField.setForeground(Color.GRAY);
                }
            }
        });
        dateField.setText("YYYY-MM-DD");
        dateField.setForeground(Color.GRAY);
        
        datePanel.add(new JLabel("Date: "));
        datePanel.add(dateField);
        
        toolBar.add(newTransactionButton);
        toolBar.add(processPaymentButton);
        toolBar.add(manageShiftsButton);
        toolBar.add(new JLabel("Filter: "));
        toolBar.add(filterComboBox);
        toolBar.add(datePanel);

        // Create table
        String[] columnNames = {"Order ID", "Customer", "Date", "Type", "Status", "Total"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(transactionsTable);

        // Add components to panel
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add listeners
        newTransactionButton.addActionListener(e -> showNewOrderDialog());
        processPaymentButton.addActionListener(e -> processPayment());
        manageShiftsButton.addActionListener(e -> manageShifts());
        filterComboBox.addActionListener(e -> applyFilter());
        dateField.addActionListener(e -> filterByDate());
    }

    private void loadTransactions() {
        tableModel.setRowCount(0);
        List<Order> orders = controller.getAllOrders();
        for (Order order : orders) {
            Customer customer = controller.getCustomerById(order.getCustomerId());
            double total = controller.calculateOrderTotal(order.getOrderId());
            Object[] row = {
                order.getOrderId(),
                customer.getFirstName() + " " + customer.getLastName(),
                order.getOrderDateTime(),
                order.getOrderType(),
                order.getOrderStatus(),
                String.format("$%.2f", total)
            };
            tableModel.addRow(row);
        }
    }

    private void showNewOrderDialog() {
        // Create dialog components
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "New Order", true);
        dialog.setLayout(new BorderLayout());
        
        // Customer selection panel
        JPanel customerPanel = new JPanel(new GridLayout(0, 2));
        List<Customer> customers = controller.getAllCustomers();
        JComboBox<Customer> customerCombo = new JComboBox<>(customers.toArray(new Customer[0]));
        JButton newCustomerButton = new JButton("New Customer");
        customerPanel.add(new JLabel("Customer:"));
        customerPanel.add(customerCombo);
        customerPanel.add(new JLabel());
        customerPanel.add(newCustomerButton);

        // Order items panel
        JPanel itemsPanel = new JPanel(new BorderLayout());
        DefaultTableModel itemsModel = new DefaultTableModel(
            new String[]{"Product", "Quantity", "Price"}, 0
        );
        JTable itemsTable = new JTable(itemsModel);
        JButton addItemButton = new JButton("Add Item");
        itemsPanel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        itemsPanel.add(addItemButton, BorderLayout.SOUTH);

        // Order type panel
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Dine-in", "Takeout", "Delivery"});
        typePanel.add(new JLabel("Order Type:"));
        typePanel.add(typeCombo);

        // Employee assignment panel
        JPanel employeePanel = new JPanel(new BorderLayout());
        DefaultTableModel employeeModel = new DefaultTableModel(
            new String[]{"Employee", "Position"}, 0
        );
        JTable employeeTable = new JTable(employeeModel);
        JButton assignEmployeeButton = new JButton("Assign Employee");
        employeePanel.add(new JScrollPane(employeeTable), BorderLayout.CENTER);
        employeePanel.add(assignEmployeeButton, BorderLayout.SOUTH);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitButton = new JButton("Submit Order");
        JButton cancelButton = new JButton("Cancel");
        buttonsPanel.add(submitButton);
        buttonsPanel.add(cancelButton);

        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridy = 0;
        mainPanel.add(customerPanel, gbc);
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        mainPanel.add(itemsPanel, gbc);
        gbc.gridy = 2;
        gbc.weighty = 0;
        mainPanel.add(typePanel, gbc);
        gbc.gridy = 3;
        gbc.weighty = 0.5;
        mainPanel.add(employeePanel, gbc);
        gbc.gridy = 4;
        gbc.weighty = 0;
        mainPanel.add(buttonsPanel, gbc);

        // Add action listeners
        List<OrderItem> orderItems = new ArrayList<>();
        List<Integer> assignedEmployees = new ArrayList<>();

        addItemButton.addActionListener(e -> {
            List<Restaurant> products = controller.getAllRestaurants();
            JComboBox<Restaurant> productCombo = new JComboBox<>(products.toArray(new Restaurant[0]));
            JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

            Object[] message = {
                "Product:", productCombo,
                "Quantity:", quantitySpinner
            };

            int option = JOptionPane.showConfirmDialog(dialog, message, "Add Item", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (option == JOptionPane.OK_OPTION) {
                Restaurant product = (Restaurant) productCombo.getSelectedItem();
                int quantity = (Integer) quantitySpinner.getValue();
                double price = product.getRating(); // Using rating as price for this example

                itemsModel.addRow(new Object[]{product.getName(), quantity, price});
                orderItems.add(new OrderItem(0, 0, product.getRestaurantID(), quantity, price));
            }
        });

        assignEmployeeButton.addActionListener(e -> {
            List<Employee> employees = controller.getAllEmployees();
            JComboBox<Employee> employeeCombo = new JComboBox<>(employees.toArray(new Employee[0]));

            int option = JOptionPane.showConfirmDialog(dialog, employeeCombo, "Assign Employee", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (option == JOptionPane.OK_OPTION) {
                Employee employee = (Employee) employeeCombo.getSelectedItem();
                employeeModel.addRow(new Object[]{
                    employee.getFirstName() + " " + employee.getLastName(),
                    employee.getPosition()
                });
                assignedEmployees.add(employee.getEmployeeId());
            }
        });

        submitButton.addActionListener(e -> {
            if (orderItems.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please add at least one item to the order.");
                return;
            }

            if (assignedEmployees.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please assign at least one employee to the order.");
                return;
            }

            Customer selectedCustomer = (Customer) customerCombo.getSelectedItem();
            String orderType = (String) typeCombo.getSelectedItem();

            Order order = new Order(0, selectedCustomer.getCustomerId(), 
                new Timestamp(System.currentTimeMillis()), orderType, "In Progress");

            if (controller.createOrder(order, orderItems, assignedEmployees)) {
                loadTransactions();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Order created successfully!");
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to create order.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        newCustomerButton.addActionListener(e -> {
            Customer newCustomer = showNewCustomerDialog(dialog);
            if (newCustomer != null) {
                customerCombo.addItem(newCustomer);
                customerCombo.setSelectedItem(newCustomer);
            }
        });

        // Show dialog
        dialog.add(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private Customer showNewCustomerDialog(JDialog parent) {
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField addressField = new JTextField();

        Object[] message = {
            "First Name:", firstNameField,
            "Last Name:", lastNameField,
            "Email:", emailField,
            "Phone:", phoneField,
            "Address:", addressField
        };

        int option = JOptionPane.showConfirmDialog(parent, message, "New Customer", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            Customer customer = new Customer(0,
                firstNameField.getText(),
                lastNameField.getText(),
                emailField.getText(),
                phoneField.getText(),
                addressField.getText()
            );

            int customerId = controller.addCustomer(customer);
            if (customerId > 0) {
                customer.setCustomerId(customerId);
                return customer;
            } else {
                JOptionPane.showMessageDialog(parent, "Failed to create customer.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    private void processPayment() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to process payment.", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int orderId = (int) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 4);
        
        if (!"In Progress".equals(status)) {
            JOptionPane.showMessageDialog(this, "This order has already been processed.", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double total = controller.calculateOrderTotal(orderId);
        String[] paymentMethods = {"Cash", "Credit Card", "Debit Card"};
        JComboBox<String> methodCombo = new JComboBox<>(paymentMethods);
        JTextField amountField = new JTextField(String.format("%.2f", total));

        Object[] message = {
            "Total Amount:", String.format("$%.2f", total),
            "Payment Method:", methodCombo,
            "Amount Paid:", amountField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Process Payment", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            try {
                double amountPaid = Double.parseDouble(amountField.getText());
                if (amountPaid < total) {
                    JOptionPane.showMessageDialog(this, "Payment amount must be at least the total amount.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String paymentMethod = (String) methodCombo.getSelectedItem();
                if (controller.processPayment(orderId, amountPaid, paymentMethod)) {
                    loadTransactions();
                    JOptionPane.showMessageDialog(this, "Payment processed successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to process payment.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid payment amount.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void manageShifts() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Manage Shifts", true);
        dialog.setLayout(new BorderLayout());

        // Create components
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton assignButton = new JButton("Assign Shift");
        JButton removeButton = new JButton("Remove Shift");
        toolBar.add(assignButton);
        toolBar.add(removeButton);

        // Create table
        String[] columnNames = {"Employee ID", "Name", "Position", "Shift Type", "Start Time", "End Time"};
        DefaultTableModel shiftsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable shiftsTable = new JTable(shiftsModel);
        JScrollPane scrollPane = new JScrollPane(shiftsTable);

        // Load current shifts
        loadShifts(shiftsModel);

        // Add action listeners
        assignButton.addActionListener(e -> {
            List<Employee> availableEmployees = controller.getAllEmployees();
            if (availableEmployees.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "No employees available.");
                return;
            }

            JComboBox<Employee> employeeCombo = new JComboBox<>(availableEmployees.toArray(new Employee[0]));
            String[] shiftTypes = {"Morning", "Afternoon", "Evening"};
            JComboBox<String> shiftCombo = new JComboBox<>(shiftTypes);

            Object[] message = {
                "Employee:", employeeCombo,
                "Shift:", shiftCombo
            };

            int option = JOptionPane.showConfirmDialog(dialog, message, "Assign Shift", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (option == JOptionPane.OK_OPTION) {
                Employee employee = (Employee) employeeCombo.getSelectedItem();
                String shiftType = (String) shiftCombo.getSelectedItem();
                String date = String.format("%tF", new java.util.Date());

                if (controller.assignShift(employee.getEmployeeId(), date, shiftType)) {
                    loadShifts(shiftsModel);
                    JOptionPane.showMessageDialog(dialog, "Shift assigned successfully!");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to assign shift.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        removeButton.addActionListener(e -> {
            int selectedRow = shiftsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Please select a shift to remove.");
                return;
            }

            int employeeId = (int) shiftsModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(dialog, 
                "Are you sure you want to remove this shift assignment?", 
                "Confirm Remove", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (controller.removeShift(employeeId)) {
                    loadShifts(shiftsModel);
                    JOptionPane.showMessageDialog(dialog, "Shift removed successfully!");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to remove shift.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add components to dialog
        dialog.add(toolBar, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Show dialog
        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void loadShifts(DefaultTableModel model) {
        model.setRowCount(0);
        List<Employee> employees = controller.getAllEmployees();
        for (Employee employee : employees) {
            Object[] row = {
                employee.getEmployeeId(),
                employee.getFirstName() + " " + employee.getLastName(),
                employee.getPosition(),
                employee.getShiftType(),
                employee.getShiftStart(),
                employee.getShiftEnd()
            };
            model.addRow(row);
        }
    }

    private void applyFilter() {
        String filter = (String) filterComboBox.getSelectedItem();
        if (filter == null) return;

        java.time.LocalDate startDate;
        java.time.LocalDate endDate = java.time.LocalDate.now();

        switch (filter) {
            case "Today" -> startDate = endDate;
            case "This Week" -> startDate = endDate.minusWeeks(1);
            case "This Month" -> startDate = endDate.minusMonths(1);
            default -> {
                loadTransactions(); // "All" selected
                return;
            }
        }

        List<Order> filteredOrders = controller.getOrdersByDateRange(
            startDate.toString(),
            endDate.toString()
        );

        updateTransactionsTable(filteredOrders);
    }

    private void filterByDate() {
        String dateStr = dateField.getText().trim();
        if (dateStr.isEmpty()) {
            loadTransactions();
            return;
        }

        try {
            // Validate date format
            java.time.LocalDate.parse(dateStr);
            
            List<Order> filteredOrders = controller.getOrdersByDateRange(dateStr, dateStr);
            updateTransactionsTable(filteredOrders);
        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                "Invalid date format. Please use YYYY-MM-DD format.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTransactionsTable(List<Order> orders) {
        tableModel.setRowCount(0);
        for (Order order : orders) {
            Customer customer = controller.getCustomerById(order.getCustomerId());
            double total = controller.calculateOrderTotal(order.getOrderId());
            Object[] row = {
                order.getOrderId(),
                customer.getFirstName() + " " + customer.getLastName(),
                order.getOrderDateTime(),
                order.getOrderType(),
                order.getOrderStatus(),
                String.format("$%.2f", total)
            };
            tableModel.addRow(row);
        }
    }
} 