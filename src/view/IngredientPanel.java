package view;

import controller.RestaurantController;
import model.Ingredient;
import model.IngredientBatch;
import view.dialogs.IngredientHelpDialog;
import view.dialogs.AddBatchDialog;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

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

        // Create ingredients panel
        JPanel ingredientsPanel = new JPanel(new BorderLayout());
        
        // Create toolbar for ingredients
        JPanel ingredientToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addIngredientButton = createStyledButton("Add Ingredient", new Color(40, 167, 69));
        JButton editIngredientButton = createStyledButton("Edit Ingredient", new Color(255, 193, 7));
        JButton deleteIngredientButton = createStyledButton("Delete Ingredient", new Color(220, 53, 69));
        JButton refreshButton = createStyledButton("Refresh", new Color(108, 117, 125));
        JButton helpButton = createStyledButton("Help", new Color(23, 162, 184));
        lowStockCheckBox = new JCheckBox("Show Low Stock Only");

        ingredientToolbar.add(addIngredientButton);
        ingredientToolbar.add(editIngredientButton);
        ingredientToolbar.add(deleteIngredientButton);
        ingredientToolbar.add(refreshButton);
        ingredientToolbar.add(helpButton);
        ingredientToolbar.add(lowStockCheckBox);

        // Create ingredients table
        String[] ingredientColumns = {"Name", "Unit", "Stock Level", "Min. Stock", "Cost/Unit", "Last Restock"};
        ingredientTableModel = new DefaultTableModel(ingredientColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        ingredientTable = new JTable(ingredientTableModel);
        JScrollPane ingredientScrollPane = new JScrollPane(ingredientTable);

        ingredientsPanel.add(ingredientToolbar, BorderLayout.NORTH);
        ingredientsPanel.add(ingredientScrollPane, BorderLayout.CENTER);

        // Create batches panel
        JPanel batchesPanel = new JPanel(new BorderLayout());
        
        // Create toolbar for batches
        JPanel batchToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBatchButton = createStyledButton("Add Batch", new Color(40, 167, 69));
        JButton editBatchButton = createStyledButton("Edit Batch", new Color(255, 193, 7));
        expiringBatchesCheckBox = new JCheckBox("Show Expiring Within");
        expiryDaysSpinner = new JSpinner(new SpinnerNumberModel(7, 1, 90, 1));
        JLabel daysLabel = new JLabel("days");

        batchToolbar.add(addBatchButton);
        batchToolbar.add(editBatchButton);
        batchToolbar.add(expiringBatchesCheckBox);
        batchToolbar.add(expiryDaysSpinner);
        batchToolbar.add(daysLabel);

        // Create batches table
        String[] batchColumns = {"Ingredient", "Supplier", "Quantity", "Remaining", "Purchase Date", "Expiry Date", "Status"};
        batchTableModel = new DefaultTableModel(batchColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        batchTable = new JTable(batchTableModel);
        JScrollPane batchScrollPane = new JScrollPane(batchTable);

        batchesPanel.add(batchToolbar, BorderLayout.NORTH);
        batchesPanel.add(batchScrollPane, BorderLayout.CENTER);

        // Add panels to tabbed pane
        tabbedPane.addTab("Ingredients", ingredientsPanel);
        tabbedPane.addTab("Batches", batchesPanel);

        // Add tabbed pane to main panel
        add(tabbedPane, BorderLayout.CENTER);

        // Add action listeners
        addIngredientButton.addActionListener(e -> showAddIngredientDialog());
        editIngredientButton.addActionListener(e -> {
            int selectedRow = ingredientTable.getSelectedRow();
            if (selectedRow != -1) {
                int ingredientId = ingredientIds.get(selectedRow);
                showEditIngredientDialog(ingredientId);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select an ingredient to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteIngredientButton.addActionListener(e -> {
            int selectedRow = ingredientTable.getSelectedRow();
            if (selectedRow != -1) {
                int ingredientId = ingredientIds.get(selectedRow);
                deleteIngredient(ingredientId);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select an ingredient to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        refreshButton.addActionListener(e -> loadData());

        helpButton.addActionListener(e -> {
            Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            IngredientHelpDialog helpDialog = new IngredientHelpDialog(parentFrame);
            helpDialog.setVisible(true);
        });

        lowStockCheckBox.addActionListener(e -> loadIngredients());
        
        addBatchButton.addActionListener(e -> showAddBatchDialog());
        editBatchButton.addActionListener(e -> {
            int selectedRow = batchTable.getSelectedRow();
            if (selectedRow != -1) {
                int batchId = batchIds.get(selectedRow);
                showEditBatchDialog(batchId);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Please select a batch to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        expiringBatchesCheckBox.addActionListener(e -> loadBatches());
        expiryDaysSpinner.addChangeListener(e -> {
            if (expiringBatchesCheckBox.isSelected()) {
                loadBatches();
            }
        });
    }

    private void loadData() {
        loadIngredients();
        loadBatches();
    }

    private void loadIngredients() {
        ingredientTableModel.setRowCount(0);
        ingredientIds.clear();
        List<Ingredient> ingredients;
        
        if (lowStockCheckBox.isSelected()) {
            ingredients = controller.getLowStockIngredients();
        } else {
            ingredients = controller.getAllIngredients();
        }

        for (Ingredient ingredient : ingredients) {
            ingredientIds.add(ingredient.getIngredientId());
            Object[] row = {
                ingredient.getName(),
                ingredient.getUnitName(),
                ingredient.getQuantityInStock(),
                ingredient.getMinimumStockLevel(),
                ingredient.getCostPerUnit(),
                ingredient.getLastRestockDate()
            };
            ingredientTableModel.addRow(row);
        }
    }

    private void loadBatches() {
        batchTableModel.setRowCount(0);
        batchIds.clear();
        List<IngredientBatch> batches;
        
        if (expiringBatchesCheckBox.isSelected()) {
            int days = (Integer) expiryDaysSpinner.getValue();
            batches = controller.getExpiringBatches(days);
        } else {
            batches = controller.getAllBatches();
        }

        for (IngredientBatch batch : batches) {
            batchIds.add(batch.getBatchId());
            Object[] row = {
                batch.getIngredientName(),
                batch.getSupplierName(),
                batch.getQuantity(),
                batch.getRemainingQuantity(),
                batch.getPurchaseDate(),
                batch.getExpiryDate(),
                batch.getStatus()
            };
            batchTableModel.addRow(row);
        }
    }

    private void showAddIngredientDialog() {
        // TODO: Implement add ingredient dialog
        JOptionPane.showMessageDialog(this, "Add ingredient dialog to be implemented");
    }

    private void showEditIngredientDialog(int ingredientId) {
        // TODO: Implement edit ingredient dialog
        JOptionPane.showMessageDialog(this, "Edit ingredient dialog to be implemented");
    }

    private void deleteIngredient(int ingredientId) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this ingredient?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.deleteIngredient(ingredientId);
                loadData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Failed to delete ingredient: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddBatchDialog() {
        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        AddBatchDialog dialog = new AddBatchDialog(parentFrame, controller);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            try {
                IngredientBatch batch = new IngredientBatch();
                batch.setIngredientId(dialog.getSelectedIngredient().getIngredientId());
                batch.setSupplierId(dialog.getSelectedSupplier().getSupplierId());
                batch.setQuantity(dialog.getQuantity());
                batch.setRemainingQuantity(dialog.getQuantity()); // Initially, remaining = total
                batch.setPurchaseDate(dialog.getPurchaseDate());
                batch.setExpiryDate(dialog.getExpiryDate());
                batch.setStatus("Available"); // Default status for new batches
                
                controller.addIngredientBatch(batch);
                loadBatches();
                JOptionPane.showMessageDialog(this,
                    "Batch added successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error adding batch: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditBatchDialog(int batchId) {
        // TODO: Implement edit batch dialog
        JOptionPane.showMessageDialog(this, "Edit batch dialog to be implemented");
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