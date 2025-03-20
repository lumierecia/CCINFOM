package view;

import controller.RestaurantController;
import model.Inventory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InventoryPanel extends JPanel {
    private final RestaurantController controller;
    private final JTable inventoryTable;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> categoryFilter;
    private final JSpinner lowStockThreshold;
    private final JCheckBox showLowStockOnly;
    private final JTextField searchField;

    public InventoryPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        // Create the table model with non-editable cells
        String[] columns = {
            "ID", "Name", "Category", "Quantity", "Make Price", "Sell Price", "Actions"
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
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Category filter
        categoryFilter = new JComboBox<>();
        categoryFilter.addItem("All Categories");
        controller.getAllCategories().forEach(categoryFilter::addItem);
        categoryFilter.addActionListener(e -> refreshTable());
        
        // Low stock filter
        lowStockThreshold = new JSpinner(new SpinnerNumberModel(10, 0, 1000, 1));
        showLowStockOnly = new JCheckBox("Show Low Stock Only");
        showLowStockOnly.addActionListener(e -> refreshTable());
        
        // Search field
        searchField = new JTextField(20);
        searchField.addActionListener(e -> refreshTable());
        
        // Add components to control panel
        controlPanel.add(new JLabel("Category:"));
        controlPanel.add(categoryFilter);
        controlPanel.add(new JLabel("Low Stock Threshold:"));
        controlPanel.add(lowStockThreshold);
        controlPanel.add(showLowStockOnly);
        controlPanel.add(new JLabel("Search:"));
        controlPanel.add(searchField);
        
        // Add refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshTable());
        controlPanel.add(refreshButton);
        
        // Add components to panel
        add(controlPanel, BorderLayout.NORTH);
        add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        
        // Initial table load
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Inventory> items;
        
        if (showLowStockOnly.isSelected()) {
            items = controller.getLowStockItems((Integer) lowStockThreshold.getValue());
        } else {
            String selectedCategory = (String) categoryFilter.getSelectedItem();
            if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
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
                    item.getProductId(),
                    item.getProductName(),
                    item.getCategoryName(),
                    item.getQuantity(),
                    String.format("%.2f", item.getMakePrice()),
                    String.format("%.2f", item.getSellPrice()),
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
            
            if (controller.restockInventory(productId, newQuantity, employeeId)) {
                dialog.dispose();
                refreshTable();
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
} 