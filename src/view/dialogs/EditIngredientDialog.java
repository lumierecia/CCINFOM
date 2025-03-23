package view.dialogs;

import controller.RestaurantController;
import model.Ingredient;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.HashMap;

public class EditIngredientDialog extends JDialog {
    private final RestaurantController controller;
    private final Ingredient ingredient;
    private JTextField nameField;
    private JComboBox<String> unitComboBox;
    private JSpinner quantitySpinner;
    private JSpinner minStockSpinner;
    private JSpinner costSpinner;
    private Map<String, Integer> unitMap;
    private boolean confirmed = false;

    public EditIngredientDialog(Frame owner, RestaurantController controller, Ingredient ingredient) {
        super(owner, "Edit Ingredient", true);
        this.controller = controller;
        this.ingredient = ingredient;
        initComponents();
        loadIngredientData();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Create main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 1;
        nameField = new JTextField(20);
        mainPanel.add(nameField, gbc);

        // Unit combo box
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Unit:"), gbc);
        
        gbc.gridx = 1;
        unitComboBox = new JComboBox<>();
        loadUnits();
        mainPanel.add(unitComboBox, gbc);

        // Quantity spinner
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Quantity in Stock:"), gbc);
        
        gbc.gridx = 1;
        SpinnerNumberModel quantityModel = new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.1);
        quantitySpinner = new JSpinner(quantityModel);
        mainPanel.add(quantitySpinner, gbc);

        // Minimum stock level spinner
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Minimum Stock Level:"), gbc);
        
        gbc.gridx = 1;
        SpinnerNumberModel minStockModel = new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.1);
        minStockSpinner = new JSpinner(minStockModel);
        mainPanel.add(minStockSpinner, gbc);

        // Cost per unit spinner
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("Cost per Unit:"), gbc);
        
        gbc.gridx = 1;
        SpinnerNumberModel costModel = new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.1);
        costSpinner = new JSpinner(costModel);
        mainPanel.add(costSpinner, gbc);

        // Add main panel
        add(mainPanel, BorderLayout.CENTER);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Set default button
        getRootPane().setDefaultButton(saveButton);

        // Add window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmed = false;
                dispose();
            }
        });
    }

    private void loadUnits() {
        unitMap = new HashMap<>();
        String query = "SELECT unit_id, unit_name FROM Units ORDER BY unit_name";
        
        try {
            java.sql.Connection conn = controller.getConnection();
            java.sql.PreparedStatement stmt = conn.prepareStatement(query);
            java.sql.ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String unitName = rs.getString("unit_name");
                int unitId = rs.getInt("unit_id");
                unitMap.put(unitName, unitId);
                unitComboBox.addItem(unitName);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Failed to load units: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadIngredientData() {
        nameField.setText(ingredient.getName());
        unitComboBox.setSelectedItem(ingredient.getUnitName());
        quantitySpinner.setValue(ingredient.getQuantityInStock());
        minStockSpinner.setValue(ingredient.getMinimumStockLevel());
        costSpinner.setValue(ingredient.getCostPerUnit());
    }

    private boolean validateInput() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter an ingredient name.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (unitComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a unit.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        double quantity = (Double) quantitySpinner.getValue();
        double minStock = (Double) minStockSpinner.getValue();
        double cost = (Double) costSpinner.getValue();

        if (minStock <= 0) {
            JOptionPane.showMessageDialog(this,
                "Minimum stock level must be greater than 0.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (cost <= 0) {
            JOptionPane.showMessageDialog(this,
                "Cost per unit must be greater than 0.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public Ingredient getUpdatedIngredient() {
        if (!confirmed) return null;

        ingredient.setName(nameField.getText().trim());
        String selectedUnit = (String) unitComboBox.getSelectedItem();
        ingredient.setUnitId(unitMap.get(selectedUnit));
        ingredient.setUnitName(selectedUnit);
        ingredient.setQuantityInStock((Double) quantitySpinner.getValue());
        ingredient.setMinimumStockLevel((Double) minStockSpinner.getValue());
        ingredient.setCostPerUnit((Double) costSpinner.getValue());

        return ingredient;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
} 