package view;

import controller.RestaurantController;
import model.Order;
import model.OrderItem;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;

public class OrderHistoryPanel extends JPanel {
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private RestaurantController controller;
    private JButton viewButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton helpButton;
    private List<Integer> orderIds = new ArrayList<>();

    public OrderHistoryPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initComponents();
        loadOrders();
    }

    private void initComponents() {
        // Create toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        viewButton = createStyledButton("View Details", new Color(70, 130, 180));
        deleteButton = createStyledButton("Delete Order", new Color(220, 53, 69));
        refreshButton = createStyledButton("Refresh", new Color(108, 117, 125));
        helpButton = createStyledButton("Help", new Color(23, 162, 184));

        toolBar.add(viewButton);
        toolBar.add(deleteButton);
        toolBar.add(refreshButton);
        toolBar.add(helpButton);

        // Create table
        String[] columnNames = {
            "Order Date", "Customer Name", "Order Type", 
            "Status", "Total Amount", "Payment Status"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(orderTable);

        // Add components to panel
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add listeners
        viewButton.addActionListener(e -> {
            int selectedRow = orderTable.getSelectedRow();
            if (selectedRow != -1) {
                int orderId = orderIds.get(selectedRow);
                showOrderDetailsDialog(orderId);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select an order to view.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = orderTable.getSelectedRow();
            if (selectedRow != -1) {
                int orderId = orderIds.get(selectedRow);
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this order?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (controller.deleteOrder(orderId)) {
                        loadOrders();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Failed to delete order.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select an order to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshButton.addActionListener(e -> loadOrders());
        helpButton.addActionListener(e -> showHelpDialog());
    }

    private void loadOrders() {
        tableModel.setRowCount(0);
        orderIds.clear();
        List<Order> orders = controller.getAllOrders();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (Order order : orders) {
            orderIds.add(order.getOrderId());
            Object[] row = {
                dateFormat.format(order.getOrderDateTime()),
                order.getCustomerName(),
                order.getOrderType(),
                order.getOrderStatus(),
                String.format("₱%.2f", order.getTotalAmount()),
                order.getPaymentStatus()
            };
            tableModel.addRow(row);
        }
    }

    private void showOrderDetailsDialog(int orderId) {
        Order order = controller.getOrderById(orderId);
        if (order == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Order Details", true);
        dialog.setLayout(new BorderLayout(10, 10));
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dialog.setContentPane(contentPane);

        // Create info panel
        JPanel infoPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        infoPanel.add(new JLabel("Order ID:"));
        infoPanel.add(new JLabel(String.valueOf(order.getOrderId())));
        infoPanel.add(new JLabel("Customer:"));
        infoPanel.add(new JLabel(order.getCustomerName()));
        infoPanel.add(new JLabel("Order Type:"));
        infoPanel.add(new JLabel(order.getOrderType()));
        infoPanel.add(new JLabel("Status:"));
        infoPanel.add(new JLabel(order.getOrderStatus()));
        infoPanel.add(new JLabel("Payment Status:"));
        infoPanel.add(new JLabel(order.getPaymentStatus()));
        infoPanel.add(new JLabel("Total Amount:"));
        infoPanel.add(new JLabel(String.format("₱%.2f", order.getTotalAmount())));

        // Create items table
        String[] columnNames = {"Item", "Quantity", "Price", "Total"};
        DefaultTableModel itemsModel = new DefaultTableModel(columnNames, 0);
        JTable itemsTable = new JTable(itemsModel);

        for (OrderItem item : order.getItems()) {
            Object[] row = {
                item.getProductName(),
                item.getQuantity(),
                String.format("₱%.2f", item.getPriceAtTime()),
                String.format("₱%.2f", item.getQuantity() * item.getPriceAtTime())
            };
            itemsModel.addRow(row);
        }

        // Add components to dialog
        contentPane.add(infoPanel, BorderLayout.NORTH);
        contentPane.add(new JScrollPane(itemsTable), BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showHelpDialog() {
        HelpDialog helpDialog = new HelpDialog(SwingUtilities.getWindowAncestor(this), "Order History");
        helpDialog.setVisible(true);
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

        button.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

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
} 