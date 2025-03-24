package view;

import controller.RestaurantController;
import model.Ingredient;
import model.IngredientBatch;
import model.Supplier;
import view.dialogs.AddBatchDialog;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

public class IngredientPanel extends JPanel {
    private final RestaurantController controller;
    private JTabbedPane tabbedPane;
    private JTable ingredientTable;
    private JTable batchTable;
    private DefaultTableModel ingredientTableModel;
    private DefaultTableModel batchTableModel;
    private List<Integer> ingredientIds = new ArrayList<>();
    private List<Integer> batchIds = new ArrayList<>();
    private JCheckBox lowStockCheckBox;
    private JCheckBox expiringBatchesCheckBox;
    private JSpinner expiryDaysSpinner;

    public IngredientPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initComponents();
        loadData();
    }

    private void initComponents() {
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Initialize Ingredients tab
        JPanel ingredientsTab = new JPanel(new BorderLayout());
        
        // Create toolbar for ingredients
        JPanel ingredientToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addIngredientBtn = createStyledButton("Add Ingredient", new Color(40, 167, 69));
        JButton editIngredientBtn = createStyledButton("Edit Ingredient", new Color(255, 193, 7));
        JButton deleteIngredientBtn = createStyledButton("Delete Ingredient", new Color(220, 53, 69));
        JButton helpBtn = createStyledButton("Help", new Color(23, 162, 184));
        
        ingredientToolbar.add(addIngredientBtn);
        ingredientToolbar.add(editIngredientBtn);
        ingredientToolbar.add(deleteIngredientBtn);
        ingredientToolbar.add(helpBtn);
        
        // Create filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lowStockCheckBox = new JCheckBox("Show Low Stock Only");
        filterPanel.add(lowStockCheckBox);
        
        // Create ingredients table
        String[] ingredientColumns = {"Name", "Unit", "Current Stock", "Min Stock Level", "Cost/Unit", "Status", "Primary Supplier"};
        ingredientTableModel = new DefaultTableModel(ingredientColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ingredientTable = new JTable(ingredientTableModel);
        
        // Add components to ingredients tab
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(ingredientToolbar, BorderLayout.NORTH);
        northPanel.add(filterPanel, BorderLayout.SOUTH);
        ingredientsTab.add(northPanel, BorderLayout.NORTH);
        ingredientsTab.add(new JScrollPane(ingredientTable), BorderLayout.CENTER);
        
        // Initialize Batches tab
        JPanel batchesTab = new JPanel(new BorderLayout());
        
        // Create toolbar for batches
        JPanel batchToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBatchBtn = createStyledButton("Add Batch", new Color(40, 167, 69));
        JButton viewBatchDetailsBtn = createStyledButton("View Details", new Color(70, 130, 180));
        
        batchToolbar.add(addBatchBtn);
        batchToolbar.add(viewBatchDetailsBtn);
        
        // Create batch filter panel
        JPanel batchFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        expiringBatchesCheckBox = new JCheckBox("Show Expiring Within");
        expiryDaysSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));
        JLabel daysLabel = new JLabel("days");
        batchFilterPanel.add(expiringBatchesCheckBox);
        batchFilterPanel.add(expiryDaysSpinner);
        batchFilterPanel.add(daysLabel);
        
        // Create batches table
        String[] batchColumns = {"Ingredient", "Supplier", "Quantity", "Remaining", "Purchase Date", "Expiry Date", "Status"};
        batchTableModel = new DefaultTableModel(batchColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        batchTable = new JTable(batchTableModel);
        
        // Add components to batches tab
        JPanel batchNorthPanel = new JPanel(new BorderLayout());
        batchNorthPanel.add(batchToolbar, BorderLayout.NORTH);
        batchNorthPanel.add(batchFilterPanel, BorderLayout.SOUTH);
        batchesTab.add(batchNorthPanel, BorderLayout.NORTH);
        batchesTab.add(new JScrollPane(batchTable), BorderLayout.CENTER);
        
        // Add tabs to tabbed pane
        tabbedPane.addTab("Ingredients", ingredientsTab);
        tabbedPane.addTab("Batches", batchesTab);
        
        // Add tabbed pane to panel
        add(tabbedPane, BorderLayout.CENTER);
        
        // Add action listeners
        addIngredientBtn.addActionListener(e -> showAddIngredientDialog());
        editIngredientBtn.addActionListener(e -> showEditIngredientDialog());
        deleteIngredientBtn.addActionListener(e -> deleteSelectedIngredient());
        helpBtn.addActionListener(e -> showHelpDialog());
        addBatchBtn.addActionListener(e -> showAddBatchDialog());
        viewBatchDetailsBtn.addActionListener(e -> showBatchDetails());
        
        // Add filter listeners
        lowStockCheckBox.addActionListener(e -> refreshIngredientTable());
        expiringBatchesCheckBox.addActionListener(e -> refreshBatchTable());
        expiryDaysSpinner.addChangeListener(e -> {
            if (expiringBatchesCheckBox.isSelected()) {
                refreshBatchTable();
            }
        });
    }

    private void loadData() {
        refreshIngredientTable();
        refreshBatchTable();
    }

    private void refreshIngredientTable() {
        ingredientTableModel.setRowCount(0);
        ingredientIds.clear();
        
        List<Ingredient> ingredients;
        if (lowStockCheckBox.isSelected()) {
            ingredients = controller.getLowStockIngredients();
        } else {
            ingredients = controller.getAllIngredients();
        }
        
        for (Ingredient ingredient : ingredients) {
            Supplier primarySupplier = controller.getPrimarySupplierForIngredient(ingredient.getIngredientId());
            String supplierName = primarySupplier != null ? primarySupplier.getName() : "None";
            
            ingredientIds.add(ingredient.getIngredientId());
            Object[] row = {
                ingredient.getName(),
                ingredient.getUnitName(),
                ingredient.getQuantityInStock(),
                ingredient.getMinimumStockLevel(),
                String.format("₱%.2f", ingredient.getCostPerUnit()),
                ingredient.getQuantityInStock() < ingredient.getMinimumStockLevel() ? "Low Stock" : "OK",
                supplierName
            };
            ingredientTableModel.addRow(row);
        }
    }

    private void refreshBatchTable() {
        batchTableModel.setRowCount(0);
        batchIds.clear();
        
        List<IngredientBatch> batches;
        if (expiringBatchesCheckBox.isSelected()) {
            int days = (Integer) expiryDaysSpinner.getValue();
            batches = controller.getExpiringBatches(days);
        } else {
            batches = controller.getAllIngredientBatches();
        }
        
        for (IngredientBatch batch : batches) {
            Ingredient ingredient = controller.getIngredientById(batch.getIngredientId());
            Supplier supplier = controller.getSupplierById(batch.getSupplierId());
            
            if (ingredient != null && supplier != null) {
                batchIds.add(batch.getBatchId());
                Object[] row = {
                    ingredient.getName(),
                    supplier.getName(),
                    batch.getQuantity(),
                    batch.getRemainingQuantity(),
                    batch.getPurchaseDate(),
                    batch.getExpiryDate(),
                    batch.getStatus()
                };
                batchTableModel.addRow(row);
            }
        }
    }

    private void showAddIngredientDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Ingredient", true);
        dialog.setLayout(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add form fields
        JTextField nameField = new JTextField(20);
        JComboBox<String> unitCombo = new JComboBox<>(new String[]{"kg", "liters", "pieces", "grams", "milliliters"});
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.1));
        JSpinner minStockSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.1));
        JSpinner costSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.1));
        
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Unit:"), gbc);
        gbc.gridx = 1;
        formPanel.add(unitCombo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Initial Stock:"), gbc);
        gbc.gridx = 1;
        formPanel.add(stockSpinner, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Minimum Stock Level:"), gbc);
        gbc.gridx = 1;
        formPanel.add(minStockSpinner, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Cost per Unit:"), gbc);
        gbc.gridx = 1;
        formPanel.add(costSpinner, gbc);
        
        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please enter an ingredient name.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create new ingredient
            Ingredient ingredient = new Ingredient();
            ingredient.setName(name);
            ingredient.setUnitId(unitCombo.getSelectedIndex() + 1); // Unit IDs start from 1
            ingredient.setUnitName((String) unitCombo.getSelectedItem());
            ingredient.setQuantityInStock((Double) stockSpinner.getValue());
            ingredient.setMinimumStockLevel((Double) minStockSpinner.getValue());
            ingredient.setCostPerUnit((Double) costSpinner.getValue());
            ingredient.setLastRestockDate(new Date());
            ingredient.setLastRestockedBy(1); // Default to admin ID 1
            
            if (controller.addIngredient(ingredient)) {
                refreshIngredientTable();
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                    "Ingredient added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Failed to add ingredient. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showEditIngredientDialog() {
        int selectedRow = ingredientTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select an ingredient to edit.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int ingredientId = ingredientIds.get(selectedRow);
        Ingredient ingredient = controller.getIngredientById(ingredientId);
        if (ingredient == null) {
            JOptionPane.showMessageDialog(this,
                "Failed to load ingredient details.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Ingredient", true);
        dialog.setLayout(new BorderLayout());
        
        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Add form fields
        JTextField nameField = new JTextField(ingredient.getName(), 20);
        JComboBox<String> unitCombo = new JComboBox<>(new String[]{"kg", "liters", "pieces", "grams", "milliliters"});
        unitCombo.setSelectedIndex(ingredient.getUnitId() - 1); // Unit IDs start from 1
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(ingredient.getQuantityInStock(), 0.0, 10000.0, 0.1));
        JSpinner minStockSpinner = new JSpinner(new SpinnerNumberModel(ingredient.getMinimumStockLevel(), 0.0, 10000.0, 0.1));
        JSpinner costSpinner = new JSpinner(new SpinnerNumberModel(ingredient.getCostPerUnit(), 0.0, 10000.0, 0.1));
        
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Unit:"), gbc);
        gbc.gridx = 1;
        formPanel.add(unitCombo, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Current Stock:"), gbc);
        gbc.gridx = 1;
        formPanel.add(stockSpinner, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Minimum Stock Level:"), gbc);
        gbc.gridx = 1;
        formPanel.add(minStockSpinner, gbc);
        
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Cost per Unit:"), gbc);
        gbc.gridx = 1;
        formPanel.add(costSpinner, gbc);
        
        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please enter an ingredient name.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Update ingredient
            ingredient.setName(name);
            ingredient.setUnitId(unitCombo.getSelectedIndex() + 1);
            ingredient.setUnitName((String) unitCombo.getSelectedItem());
            ingredient.setQuantityInStock((Double) stockSpinner.getValue());
            ingredient.setMinimumStockLevel((Double) minStockSpinner.getValue());
            ingredient.setCostPerUnit((Double) costSpinner.getValue());
            ingredient.setLastRestockDate(new Date());
            
            if (controller.updateIngredient(ingredient)) {
                refreshIngredientTable();
                dialog.dispose();
                JOptionPane.showMessageDialog(this,
                    "Ingredient updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Failed to update ingredient. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteSelectedIngredient() {
        int selectedRow = ingredientTable.getSelectedRow();
        if (selectedRow >= 0) {
            int ingredientId = ingredientIds.get(selectedRow);
            String ingredientName = (String) ingredientTableModel.getValueAt(selectedRow, 0);
            
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete " + ingredientName + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                if (controller.deleteIngredient(ingredientId)) {
                    refreshIngredientTable();
                    JOptionPane.showMessageDialog(
                        this,
                        "Ingredient deleted successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Failed to delete ingredient",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }

    private void showAddBatchDialog() {
        int selectedRow = ingredientTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                this,
                "Please select an ingredient first",
                "No Selection",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int ingredientId = ingredientIds.get(selectedRow);
        Ingredient ingredient = controller.getIngredientById(ingredientId);
        
        if (ingredient != null) {
            AddBatchDialog dialog = new AddBatchDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                ingredient,
                controller
            );
            dialog.setVisible(true);
            
            // Refresh tables after adding a batch
            if (dialog.isSuccess()) {
                refreshIngredientTable();
                refreshBatchTable();
            }
        }
    }

    private void showBatchDetails() {
        int selectedRow = batchTable.getSelectedRow();
        if (selectedRow >= 0) {
            int batchId = batchIds.get(selectedRow);
            IngredientBatch batch = controller.getIngredientBatchById(batchId);
            
            if (batch != null) {
                // Show batch details in a dialog
                JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    "Batch Details", true);
                // ... implement batch details dialog
            }
        }
    }

    private void showHelpDialog() {
        String helpText = """
            Ingredient Management Help
            
            Overview:
            The ingredient management system helps you track your restaurant's ingredients,
            their stock levels, and batch information.
            
            Ingredients Tab Features:
            1. Add Ingredient: Create new ingredients with units and stock levels
            2. Edit Ingredient: Modify ingredient details and stock thresholds
            3. Delete Ingredient: Remove unused ingredients
            4. Low Stock Filter: View only ingredients below minimum stock level
            
            Batches Tab Features:
            1. Add Batch: Record new ingredient deliveries with expiry dates
            2. View Details: Check batch information and usage history
            3. Expiry Filter: Monitor batches nearing expiration
            
            Stock Management:
            • Green Status: Stock level is adequate
            • Red Status: Stock is below minimum threshold
            • System tracks:
              - Current stock levels
              - Minimum stock thresholds
              - Cost per unit
              - Primary suppliers
            
            Batch Tracking:
            • Each batch records:
              - Purchase date
              - Expiry date
              - Initial quantity
              - Remaining quantity
              - Supplier information
            
            Tips:
            • Regularly check low stock indicators
            • Monitor expiring batches
            • Update stock levels after usage
            • Maintain accurate supplier associations
            
            Note: Deleting an ingredient will preserve its historical records
            but prevent future use in recipes and orders.
            """;
        
        JDialog helpDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Ingredient Management Help", true);
        helpDialog.setLayout(new BorderLayout());
        
        JTextArea textArea = new JTextArea(helpText);
        textArea.setEditable(false);
        textArea.setMargin(new Insets(10, 10, 10, 10));
        textArea.setBackground(new Color(250, 250, 250));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        helpDialog.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> helpDialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        helpDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        helpDialog.setSize(500, 500);
        helpDialog.setLocationRelativeTo(this);
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
} 