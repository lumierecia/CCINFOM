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
import javax.swing.ListSelectionModel;

public class PaymentPanel extends JPanel {
    private final RestaurantController controller;
    private JTable ordersTable;
    private DefaultTableModel tableModel;

    public PaymentPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        initComponents();
        loadUnpaidOrders();
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font(button.getFont().getName(), Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(backgroundColor.darker(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(backgroundColor.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });

        return button;
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton processButton = createStyledButton("Process Payment", new Color(40, 167, 69));
        processButton.addActionListener(e -> processPayment());
        
        JButton refreshButton = createStyledButton("Refresh", new Color(70, 130, 180));
        refreshButton.addActionListener(e -> loadUnpaidOrders());
        
        JButton helpButton = createStyledButton("Help", new Color(108, 117, 125));
        helpButton.addActionListener(e -> showHelpDialog());
        
        buttonPanel.add(processButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(helpButton);
        
        add(buttonPanel, BorderLayout.NORTH);

        // Create orders table
        String[] columns = {
            "Customer Name", "Order Type", "Order Date", 
            "Total Amount", "Status"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        ordersTable = new JTable(tableModel);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ordersTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        ordersTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Customer Name
        ordersTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Order Type
        ordersTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Order Date
        ordersTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Total Amount
        ordersTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Status

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
            if ("Pending".equals(order.getPaymentStatus())) {
                Customer customer = controller.getCustomerById(order.getCustomerId());
                double total = controller.calculateOrderTotal(order.getOrderId());
                
                Object[] row = {
                    customer.getFirstName() + " " + customer.getLastName(),
                    order.getOrderType(),
                    order.getOrderDateTime(),
                    String.format("₱%.2f", order.getTotalAmount()),
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
        dialog.setLayout(new BorderLayout(10, 10));

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add order details
        formPanel.add(new JLabel("Order ID:"));
        formPanel.add(new JLabel(String.valueOf(orderId)));
        
        formPanel.add(new JLabel("Customer:"));
        formPanel.add(new JLabel((String) tableModel.getValueAt(selectedRow, 1)));
        
        formPanel.add(new JLabel("Order Type:"));
        formPanel.add(new JLabel((String) tableModel.getValueAt(selectedRow, 2)));

        // Add total with bold font
        JLabel totalLabel = new JLabel("Total Amount:");
        JLabel totalValueLabel = new JLabel(String.format("$%.2f", total));
        Font boldFont = totalValueLabel.getFont().deriveFont(Font.BOLD);
        totalValueLabel.setFont(boldFont);
        formPanel.add(totalLabel);
        formPanel.add(totalValueLabel);

        // Add payment method selection
        formPanel.add(new JLabel("Payment Method:"));
        String[] methods = {"Cash", "Credit Card"};
        JComboBox<String> methodCombo = new JComboBox<>(methods);
        formPanel.add(methodCombo);

        // Add amount received field (for cash payments)
        JLabel amountLabel = new JLabel("Amount Received:");
        JTextField amountField = new JTextField();
        formPanel.add(amountLabel);
        formPanel.add(amountField);

        // Add change due field
        JLabel changeDueLabel = new JLabel("Change Due:");
        JLabel changeValueLabel = new JLabel("$0.00");
        changeValueLabel.setFont(boldFont);
        formPanel.add(changeDueLabel);
        formPanel.add(changeValueLabel);

        // Update change when amount received changes
        amountField.getDocument().addDocumentListener(new DocumentListener() {
            private void updateChange() {
                try {
                    double received = Double.parseDouble(amountField.getText().trim());
                    double change = received - total;
                    changeValueLabel.setText(String.format("$%.2f", Math.max(0, change)));
                    changeValueLabel.setForeground(change >= 0 ? Color.BLACK : Color.RED);
                } catch (NumberFormatException ex) {
                    changeValueLabel.setText("$0.00");
                    changeValueLabel.setForeground(Color.BLACK);
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
            amountLabel.setEnabled(isCash);
            changeDueLabel.setEnabled(isCash);
            changeValueLabel.setEnabled(isCash);
            if (!isCash) {
                amountField.setText("");
                changeValueLabel.setText("$0.00");
                changeValueLabel.setForeground(Color.BLACK);
            }
        });

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton processButton = new JButton("Process Payment");
        JButton cancelButton = new JButton("Cancel");

        processButton.addActionListener(e -> {
            try {
                String selectedMethod = (String) methodCombo.getSelectedItem();
                double amountReceived;
                
                if ("Cash".equals(selectedMethod)) {
                    if (amountField.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(dialog,
                            "Please enter the amount received.",
                            "Missing Amount",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    amountReceived = Double.parseDouble(amountField.getText().trim());
                    if (amountReceived < total) {
                        JOptionPane.showMessageDialog(dialog,
                            "Insufficient amount received.",
                            "Invalid Amount",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } else {
                    // For credit card, amount received equals total
                    amountReceived = total;
                }
                
                // Process the payment
                if (controller.processPayment(orderId, amountReceived, selectedMethod)) {
                    JOptionPane.showMessageDialog(dialog,
                        String.format("""
                            Payment processed successfully!
                            
                            Order ID: %d
                            Total Amount: $%.2f
                            Payment Method: %s%s
                            """,
                            orderId,
                            total,
                            selectedMethod,
                            "Cash".equals(selectedMethod) 
                                ? String.format("\nAmount Received: $%.2f\nChange Due: $%.2f",
                                    amountReceived,
                                    amountReceived - total)
                                : ""),
                        "Payment Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadUnpaidOrders(); // Refresh the table
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Please enter a valid amount.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(processButton);
        buttonPanel.add(cancelButton);

        // Add components to dialog
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Show dialog
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showHelpDialog() {
        HelpDialog helpDialog = new HelpDialog(SwingUtilities.getWindowAncestor(this), "Payments");
        helpDialog.setVisible(true);
    }
} 