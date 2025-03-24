package view.dialogs;

import controller.RestaurantController;
import model.Dish;
import model.Ingredient;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AddDishDialog extends JDialog {
    private final RestaurantController controller;
    private boolean dishAdded = false;
    private final JTextField nameField;
    private final JComboBox<String> categoryComboBox;
    private final JSpinner priceSpinner;
    private final JTextArea recipeArea;
    private final JCheckBox availableCheckBox;
    private final JTable ingredientsTable;
    private final DefaultTableModel tableModel;
    private final Map<Integer, Double> ingredientQuantities = new HashMap<>();

    public AddDishDialog(Window owner, RestaurantController controller) {
        super(owner, "Add New Dish", ModalityType.APPLICATION_MODAL);
        this.controller = controller;

        setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        mainPanel.add(nameField, gbc);

        // Category combo box
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Category:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        categoryComboBox = new JComboBox<>();
        List<String> categories = controller.getAllCategories();
        for (String category : categories) {
            categoryComboBox.addItem(category);
        }
        mainPanel.add(categoryComboBox, gbc);

        // Price spinner
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Price:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        SpinnerNumberModel priceModel = new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.1);
        priceSpinner = new JSpinner(priceModel);
        mainPanel.add(priceSpinner, gbc);

        // Ingredients table
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Ingredients:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        
        String[] columnNames = {"Ingredient", "Quantity"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 1 ? Double.class : String.class;
            }
        };
        ingredientsTable = new JTable(tableModel);
        JScrollPane ingredientsScroll = new JScrollPane(ingredientsTable);
        mainPanel.add(ingredientsScroll, gbc);

        // Add ingredient button
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weighty = 0.0;
        JButton addIngredientButton = new JButton("Add Ingredient");
        addIngredientButton.addActionListener(e -> showAddIngredientDialog());
        mainPanel.add(addIngredientButton, gbc);

        // Recipe instructions
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Recipe:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        recipeArea = new JTextArea(5, 20);
        recipeArea.setLineWrap(true);
        recipeArea.setWrapStyleWord(true);
        JScrollPane recipeScroll = new JScrollPane(recipeArea);
        mainPanel.add(recipeScroll, gbc);

        // Available checkbox
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        availableCheckBox = new JCheckBox("Available", true);
        mainPanel.add(availableCheckBox, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveDish());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void showAddIngredientDialog() {
        List<Ingredient> availableIngredients = controller.getAllIngredients();
        if (availableIngredients.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No ingredients available. Please add ingredients first.",
                "No Ingredients",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<Ingredient> ingredientCombo = new JComboBox<>(
            availableIngredients.toArray(new Ingredient[0]));
        SpinnerNumberModel quantityModel = new SpinnerNumberModel(1.0, 0.1, 1000.0, 0.1);
        JSpinner quantitySpinner = new JSpinner(quantityModel);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Ingredient:"));
        panel.add(ingredientCombo);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantitySpinner);

        int result = JOptionPane.showConfirmDialog(this, panel,
            "Add Ingredient", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            Ingredient selectedIngredient = (Ingredient) ingredientCombo.getSelectedItem();
            double quantity = (Double) quantitySpinner.getValue();

            // Update the table and map
            ingredientQuantities.put(selectedIngredient.getIngredientId(), quantity);
            tableModel.addRow(new Object[]{
                selectedIngredient.getName(),
                quantity
            });
        }
    }

    private void saveDish() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a dish name.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (ingredientQuantities.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please add at least one ingredient to the dish.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        String category = (String) categoryComboBox.getSelectedItem();
        double price = (Double) priceSpinner.getValue();
        String recipe = recipeArea.getText().trim();
        boolean available = availableCheckBox.isSelected();

        Dish dish = new Dish(name, category, price, recipe, available);
        if (controller.addDish(dish, ingredientQuantities)) {
            dishAdded = true;
            dispose();
        }
    }

    public boolean isDishAdded() {
        return dishAdded;
    }
}