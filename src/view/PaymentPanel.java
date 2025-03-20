package view;

import controller.RestaurantController;
import model.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class PaymentPanel extends JPanel {
    private final RestaurantController controller;
    private JTable ordersTable;
    private DefaultTableModel tableModel;

    public PaymentPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initComponents();
        loadUnpaidOrders();
    }

    private void initComponents() {
        // Create toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton processButton = new JButton("Process Payment");
        processButton.addActionListener(e -> processPayment());
        toolBar.add(processButton);
        add(toolBar, BorderLayout.NORTH);

        // Create orders table
        String[] columns = {"Order ID", "Customer", "Date", "Type", "Total", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ordersTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add double-click listener to show order details
        ordersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showOrderDetails();
                }
            }
        });
    }

    private void loadUnpaidOrders() {
        tableModel.setRowCount(0);
        List<Order> orders = controller.getAllOrders();
        
        for (Order order : orders) {
            if ("Unpaid".equals(order.getPaymentStatus())) {
                Customer customer = controller.getCustomerById(order.getCustomerId());
                double total = controller.calculateOrderTotal(order.getOrderId());
                
                Object[] row = {
                    order.getOrderId(),
                    customer.getFirstName() + " " + customer.getLastName(),
                    order.getOrderDateTime(),
                    order.getOrderType(),
                    String.format("$%.2f", total),
                    order.getOrderStatus()
                };
                tableModel.addRow(row);
            }
        }
    }

    private void showOrderDetails() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) return;

        int orderId = (int) tableModel.getValueAt(selectedRow, 0);
        Order order = controller.getOrderById(orderId);
        if (order == null) return;

        // Create dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Order Details", true);
        dialog.setLayout(new BorderLayout());

        // Create order info panel
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        Customer customer = controller.getCustomerById(order.getCustomerId());
        infoPanel.add(new JLabel("Customer:"));
        infoPanel.add(new JLabel(customer.getFirstName() + " " + customer.getLastName()));
        infoPanel.add(new JLabel("Order Date:"));
        infoPanel.add(new JLabel(order.getOrderDateTime().toString()));
        infoPanel.add(new JLabel("Order Type:"));
        infoPanel.add(new JLabel(order.getOrderType()));
        infoPanel.add(new JLabel("Status:"));
        infoPanel.add(new JLabel(order.getOrderStatus()));
        dialog.add(infoPanel, BorderLayout.NORTH);

        // Create items table
        String[] columns = {"Product", "Quantity", "Unit Price", "Subtotal"};
        DefaultTableModel itemsModel = new DefaultTableModel(columns, 0);
        JTable itemsTable = new JTable(itemsModel);
        
        double total = 0;
        for (OrderItem item : order.getItems()) {
            Inventory product = controller.getInventoryItemById(item.getProductId());
            double subtotal = item.getQuantity() * item.getPriceAtTime();
            Object[] row = {
                product.getProductName(),
                item.getQuantity(),
                String.format("$%.2f", item.getPriceAtTime()),
                String.format("$%.2f", subtotal)
            };
            itemsModel.addRow(row);
            total += subtotal;
        }
        
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Create total panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.add(new JLabel("Total: " + String.format("$%.2f", total)));
        dialog.add(totalPanel, BorderLayout.SOUTH);

        // Set dialog properties
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void processPayment() {
        int selectedRow = ordersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an order to process payment.",
                "No Order Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int orderId = (int) tableModel.getValueAt(selectedRow, 0);
        Order order = controller.getOrderById(orderId);
        if (order == null) {
            JOptionPane.showMessageDialog(this,
                "Could not find order details.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("Paid".equals(order.getPaymentStatus())) {
            JOptionPane.showMessageDialog(this,
                "This order has already been paid.",
                "Already Paid",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Calculate total
        double total = controller.calculateOrderTotal(orderId);

        // Create payment dialog
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Process Payment", true);
        dialog.setLayout(new BorderLayout());

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add total
        formPanel.add(new JLabel("Total Amount:"));
        formPanel.add(new JLabel(String.format("$%.2f", total)));

        // Add payment method selection
        formPanel.add(new JLabel("Payment Method:"));
        String[] methods = {"Cash", "Credit Card", "Debit Card"};
        JComboBox<String> methodCombo = new JComboBox<>(methods);
        formPanel.add(methodCombo);

        // Add amount received field (for cash payments)
        formPanel.add(new JLabel("Amount Received:"));
        JTextField amountField = new JTextField();
        formPanel.add(amountField);

        // Add change due field
        formPanel.add(new JLabel("Change Due:"));
        JLabel changeLabel = new JLabel("$0.00");
        formPanel.add(changeLabel);

        // Update change when amount received changes
        amountField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateChange() {
                try {
                    double received = Double.parseDouble(amountField.getText());
                    double change = received - total;
                    changeLabel.setText(String.format("$%.2f", Math.max(0, change)));
                } catch (NumberFormatException ex) {
                    changeLabel.setText("$0.00");
                }
            }

            @Override
            public void insertUpdate(DocumentEvent e) { updateChange(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateChange(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateChange(); }
        });

        // Enable/disable amount field based on payment method
        methodCombo.addActionListener(e -> {
            boolean isCash = "Cash".equals(methodCombo.getSelectedItem());
            amountField.setEnabled(isCash);
            if (!isCash) {
                amountField.setText("");
                changeLabel.setText("$0.00");
            }
        });

        dialog.add(formPanel, BorderLayout.CENTER);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton processButton = new JButton("Process");
        JButton cancelButton = new JButton("Cancel");

        processButton.addActionListener(e -> {
            try {
                double amountReceived = 0.0;
                if ("Cash".equals(methodCombo.getSelectedItem())) {
                    amountReceived = Double.parseDouble(amountField.getText());
                    if (amountReceived < total) {
                        JOptionPane.showMessageDialog(dialog,
                            "Insufficient amount received.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                // Process the payment
                if (controller.processPayment(orderId, amountReceived, methodCombo.getSelectedItem().toString())) {
                    JOptionPane.showMessageDialog(dialog,
                        "Payment processed successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadUnpaidOrders(); // Refresh the table
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Failed to process payment.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Invalid amount entered.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(processButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Show dialog
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
} 