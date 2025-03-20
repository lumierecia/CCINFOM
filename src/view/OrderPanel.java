package view;

import controller.RestaurantController;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class OrderPanel extends JPanel {
    private final RestaurantController controller;
    private JTable orderItemsTable;
    private DefaultTableModel itemsModel;
    private JComboBox<String> categoryCombo;
    private JComboBox<Inventory> productCombo;
    private JSpinner quantitySpinner;
    private JLabel totalLabel;
    private double total = 0.0;
    private List<OrderItem> orderItems = new ArrayList<>();

    public OrderPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // Create main panels
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Setup category selection
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        categoryPanel.add(new JLabel("Category:"));
        categoryCombo = new JComboBox<>(controller.getAllCategories().toArray(new String[0]));
        categoryCombo.addActionListener(e -> updateProductCombo());
        categoryPanel.add(categoryCombo);

        // Setup product selection
        JPanel productPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        productPanel.add(new JLabel("Product:"));
        productCombo = new JComboBox<>();
        updateProductCombo();
        productPanel.add(productCombo);

        // Setup quantity selection
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        quantityPanel.add(new JLabel("Quantity:"));
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        quantityPanel.add(quantitySpinner);

        // Add item button
        JButton addButton = new JButton("Add to Order");
        addButton.addActionListener(e -> addItemToOrder());

        // Combine top components
        topPanel.add(categoryPanel, BorderLayout.WEST);
        topPanel.add(productPanel, BorderLayout.CENTER);
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rightPanel.add(quantityPanel);
        rightPanel.add(addButton);
        topPanel.add(rightPanel, BorderLayout.EAST);

        // Setup order items table
        String[] columns = {"Product", "Quantity", "Unit Price", "Subtotal"};
        itemsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderItemsTable = new JTable(itemsModel);
        JScrollPane scrollPane = new JScrollPane(orderItemsTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Setup bottom panel with total and buttons
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalLabel = new JLabel("Total: $0.00");
        totalPanel.add(totalLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(e -> removeSelectedItem());
        JButton clearButton = new JButton("Clear All");
        clearButton.addActionListener(e -> clearOrder());
        JButton placeOrderButton = new JButton("Place Order");
        placeOrderButton.addActionListener(e -> placeOrder());

        buttonPanel.add(removeButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(placeOrderButton);

        bottomPanel.add(totalPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // Add all panels to main panel
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void updateProductCombo() {
        productCombo.removeAllItems();
        String category = (String) categoryCombo.getSelectedItem();
        if (category != null) {
            List<Inventory> products = controller.getInventoryItemsByCategory(category);
            for (Inventory product : products) {
                if (product.getQuantity() > 0) {
                    productCombo.addItem(product);
                }
            }
        }
    }

    private void addItemToOrder() {
        Inventory product = (Inventory) productCombo.getSelectedItem();
        if (product == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a product to add.",
                "No Product Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity = (int) quantitySpinner.getValue();
        if (quantity > product.getQuantity()) {
            JOptionPane.showMessageDialog(this,
                "Not enough stock available.",
                "Insufficient Stock",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if product already exists in order
        for (int i = 0; i < itemsModel.getRowCount(); i++) {
            if (itemsModel.getValueAt(i, 0).equals(product.getProductName())) {
                int currentQty = (int) itemsModel.getValueAt(i, 1);
                if (currentQty + quantity > product.getQuantity()) {
                    JOptionPane.showMessageDialog(this,
                        "Not enough stock available.",
                        "Insufficient Stock",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                itemsModel.setValueAt(currentQty + quantity, i, 1);
                double subtotal = (currentQty + quantity) * product.getSellPrice();
                itemsModel.setValueAt(String.format("$%.2f", subtotal), i, 3);
                updateTotal();
                return;
            }
        }

        // Add new item
        double subtotal = quantity * product.getSellPrice();
        Object[] row = {
            product.getProductName(),
            quantity,
            String.format("$%.2f", product.getSellPrice()),
            String.format("$%.2f", subtotal)
        };
        itemsModel.addRow(row);

        // Add to order items list
        OrderItem item = new OrderItem(
            0,  // order_id (will be set when order is created)
            product.getProductId(),
            quantity,
            product.getSellPrice()  // price_at_time
        );
        orderItems.add(item);

        updateTotal();
    }

    private void removeSelectedItem() {
        int selectedRow = orderItemsTable.getSelectedRow();
        if (selectedRow != -1) {
            orderItems.remove(selectedRow);
            itemsModel.removeRow(selectedRow);
            updateTotal();
        }
    }

    private void clearOrder() {
        itemsModel.setRowCount(0);
        orderItems.clear();
        updateTotal();
    }

    private void updateTotal() {
        total = 0.0;
        for (OrderItem item : orderItems) {
            total += item.getQuantity() * item.getPriceAtTime();
        }
        totalLabel.setText(String.format("Total: $%.2f", total));
    }

    private void placeOrder() {
        if (orderItems.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please add items to the order first.",
                "Empty Order",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get customer
        Customer customer = getCustomer();
        if (customer == null) return;

        // Get order type
        String[] types = {"Dine-in", "Takeout", "Delivery"};
        String type = (String) JOptionPane.showInputDialog(this,
            "Select order type:",
            "Order Type",
            JOptionPane.QUESTION_MESSAGE,
            null,
            types,
            types[0]);
        if (type == null) return;

        // Get assigned employees
        List<Integer> employeeIds = getAssignedEmployees();
        if (employeeIds == null) return;

        // Create order
        Order order = new Order(
            0,  // order_id (auto-generated)
            customer.getCustomerId(),
            new java.sql.Timestamp(System.currentTimeMillis()),  // order_datetime
            type.equals("Dine-in") ? "Dine-In" : type,  // match ENUM case exactly
            "In Progress",  // order_status
            "Pending",     // payment_status
            total,         // total_amount
            null          // payment_method (will be set during payment)
        );

        // Place order
        if (controller.createOrder(order, orderItems, employeeIds)) {
            JOptionPane.showMessageDialog(this,
                "Order placed successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            clearOrder();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to place order. Please try again.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private Customer getCustomer() {
        // Show dialog to get customer info
        String[] options = {"Existing Customer", "New Customer"};
        int choice = JOptionPane.showOptionDialog(this,
            "Select customer type:",
            "Customer Selection",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        if (choice == 0) {
            // Show existing customer selection dialog
            List<Customer> customers = controller.getAllCustomers();
            Customer selected = (Customer) JOptionPane.showInputDialog(this,
                "Select customer:",
                "Customer Selection",
                JOptionPane.QUESTION_MESSAGE,
                null,
                customers.toArray(),
                null);
            return selected;
        } else if (choice == 1) {
            // Show new customer dialog
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

            int option = JOptionPane.showConfirmDialog(this,
                message,
                "New Customer",
                JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                Customer newCustomer = new Customer(
                    0,
                    firstNameField.getText(),
                    lastNameField.getText(),
                    emailField.getText(),
                    phoneField.getText(),
                    addressField.getText()
                );

                int customerId = controller.addCustomer(newCustomer);
                if (customerId != -1) {
                    newCustomer.setCustomerId(customerId);
                    return newCustomer;
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to create customer.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        return null;
    }

    private List<Integer> getAssignedEmployees() {
        List<Employee> employees = controller.getAllEmployees();
        List<Integer> selectedIds = new ArrayList<>();

        // Create checkboxes for each employee
        List<JCheckBox> checkboxes = new ArrayList<>();
        for (Employee emp : employees) {
            JCheckBox cb = new JCheckBox(emp.getFirstName() + " " + emp.getLastName());
            cb.setActionCommand(String.valueOf(emp.getEmployeeId()));
            checkboxes.add(cb);
        }

        // Show dialog with employee checkboxes
        int result = JOptionPane.showConfirmDialog(this,
            checkboxes.toArray(),
            "Select Employees",
            JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            for (JCheckBox cb : checkboxes) {
                if (cb.isSelected()) {
                    selectedIds.add(Integer.parseInt(cb.getActionCommand()));
                }
            }
            if (selectedIds.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please select at least one employee.",
                    "No Employees Selected",
                    JOptionPane.WARNING_MESSAGE);
                return null;
            }
            return selectedIds;
        }
        return null;
    }
} 