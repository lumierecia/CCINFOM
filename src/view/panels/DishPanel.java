package view.panels;

import controller.RestaurantController;
import model.Dish;
import util.StyledComponents;
import view.dialogs.AddDishDialog;
import view.dialogs.EditDishDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class DishPanel extends JPanel {
    private final RestaurantController controller;
    private final JTable dishTable;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> categoryFilter;
    private final JCheckBox showUnavailableCheckbox;
    private final JButton helpButton;

    public DishPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        // Create toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        // Add dish button
        JButton addButton = StyledComponents.createStyledButton("Add Dish", new Color(40, 167, 69));
        addButton.addActionListener(e -> showAddDishDialog());
        toolbar.add(addButton);

        // Edit dish button
        JButton editButton = StyledComponents.createStyledButton("Edit Dish", new Color(255, 193, 7));
        editButton.addActionListener(e -> showEditDishDialog());
        toolbar.add(editButton);

        // Delete dish button
        JButton deleteButton = StyledComponents.createStyledButton("Delete Dish", new Color(220, 53, 69));
        deleteButton.addActionListener(e -> deleteDish());
        toolbar.add(deleteButton);

        toolbar.addSeparator();

        // Category filter
        toolbar.add(new JLabel("Category: "));
        categoryFilter = new JComboBox<>();
        categoryFilter.addItem("All Categories");
        try {
            // Add categories from database
            List<String> categories = controller.getAllCategories();
            for (String category : categories) {
                categoryFilter.addItem(category);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading categories: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        categoryFilter.addActionListener(e -> refreshTable());
        toolbar.add(categoryFilter);

        toolbar.addSeparator();

        // Show unavailable checkbox
        showUnavailableCheckbox = new JCheckBox("Show Unavailable");
        showUnavailableCheckbox.addActionListener(e -> refreshTable());
        toolbar.add(showUnavailableCheckbox);

        // Refresh button
        JButton refreshButton = StyledComponents.createStyledButton("Refresh", new Color(70, 130, 180));
        refreshButton.addActionListener(e -> refreshTable());
        toolbar.add(refreshButton);

        // Help button
        helpButton = StyledComponents.createStyledButton("Help", new Color(108, 117, 125));
        helpButton.addActionListener(e -> showHelp());
        toolbar.add(helpButton);

        add(toolbar, BorderLayout.NORTH);

        // Create table
        String[] columns = {"ID", "Name", "Category", "Price", "Available"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dishTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(dishTable);
        add(scrollPane, BorderLayout.CENTER);

        // Initial load
        refreshTable();
    }

    private void showAddDishDialog() {
        AddDishDialog dialog = new AddDishDialog((Frame)SwingUtilities.getWindowAncestor(this), controller);
        dialog.setVisible(true);
        if (dialog.isDishAdded()) {
            refreshTable();
        }
    }

    private void showEditDishDialog() {
        int selectedRow = dishTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a dish to edit.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int dishId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            Dish dish = controller.getDishById(dishId);
            if (dish != null) {
                EditDishDialog dialog = new EditDishDialog((Frame)SwingUtilities.getWindowAncestor(this), controller, dish);
                dialog.setVisible(true);
                if (dialog.isDishUpdated()) {
                    refreshTable();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading dish: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDish() {
        int selectedRow = dishTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a dish to delete.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int dishId = (int) tableModel.getValueAt(selectedRow, 0);
        String dishName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete " + dishName + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (controller.deleteDish(dishId)) {
                    refreshTable();
                    JOptionPane.showMessageDialog(this,
                        "Dish deleted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to delete dish.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error deleting dish: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            List<Dish> dishes = controller.getAllDishes();
            String selectedCategory = (String) categoryFilter.getSelectedItem();
            boolean showUnavailable = showUnavailableCheckbox.isSelected();

            for (Dish dish : dishes) {
                if ((selectedCategory.equals("All Categories") || 
                     selectedCategory.equals(dish.getCategoryName())) &&
                    (showUnavailable || dish.isAvailable())) {
                    tableModel.addRow(new Object[]{
                        dish.getDishId(),
                        dish.getName(),
                        dish.getCategoryName(),
                        String.format("$%.2f", dish.getSellingPrice()),
                        dish.isAvailable() ? "Yes" : "No"
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error loading dishes: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showHelp() {
        String helpText = """
            Dish Management Help:
            
            1. Adding Dishes:
               • Click "Add Dish" to create new dish
               • Enter name, category, and price
               • Set availability status
            
            2. Managing Dishes:
               • Select a dish to edit or delete
               • Click "Edit Dish" to modify details
               • Click "Delete Dish" to remove
            
            3. Filtering Dishes:
               • Use category filter to view specific dishes
               • Check "Show Unavailable" to see all dishes
               • Click "Refresh" to update the list
            
            4. Additional Features:
               • Price and availability tracking
               • Category-based organization
               • Real-time updates
            """;
        
        JOptionPane.showMessageDialog(this,
            helpText,
            "Dish Management Help",
            JOptionPane.INFORMATION_MESSAGE);
    }
} 