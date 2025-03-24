package view;

import controller.RestaurantController;
import model.Order;
import model.OrderItem;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import model.Dish;
import model.Ingredient;
import model.DishIngredient;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

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

        // Create items table with ingredients button
        String[] columnNames = {"Item", "Quantity", "Price", "Total", ""};
        DefaultTableModel itemsModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable itemsTable = new JTable(itemsModel);

        // Create a button column for viewing ingredients
        itemsTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer("View Ingredients"));
        itemsTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));

        // Add mouse listener for ingredient button clicks
        itemsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = itemsTable.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / itemsTable.getRowHeight();

                if (row < itemsTable.getRowCount() && row >= 0 &&
                        column < itemsTable.getColumnCount() && column >= 0) {
                    if (column == 4) {  // Ingredients button column
                        OrderItem item = order.getItems().get(row);
                        showIngredientDetailsDialog(item);
                    }
                }
            }
        });

        for (OrderItem item : order.getItems()) {
            Object[] row = {
                    item.getDishName(),
                    item.getQuantity(),
                    String.format("₱%.2f", item.getPriceAtTime()),
                    String.format("₱%.2f", item.getQuantity() * item.getPriceAtTime()),
                    "View Ingredients"  // Button text
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

        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showIngredientDetailsDialog(OrderItem item) {
        try {
            Dish dish = controller.getDishById(item.getDishId());
            if (dish == null) return;

            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Dish Ingredients", true);
            dialog.setLayout(new BorderLayout(10, 10));

            // Create info panel
            JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel nameLabel = new JLabel("Dish: " + dish.getName());
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
            infoPanel.add(nameLabel);

            JLabel quantityLabel = new JLabel("Quantity Ordered: " + item.getQuantity());
            infoPanel.add(quantityLabel);

            JLabel priceLabel = new JLabel(String.format("Price at Time: ₱%.2f", item.getPriceAtTime()));
            infoPanel.add(priceLabel);

            // Create ingredients table
            String[] columns = {"Ingredient", "Required Amount", "Total Required"};
            DefaultTableModel ingredientModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            JTable ingredientTable = new JTable(ingredientModel);

            // Get ingredients for this dish
            List<DishIngredient> dishIngredients = controller.getDishIngredients(dish.getDishId());
            for (DishIngredient dishIngredient : dishIngredients) {
                Ingredient ingredient = controller.getIngredientById(dishIngredient.getIngredientId());
                if (ingredient != null) {
                    double requiredPerDish = dishIngredient.getQuantityNeeded();
                    double totalRequired = requiredPerDish * item.getQuantity();
                    Object[] row = {
                            ingredient.getName(),
                            String.format("%.2f %s", requiredPerDish, ingredient.getUnitName()),
                            String.format("%.2f %s", totalRequired, ingredient.getUnitName())
                    };
                    ingredientModel.addRow(row);
                }
            }

            // Add recipe instructions if available
            if (dish.getRecipeInstructions() != null && !dish.getRecipeInstructions().trim().isEmpty()) {
                JTextArea recipeArea = new JTextArea(dish.getRecipeInstructions());
                recipeArea.setEditable(false);
                recipeArea.setLineWrap(true);
                recipeArea.setWrapStyleWord(true);
                recipeArea.setBackground(new Color(250, 250, 250));
                recipeArea.setBorder(BorderFactory.createTitledBorder("Recipe Instructions"));

                JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
                contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                contentPanel.add(infoPanel, BorderLayout.NORTH);
                contentPanel.add(new JScrollPane(ingredientTable), BorderLayout.CENTER);
                contentPanel.add(new JScrollPane(recipeArea), BorderLayout.SOUTH);
                dialog.add(contentPanel, BorderLayout.CENTER);
            } else {
                JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
                contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                contentPanel.add(infoPanel, BorderLayout.NORTH);
                contentPanel.add(new JScrollPane(ingredientTable), BorderLayout.CENTER);
                dialog.add(contentPanel, BorderLayout.CENTER);
            }

            // Add close button
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dialog.dispose());
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.add(closeButton);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            // Show dialog
            dialog.pack();
            dialog.setSize(new Dimension(
                    Math.max(dialog.getWidth(), 500),
                    Math.max(dialog.getHeight(), 400)
            ));
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading dish details: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Button renderer for the ingredients button column
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String text) {
            setText(text);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            return this;
        }
    }

    // Button editor for the ingredients button column
    private class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // Handle button click
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
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