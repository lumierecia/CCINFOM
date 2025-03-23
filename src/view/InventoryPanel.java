package view;

import controller.RestaurantController;
import model.Inventory;
import util.DatabaseErrorHandler;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.sql.SQLException;
import java.util.ArrayList;

public class InventoryPanel extends JPanel {
    private final RestaurantController controller;
    private final JTable inventoryTable;
    private final DefaultTableModel tableModel;
    private final JSpinner lowStockThreshold;
    private final JCheckBox showLowStockOnly;
    private final JTextField searchField;
    private final JPanel categoryPanel;
    private final JButton helpButton;

    public InventoryPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        // Create help button
        helpButton = new JButton("Help");
        helpButton.setIcon(new ImageIcon(getClass().getResource("/icons/help.png")));
        helpButton.addActionListener(e -> showHelp());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(helpButton);
        add(topPanel, BorderLayout.NORTH);

        // Create the table model with non-editable cells
        String[] columns = {
            "Name", "Category", "Quantity", "Make Price", "Sell Price", "Status", "Actions"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only allow editing the Actions column
            }
        };
        
        // Create and set up the table
        inventoryTable = new JTable(tableModel);
        inventoryTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        inventoryTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Create top control panel
        JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
        
        // Create category filter panel with toggle buttons
        categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        categoryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        ButtonGroup categoryGroup = new ButtonGroup();
        
        // Add toggle button for each category
        for (String category : controller.getAllCategories()) {
            JToggleButton categoryBtn = new JToggleButton(category);
            styleToggleButton(categoryBtn);
            categoryGroup.add(categoryBtn);
            categoryPanel.add(categoryBtn);
        }
        
        // Select first category by default
        if (categoryPanel.getComponents().length > 0) {
            ((JToggleButton)categoryPanel.getComponents()[0]).setSelected(true);
        }
        
        // Add action listener to all toggle buttons
        for (Component comp : categoryPanel.getComponents()) {
            if (comp instanceof JToggleButton) {
                ((JToggleButton) comp).addActionListener(e -> refreshTable());
            }
        }
        
        // Create other controls panel
        JPanel otherControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Low stock filter
        lowStockThreshold = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
        showLowStockOnly = new JCheckBox("Show Low Stock Only");
        showLowStockOnly.addActionListener(e -> refreshTable());
        
        // Search field
        searchField = new JTextField(20);
        searchField.addActionListener(e -> refreshTable());
        
        // Add components to other controls panel
        otherControlsPanel.add(new JLabel("Low Stock Threshold:"));
        otherControlsPanel.add(lowStockThreshold);
        otherControlsPanel.add(showLowStockOnly);
        otherControlsPanel.add(new JLabel("Search:"));
        otherControlsPanel.add(searchField);
        
        // Add refresh button
        JButton refreshButton = createStyledButton("Refresh", new Color(70, 130, 180));
        refreshButton.addActionListener(e -> refreshTable());
        otherControlsPanel.add(refreshButton);
        
        // Add panels to control panel
        controlPanel.add(categoryPanel, BorderLayout.NORTH);
        controlPanel.add(otherControlsPanel, BorderLayout.CENTER);
        
        // Add components to main panel
        add(controlPanel, BorderLayout.NORTH);
        add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        
        // Initial table load
        refreshTable();
    }

    private void styleToggleButton(JToggleButton button) {
        button.setFont(new Font(button.getFont().getName(), Font.PLAIN, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // Add hover and selection effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!button.isSelected()) {
                    button.setBackground(new Color(240, 240, 240));
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!button.isSelected()) {
                    button.setBackground(UIManager.getColor("Button.background"));
                }
            }
        });
        
        // Set selection colors
        button.addChangeListener(e -> {
            if (button.isSelected()) {
                button.setBackground(new Color(70, 130, 180));
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(UIManager.getColor("Button.background"));
                button.setForeground(Color.BLACK);
            }
        });
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font(button.getFont().getName(), Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(backgroundColor.darker(), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(backgroundColor.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });

        return button;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Inventory> items;
        
        if (showLowStockOnly.isSelected()) {
            items = controller.getLowStockItems((Integer) lowStockThreshold.getValue());
        } else {
            // Find selected category button
            String selectedCategory = null;
            for (Component comp : categoryPanel.getComponents()) {
                if (comp instanceof JToggleButton && ((JToggleButton) comp).isSelected()) {
                    selectedCategory = ((JToggleButton) comp).getText();
                    break;
                }
            }
            
            if (selectedCategory != null) {
                items = controller.getInventoryItemsByCategory(selectedCategory);
            } else {
                items = controller.getAllInventoryItems();
            }
        }
        
        String searchTerm = searchField.getText().toLowerCase().trim();
        for (Inventory item : items) {
            if (searchTerm.isEmpty() || 
                item.getProductName().toLowerCase().contains(searchTerm) ||
                item.getCategoryName().toLowerCase().contains(searchTerm)) {
                
                Object[] row = {
                    item.getProductName(),
                    item.getCategoryName(),
                    item.getQuantity(),
                    String.format("%.2f", item.getMakePrice()),
                    String.format("%.2f", item.getSellPrice()),
                    item.getStatus(),
                    "Restock"
                };
                tableModel.addRow(row);
            }
        }
    }

    // Custom button renderer
    private class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // Custom button editor
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
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int row = inventoryTable.getSelectedRow();
                if (row != -1) {
                    int productId = (Integer) tableModel.getValueAt(row, 0);
                    showRestockDialog(productId);
                }
            }
            isPushed = false;
            return label;
        }
    }

    private void showRestockDialog(int productId) {
        Inventory item = controller.getInventoryItemById(productId);
        if (item == null) return;

        // Create dialog components
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Restock " + item.getProductName(), true);
        dialog.setLayout(new BorderLayout(10, 10));

        // Create input panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Current quantity display
        inputPanel.add(new JLabel("Current Quantity:"));
        inputPanel.add(new JLabel(String.valueOf(item.getQuantity())));

        // New quantity input
        inputPanel.add(new JLabel("New Quantity:"));
        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(
            item.getQuantity() + 1, // initial value
            item.getQuantity() + 1, // minimum value (must be greater than current)
            99999, // maximum value
            1 // step size
        ));
        inputPanel.add(quantitySpinner);

        // Employee ID input
        inputPanel.add(new JLabel("Employee ID:"));
        JSpinner employeeSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99999, 1));
        inputPanel.add(employeeSpinner);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton confirmButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel");

        // Add action listeners
        confirmButton.addActionListener(e -> {
            int newQuantity = (Integer) quantitySpinner.getValue();
            int employeeId = (Integer) employeeSpinner.getValue();
            
            try {
                if (controller.restockInventory(productId, newQuantity, employeeId)) {
                    dialog.dispose();
                    refreshTable();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog,
                    DatabaseErrorHandler.getUserFriendlyMessage(ex),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());

        // Add buttons to panel
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        // Add panels to dialog
        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Show dialog
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showHelp() {
        HelpDialog helpDialog = new HelpDialog(SwingUtilities.getWindowAncestor(this), "Inventory");
        helpDialog.setVisible(true);
    }
} 