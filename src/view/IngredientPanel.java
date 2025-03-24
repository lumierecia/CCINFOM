package view;

import controller.RestaurantController;
import model.Ingredient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

public class IngredientPanel extends JPanel {
    private final RestaurantController controller;
    private JTable ingredientTable;
    private DefaultTableModel ingredientTableModel;
    private List<Integer> ingredientIds = new ArrayList<>();
    private JCheckBox lowStockCheckBox;

    public IngredientPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initComponents();
        loadData();
    }

    private void initComponents() {
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
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
        
        // Add components to main panel
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(ingredientToolbar, BorderLayout.NORTH);
        northPanel.add(filterPanel, BorderLayout.SOUTH);
        mainPanel.add(northPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(ingredientTable), BorderLayout.CENTER);

        // Add button listeners
        addIngredientBtn.addActionListener(e -> showAddIngredientDialog());
        editIngredientBtn.addActionListener(e -> editSelectedIngredient());
        deleteIngredientBtn.addActionListener(e -> deleteSelectedIngredient());
        helpBtn.addActionListener(e -> showHelp());
        
        // Add filter listener
        lowStockCheckBox.addActionListener(e -> loadData());
        
        add(mainPanel);
    }

    private void loadData() {
        ingredientTableModel.setRowCount(0);
        ingredientIds.clear();
        
        List<Ingredient> ingredients = controller.getAllIngredients();
        boolean showLowStockOnly = lowStockCheckBox.isSelected();
        
        for (Ingredient ingredient : ingredients) {
            if (ingredient.isDeleted()) continue;
            if (showLowStockOnly && ingredient.getQuantityInStock() > ingredient.getMinimumStockLevel()) continue;
            
            ingredientIds.add(ingredient.getIngredientId());
            String status = ingredient.getQuantityInStock() <= ingredient.getMinimumStockLevel() ? "Low Stock" : "In Stock";
            
            ingredientTableModel.addRow(new Object[]{
                ingredient.getName(),
                ingredient.getUnitName(),
                ingredient.getQuantityInStock(),
                ingredient.getMinimumStockLevel(),
                String.format("%.2f", ingredient.getCostPerUnit()),
                status,
                "N/A"
            });
        }
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font(button.getFont().getName(), Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(Color.BLACK    );
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
            ingredient.setUnitId(unitCombo.getSelectedIndex() + 1);
            ingredient.setUnitName((String) unitCombo.getSelectedItem());
            ingredient.setQuantityInStock((Double) stockSpinner.getValue());
            ingredient.setMinimumStockLevel((Double) minStockSpinner.getValue());
            ingredient.setCostPerUnit((Double) costSpinner.getValue());
            
            if (controller.addIngredient(ingredient)) {
                loadData();
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

    private void editSelectedIngredient() {
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
        unitCombo.setSelectedIndex(ingredient.getUnitId() - 1);
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
            
            if (controller.updateIngredient(ingredient)) {
                loadData();
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
                    loadData();
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
        } else {
            JOptionPane.showMessageDialog(this,
                "Please select an ingredient to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showHelp() {
        JOptionPane.showMessageDialog(this,
            """
            Ingredient Management Help:
            
            1. Add Ingredient: Add a new ingredient to the inventory
            2. Edit Ingredient: Modify existing ingredient details
            3. Delete Ingredient: Remove an ingredient from the system
            4. Low Stock Filter: Show only ingredients below minimum stock level
            
            Status Colors:
            - In Stock: Normal stock levels
            - Low Stock: Below minimum stock level
            """,
            "Help",
            JOptionPane.INFORMATION_MESSAGE);
    }
}