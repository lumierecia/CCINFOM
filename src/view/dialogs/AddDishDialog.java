package view.dialogs;

import controller.RestaurantController;
import model.Dish;
import model.DishIngredient;
import model.Ingredient;
import view.components.BaseDialog;
import view.components.BaseFormDialog;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddDishDialog extends BaseFormDialog {
    private final RestaurantController controller;
    private final JTextField nameField;
    private final JComboBox<String> categoryComboBox;
    private final JSpinner priceSpinner;
    private final JTextArea recipeArea;
    private final JCheckBox availableCheckBox;
    private final JPanel ingredientsPanel;
    private final List<IngredientRow> ingredientRows;
    private final JButton addIngredientButton;
    private boolean dishAdded = false;

    public AddDishDialog(Frame owner, RestaurantController controller) {
        super(owner, "Add New Dish");
        this.controller = controller;
        this.ingredientRows = new ArrayList<>();

        // Create form fields
        nameField = createTextField();
        categoryComboBox = createComboBox(getCategories());
        priceSpinner = createSpinner(new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.01));
        recipeArea = createTextArea();
        availableCheckBox = createCheckBox();
        ingredientsPanel = new JPanel();
        ingredientsPanel.setLayout(new BoxLayout(ingredientsPanel, BoxLayout.Y_AXIS));
        addIngredientButton = new JButton("Add Ingredient");

        // Add form fields
        addFormField("Name:", nameField);
        addFormField("Category:", categoryComboBox);
        addFormField("Price:", priceSpinner);
        addFormField("Recipe:", recipeArea);
        addFormField("Available:", availableCheckBox);
        addFormField("Ingredients:", ingredientsPanel);

        // Add ingredient button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addIngredientButton);
        ingredientsPanel.add(buttonPanel);

        // Set up ingredient button listener
        addIngredientButton.addActionListener(e -> addIngredientRow());

        // Set default values
        availableCheckBox.setSelected(true);
    }

    private void addIngredientRow() {
        IngredientRow row = new IngredientRow();
        ingredientRows.add(row);
        ingredientsPanel.add(row.panel);
        ingredientsPanel.revalidate();
        ingredientsPanel.repaint();
    }

    private String[] getCategories() {
        try {
            List<String> categories = controller.getAllCategories();
            return categories.toArray(new String[0]);
        } catch (SQLException e) {
            BaseDialog.showError(this, "Error loading categories: " + e.getMessage(), "Database Error");
            return new String[0];
        }
    }

    private String[] getIngredients() {
        try {
            List<Ingredient> ingredients = controller.getAllIngredients();
            return ingredients.stream()
                    .map(i -> i.getIngredientId() + " - " + i.getName())
                    .toArray(String[]::new);
        } catch (Exception e) {
            BaseDialog.showError(this, "Error loading ingredients: " + e.getMessage(), "Database Error");
            return new String[0];
        }
    }

    private String[] getUnits() {
        try {
            List<String> units = controller.getAllUnits();
            return units.toArray(new String[0]);
        } catch (SQLException e) {
            BaseDialog.showError(this, "Error loading units: " + e.getMessage(), "Database Error");
            return new String[0];
        }
    }

    @Override
    protected boolean validateForm() {
        String name = getTextFieldValue(nameField);
        if (name.isEmpty()) {
            BaseDialog.showError(this, "Please enter a dish name.", "Validation Error");
            return false;
        }

        try {
            double price = getSpinnerValue(priceSpinner);
            if (price <= 0) {
                BaseDialog.showError(this, "Price must be greater than 0.", "Validation Error");
                return false;
            }
        } catch (NumberFormatException e) {
            BaseDialog.showError(this, "Please enter a valid price.", "Validation Error");
            return false;
        }

        // Validate ingredients
        if (ingredientRows.isEmpty()) {
            BaseDialog.showError(this, "Please add at least one ingredient.", "Validation Error");
            return false;
        }

        for (IngredientRow row : ingredientRows) {
            if (!row.validate()) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void saveFormData() {
        try {
            String name = getTextFieldValue(nameField);
            String category = getComboBoxValue(categoryComboBox);
            double price = getSpinnerValue(priceSpinner);
            String recipe = getTextAreaValue(recipeArea);
            boolean available = getCheckBoxValue(availableCheckBox);

            Dish dish = new Dish();
            dish.setName(name);
            dish.setCategoryId(controller.getCategoryId(category));
            dish.setSellingPrice(price);
            dish.setRecipeInstructions(recipe);
            dish.setAvailable(available);

            // Add ingredients
            List<DishIngredient> ingredients = new ArrayList<>();
            for (IngredientRow row : ingredientRows) {
                DishIngredient ingredient = row.createIngredient();
                if (ingredient != null) {
                    ingredients.add(ingredient);
                }
            }
            dish.setIngredients(ingredients);

            if (controller.addDish(dish)) {
                dishAdded = true;
                BaseDialog.showInfo(this, "Dish added successfully!", "Success");
            } else {
                BaseDialog.showError(this, "Failed to add dish.", "Error");
            }
        } catch (SQLException e) {
            BaseDialog.showError(this, "Database error: " + e.getMessage(), "Error");
        }
    }

    public boolean isDishAdded() {
        return dishAdded;
    }

    private class IngredientRow {
        private final JPanel panel;
        private final JComboBox<String> ingredientCombo;
        private final JSpinner quantitySpinner;
        private final JComboBox<String> unitCombo;
        private final JButton removeButton;

        public IngredientRow() {
            panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            
            ingredientCombo = createComboBox(getIngredients());
            quantitySpinner = createSpinner(new SpinnerNumberModel(0.0, 0.0, 1000.0, 0.1));
            unitCombo = createComboBox(getUnits());
            removeButton = new JButton("Remove");

            panel.add(new JLabel("Ingredient:"));
            panel.add(ingredientCombo);
            panel.add(new JLabel("Quantity:"));
            panel.add(quantitySpinner);
            panel.add(new JLabel("Unit:"));
            panel.add(unitCombo);
            panel.add(removeButton);

            removeButton.addActionListener(e -> {
                ingredientRows.remove(this);
                ingredientsPanel.remove(panel);
                ingredientsPanel.revalidate();
                ingredientsPanel.repaint();
            });
        }

        public boolean validate() {
            if (ingredientCombo.getSelectedItem() == null) {
                BaseDialog.showError(AddDishDialog.this, "Please select an ingredient.", "Validation Error");
                return false;
            }

            try {
                double quantity = (Double) quantitySpinner.getValue();
                if (quantity <= 0) {
                    BaseDialog.showError(AddDishDialog.this, "Quantity must be greater than 0.", "Validation Error");
                    return false;
                }
            } catch (NumberFormatException e) {
                BaseDialog.showError(AddDishDialog.this, "Please enter a valid quantity.", "Validation Error");
                return false;
            }

            if (unitCombo.getSelectedItem() == null) {
                BaseDialog.showError(AddDishDialog.this, "Please select a unit.", "Validation Error");
                return false;
            }

            return true;
        }

        public DishIngredient createIngredient() {
            try {
                String ingredientStr = (String) ingredientCombo.getSelectedItem();
                int ingredientId = Integer.parseInt(ingredientStr.split(" - ")[0]);
                double quantity = (Double) quantitySpinner.getValue();
                String unitStr = (String) unitCombo.getSelectedItem();
                int unitId = controller.getUnitId(unitStr);

                return new DishIngredient(0, ingredientId, quantity, unitId);
            } catch (SQLException e) {
                BaseDialog.showError(AddDishDialog.this, "Error creating ingredient: " + e.getMessage(), "Error");
                return null;
            }
        }
    }
} 