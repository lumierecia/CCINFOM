package view.dialogs;

import controller.RestaurantController;
import model.Dish;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class EditDishDialog extends JDialog {
    private final RestaurantController controller;
    private final Dish dish;
    private boolean dishUpdated = false;
    private final JTextField nameField;
    private final JComboBox<String> categoryComboBox;
    private final JSpinner priceSpinner;
    private final JTextArea recipeArea;
    private final JCheckBox availableCheckBox;

    public EditDishDialog(Window owner, RestaurantController controller, Dish dish) {
        super(owner, "Edit Dish", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.dish = dish;

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
        nameField = new JTextField(dish.getName(), 20);
        mainPanel.add(nameField, gbc);

        // Category combo box
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Category:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        categoryComboBox = new JComboBox<>();
        try {
            List<String> categories = controller.getAllCategories();
            for (String category : categories) {
                categoryComboBox.addItem(category);
            }
            categoryComboBox.setSelectedItem(dish.getCategoryName());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load categories: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        mainPanel.add(categoryComboBox, gbc);

        // Price spinner
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Price:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        SpinnerNumberModel priceModel = new SpinnerNumberModel(dish.getSellingPrice(), 0.0, 10000.0, 0.1);
        priceSpinner = new JSpinner(priceModel);
        mainPanel.add(priceSpinner, gbc);

        // Recipe instructions
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Recipe:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        recipeArea = new JTextArea(dish.getRecipeInstructions(), 5, 20);
        recipeArea.setLineWrap(true);
        recipeArea.setWrapStyleWord(true);
        JScrollPane recipeScroll = new JScrollPane(recipeArea);
        mainPanel.add(recipeScroll, gbc);

        // Available checkbox
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        availableCheckBox = new JCheckBox("Available", dish.isAvailable());
        mainPanel.add(availableCheckBox, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> updateDish());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void updateDish() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a dish name.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String category = (String) categoryComboBox.getSelectedItem();
        double price = (Double) priceSpinner.getValue();
        String recipe = recipeArea.getText().trim();
        boolean available = availableCheckBox.isSelected();

        dish.setName(name);
        dish.setCategoryName(category);
        dish.setSellingPrice(price);
        dish.setRecipeInstructions(recipe);
        dish.setAvailable(available);

        try {
            if (controller.updateDish(dish)) {
                dishUpdated = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to update dish. Please try again.",
                        "Update Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database error while updating dish: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isDishUpdated() {
        return dishUpdated;
    }
} 