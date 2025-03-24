package view;

import controller.DishController;
import model.Dish;
import model.DishIngredient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DishView extends JFrame {
    private DishController dishController;
    private JTable dishTable;
    private DefaultTableModel tableModel;
    private JTextField nameField;
    private JTextField priceField;
    private JComboBox<String> categoryComboBox;
    private JTextArea recipeArea;
    private JCheckBox availableCheckBox;
    private JTextArea ingredientsArea;

    public DishView() {
        try {
            dishController = new DishController();
            initializeUI();
            loadDishes();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error connecting to database: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initializeUI() {
        setTitle("Menu Management");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        // Price field
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Price:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        priceField = new JTextField(20);
        formPanel.add(priceField, gbc);

        // Category combo box
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        categoryComboBox = new JComboBox<>(new String[]{"Appetizers", "Main Course", "Desserts", "Beverages"});
        formPanel.add(categoryComboBox, gbc);

        // Recipe area
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Recipe:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        recipeArea = new JTextArea(5, 20);
        recipeArea.setLineWrap(true);
        formPanel.add(new JScrollPane(recipeArea), gbc);

        // Available checkbox
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Available:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        availableCheckBox = new JCheckBox();
        formPanel.add(availableCheckBox, gbc);

        // Ingredients area
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Ingredients (one per line):"), gbc);
        gbc.gridx = 1; gbc.gridy = 5;
        ingredientsArea = new JTextArea(5, 20);
        ingredientsArea.setLineWrap(true);
        formPanel.add(new JScrollPane(ingredientsArea), gbc);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Dish");
        JButton updateButton = new JButton("Update Dish");
        JButton deleteButton = new JButton("Delete Dish");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        // Table
        String[] columns = {"ID", "Name", "Category", "Price", "Available"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dishTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(dishTable);

        // Add components to main panel
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        // Add action listeners
        addButton.addActionListener(e -> handleAddDish());
        updateButton.addActionListener(e -> handleUpdateDish());
        deleteButton.addActionListener(e -> handleDeleteDish());
        refreshButton.addActionListener(e -> loadDishes());

        // Add selection listener to table
        dishTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = dishTable.getSelectedRow();
                if (selectedRow >= 0) {
                    loadDishData(selectedRow);
                }
            }
        });

        add(mainPanel);
    }

    private void loadDishes() {
        tableModel.setRowCount(0);
        List<Dish> dishes = dishController.getAllDishes();
        for (Dish dish : dishes) {
            tableModel.addRow(new Object[]{
                dish.getDishId(),
                dish.getName(),
                getCategoryName(dish.getCategoryId()),
                dish.getSellingPrice(),
                dish.isAvailable() ? "Yes" : "No"
            });
        }
    }

    private void loadDishData(int row) {
        int dishId = (int) tableModel.getValueAt(row, 0);
        Dish dish = dishController.getDishById(dishId);
        if (dish != null) {
            nameField.setText(dish.getName());
            priceField.setText(String.valueOf(dish.getSellingPrice()));
            categoryComboBox.setSelectedItem(getCategoryName(dish.getCategoryId()));
            recipeArea.setText(dish.getRecipeInstructions());
            availableCheckBox.setSelected(dish.isAvailable());

            StringBuilder ingredients = new StringBuilder();
            for (DishIngredient ingredient : dish.getIngredients()) {
                ingredients.append(ingredient.toString()).append("\n");
            }
            ingredientsArea.setText(ingredients.toString());
        }
    }

    private void handleAddDish() {
        try {
            Dish dish = new Dish();
            dish.setName(nameField.getText());
            dish.setSellingPrice(Double.parseDouble(priceField.getText()));
            dish.setCategoryId(getCategoryId((String) categoryComboBox.getSelectedItem()));
            dish.setRecipeInstructions(recipeArea.getText());
            dish.setAvailable(availableCheckBox.isSelected());

            List<DishIngredient> ingredients = parseIngredients(ingredientsArea.getText());

            if (dishController.addDish(dish, ingredients)) {
                JOptionPane.showMessageDialog(this, "Dish added successfully!");
                clearForm();
                loadDishes();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add dish.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid price.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateDish() {
        int selectedRow = dishTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a dish to update.");
            return;
        }

        try {
            int dishId = (int) tableModel.getValueAt(selectedRow, 0);
            Dish dish = dishController.getDishById(dishId);
            if (dish != null) {
                dish.setName(nameField.getText());
                dish.setSellingPrice(Double.parseDouble(priceField.getText()));
                dish.setCategoryId(getCategoryId((String) categoryComboBox.getSelectedItem()));
                dish.setRecipeInstructions(recipeArea.getText());
                dish.setAvailable(availableCheckBox.isSelected());

                if (dishController.updateDish(dish)) {
                    JOptionPane.showMessageDialog(this, "Dish updated successfully!");
                    loadDishes();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update dish.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid price.",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteDish() {
        int selectedRow = dishTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a dish to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this dish?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            int dishId = (int) tableModel.getValueAt(selectedRow, 0);
            if (dishController.deleteDish(dishId)) {
                JOptionPane.showMessageDialog(this, "Dish deleted successfully!");
                clearForm();
                loadDishes();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete dish.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearForm() {
        nameField.setText("");
        priceField.setText("");
        categoryComboBox.setSelectedIndex(0);
        recipeArea.setText("");
        availableCheckBox.setSelected(true);
        ingredientsArea.setText("");
    }

    private String getCategoryName(int categoryId) {
        switch (categoryId) {
            case 1: return "Appetizers";
            case 2: return "Main Course";
            case 3: return "Desserts";
            case 4: return "Beverages";
            default: return "Unknown";
        }
    }

    private int getCategoryId(String categoryName) {
        switch (categoryName) {
            case "Appetizers": return 1;
            case "Main Course": return 2;
            case "Desserts": return 3;
            case "Beverages": return 4;
            default: return 0;
        }
    }

    private List<DishIngredient> parseIngredients(String ingredientsText) {
        // TODO: Implement ingredient parsing logic
        // This should parse the ingredients text and create DishIngredient objects
        // For now, return an empty list
        return List.of();
    }
} 