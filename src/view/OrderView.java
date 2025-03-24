package view;

import controller.OrderController;
import model.Order;
import model.OrderItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class OrderView extends JFrame {
    private OrderController orderController;
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> orderTypeCombo;
    private JComboBox<String> orderStatusCombo;
    private JComboBox<String> paymentMethodCombo;
    private JComboBox<String> paymentStatusCombo;
    private JTextField customerIdField;
    private JTextField tableIdField;
    private JTextArea itemsArea;
    private JTextArea assignedEmployeesArea;

    public OrderView() {
        try {
            this.orderController = new OrderController();
            initializeUI();
            loadOrders();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error initializing order view: " + e.getMessage());
        }
    }

    private void initializeUI() {
        setTitle("Order Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel);

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add form fields
        formPanel.add(new JLabel("Customer ID:"));
        customerIdField = new JTextField();
        formPanel.add(customerIdField);

        formPanel.add(new JLabel("Order Type:"));
        orderTypeCombo = new JComboBox<>(new String[]{"Dine-In", "Takeout", "Delivery"});
        formPanel.add(orderTypeCombo);

        formPanel.add(new JLabel("Order Status:"));
        orderStatusCombo = new JComboBox<>(new String[]{"In Progress", "Ready", "Served", "Completed", "Cancelled"});
        formPanel.add(orderStatusCombo);

        formPanel.add(new JLabel("Payment Method:"));
        paymentMethodCombo = new JComboBox<>(new String[]{"Cash", "Credit Card"});
        formPanel.add(paymentMethodCombo);

        formPanel.add(new JLabel("Payment Status:"));
        paymentStatusCombo = new JComboBox<>(new String[]{"Pending", "Paid"});
        formPanel.add(paymentStatusCombo);

        formPanel.add(new JLabel("Table ID:"));
        tableIdField = new JTextField();
        formPanel.add(tableIdField);

        formPanel.add(new JLabel("Items (JSON):"));
        itemsArea = new JTextArea(3, 20);
        formPanel.add(new JScrollPane(itemsArea));

        formPanel.add(new JLabel("Assigned Employees (comma-separated):"));
        assignedEmployeesArea = new JTextArea(3, 20);
        formPanel.add(new JScrollPane(assignedEmployeesArea));

        // Create button panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Order");
        JButton updateButton = new JButton("Update Order");
        JButton deleteButton = new JButton("Delete Order");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        // Create table
        String[] columns = {"ID", "Customer", "Type", "Status", "Total", "Payment", "Table"};
        tableModel = new DefaultTableModel(columns, 0);
        orderTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(orderTable);

        // Add components to main panel
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add button listeners
        addButton.addActionListener(e -> handleAddOrder());
        updateButton.addActionListener(e -> handleUpdateOrder());
        deleteButton.addActionListener(e -> handleDeleteOrder());
        refreshButton.addActionListener(e -> loadOrders());
    }

    private void loadOrders() {
        try {
            List<Order> orders = orderController.getAllActiveOrders();
            tableModel.setRowCount(0);
            
            for (Order order : orders) {
                tableModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getCustomerId(),
                    order.getOrderType(),
                    order.getOrderStatus(),
                    String.format("₱%.2f", order.getTotalAmount()),
                    order.getPaymentStatus(),
                    order.getTableId()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + e.getMessage());
        }
    }

    private void handleAddOrder() {
        try {
            // Get values from form
            int customerId = Integer.parseInt(customerIdField.getText());
            String orderType = (String) orderTypeCombo.getSelectedItem();
            String orderStatus = (String) orderStatusCombo.getSelectedItem();
            String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
            String paymentStatus = (String) paymentStatusCombo.getSelectedItem();
            int tableId = Integer.parseInt(tableIdField.getText());

            // Create order object
            Order order = new Order();
            order.setCustomerId(customerId);
            order.setOrderType(orderType);
            order.setOrderStatus(orderStatus);
            order.setPaymentMethod(paymentMethod);
            order.setPaymentStatus(paymentStatus);
            order.setTableId(tableId);

            // Add order
            int orderId = orderController.createOrder(order);
            JOptionPane.showMessageDialog(this, "Order created successfully with ID: " + orderId);
            
            // Clear form and refresh table
            clearForm();
            loadOrders();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating order: " + e.getMessage());
        }
    }

    private void handleUpdateOrder() {
        try {
            int selectedRow = orderTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an order to update");
                return;
            }

            int orderId = (int) orderTable.getValueAt(selectedRow, 0);
            String newStatus = (String) orderStatusCombo.getSelectedItem();
            String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
            String paymentStatus = (String) paymentStatusCombo.getSelectedItem();

            orderController.updateOrderStatus(orderId, newStatus);
            orderController.updatePaymentStatus(orderId, paymentMethod, paymentStatus);

            JOptionPane.showMessageDialog(this, "Order updated successfully");
            loadOrders();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating order: " + e.getMessage());
        }
    }

    private void handleDeleteOrder() {
        try {
            int selectedRow = orderTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an order to delete");
                return;
            }

            int orderId = (int) orderTable.getValueAt(selectedRow, 0);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this order?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                orderController.deleteOrder(orderId);
                JOptionPane.showMessageDialog(this, "Order deleted successfully");
                loadOrders();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting order: " + e.getMessage());
        }
    }

    private void clearForm() {
        customerIdField.setText("");
        orderTypeCombo.setSelectedIndex(0);
        orderStatusCombo.setSelectedIndex(0);
        paymentMethodCombo.setSelectedIndex(0);
        paymentStatusCombo.setSelectedIndex(0);
        tableIdField.setText("");
        itemsArea.setText("");
        assignedEmployeesArea.setText("");
    }
} 