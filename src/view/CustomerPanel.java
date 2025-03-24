package view;

import controller.RestaurantController;
import model.Customer;
import util.StyledComponents;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

public class CustomerPanel extends JPanel {
    private JTable customerTable;
    private DefaultTableModel tableModel;
    private RestaurantController controller;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton viewOrdersButton;
    private JButton refreshButton;
    private JButton helpButton;
    private List<Integer> customerIds = new ArrayList<>();

    public CustomerPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initComponents();
        loadCustomers();
    }

    private void initComponents() {
        // Create toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = StyledComponents.createStyledButton("Add Customer", new Color(40, 167, 69));
        editButton = StyledComponents.createStyledButton("Edit Customer", new Color(255, 193, 7));
        deleteButton = StyledComponents.createStyledButton("Delete Customer", new Color(220, 53, 69));
        viewOrdersButton = StyledComponents.createStyledButton("View Orders", new Color(70, 130, 180));
        refreshButton = StyledComponents.createStyledButton("Refresh", new Color(108, 117, 125));

        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.add(viewOrdersButton);
        toolBar.add(refreshButton);

        // Create help button
        helpButton = StyledComponents.createStyledButton("Help", new Color(108, 117, 125));
        helpButton.addActionListener(e -> showHelp());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(helpButton);
        add(topPanel, BorderLayout.NORTH);

        // Create table
        String[] columnNames = {"First Name", "Last Name", "Email", "Phone", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(customerTable);

        // Add components to panel
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add listeners
        addButton.addActionListener(e -> showCustomerDialog(null));
        editButton.addActionListener(e -> {
            int selectedRow = customerTable.getSelectedRow();
            if (selectedRow != -1) {
                int customerId = customerIds.get(selectedRow);
                Customer customer = controller.getCustomerById(customerId);
                if (customer != null) {
                    showCustomerDialog(customer);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a customer to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        deleteButton.addActionListener(e -> {
            int selectedRow = customerTable.getSelectedRow();
            if (selectedRow != -1) {
                int customerId = customerIds.get(selectedRow);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this customer?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (controller.deleteCustomer(customerId)) {
                        loadCustomers();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Failed to delete customer.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a customer to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        viewOrdersButton.addActionListener(e -> {
            int selectedRow = customerTable.getSelectedRow();
            if (selectedRow != -1) {
                int customerId = customerIds.get(selectedRow);
                showCustomerOrders(customerId);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a customer to view orders.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        refreshButton.addActionListener(e -> loadCustomers());
    }

    private void loadCustomers() {
        tableModel.setRowCount(0);
        customerIds.clear();
        List<Customer> customers = controller.getAllCustomers();
        for (Customer customer : customers) {
            customerIds.add(customer.getCustomerId());
            Object[] row = {
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress()
            };
            tableModel.addRow(row);
        }
    }

    private void showCustomerDialog(Customer customer) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            customer == null ? "Add Customer" : "Edit Customer",
            true);
        dialog.setLayout(new BorderLayout());

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Create form fields
        JTextField firstNameField = new JTextField(20);
        JTextField lastNameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField addressField = new JTextField(20);

        // If editing, populate fields
        if (customer != null) {
            firstNameField.setText(customer.getFirstName());
            lastNameField.setText(customer.getLastName());
            emailField.setText(customer.getEmail());
            phoneField.setText(customer.getPhone());
            addressField.setText(customer.getAddress());
        }

        // Add components to form
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(firstNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(lastNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        formPanel.add(addressField, gbc);

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton(customer == null ? "Add" : "Save");
        JButton cancelButton = new JButton("Cancel");
        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);

        // Add action listeners
        saveButton.addActionListener(e -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please fill in all required fields.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            Customer newCustomer = new Customer(
                customer != null ? customer.getCustomerId() : 0,
                firstName,
                lastName,
                email,
                phone,
                address
            );

            boolean success;
            if (customer == null) {
                success = controller.addCustomer(newCustomer) > 0;
            } else {
                success = controller.updateCustomer(newCustomer);
            }

            if (success) {
                loadCustomers();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Failed to " + (customer == null ? "add" : "update") + " customer.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        // Add panels to dialog
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonsPanel, BorderLayout.SOUTH);

        // Show dialog
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showCustomerOrders(int customerId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Order History", true);
        dialog.setLayout(new BorderLayout());

        // Create table
        String[] columnNames = {"Order ID", "Date", "Total", "Status"};
        DefaultTableModel orderModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable orderTable = new JTable(orderModel);
        JScrollPane scrollPane = new JScrollPane(orderTable);

        // Load orders
        List<model.Order> orders = controller.getCustomerOrders(customerId);
        for (model.Order order : orders) {
            Object[] row = {
                order.getOrderId(),
                order.getOrderDateTime(),
                String.format("$%.2f", controller.calculateOrderTotal(order.getOrderId())),
                order.getOrderStatus()
            };
            orderModel.addRow(row);
        }

        // Add close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);

        // Add components to dialog
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Show dialog
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showHelp() {
        String helpText = """
            Customer Management Help:
            
            1. Adding Customers:
               • Click "Add Customer" to create new customer
               • Fill in all required fields
               • Click "Add" to save
            
            2. Managing Customers:
               • Select a customer to edit or delete
               • Click "Edit Customer" to modify details
               • Click "Delete Customer" to remove
            
            3. Viewing Orders:
               • Select a customer
               • Click "View Orders" to see order history
               • Orders show date, total, and status
            
            4. Additional Features:
               • Use "Refresh" to update the list
               • Search and filter options available
               • Customer details are saved automatically
            """;
        
        JOptionPane.showMessageDialog(this,
            helpText,
            "Customer Management Help",
            JOptionPane.INFORMATION_MESSAGE);
    }
} 