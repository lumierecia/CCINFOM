package view.panels;

import controller.RestaurantController;
import model.Dish;
import view.dialogs.AddDishDialog;
import view.dialogs.EditDishDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DishPanel extends JPanel {
    private final RestaurantController controller;
    private final JTable dishTable;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> categoryFilter;
    private final JCheckBox showUnavailableCheckbox;

    public DishPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        // Create toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        // Add dish button
        JButton addButton = new JButton("Add Dish");
        addButton.addActionListener(e -> showAddDishDialog());
        toolbar.add(addButton);

        // Edit dish button
        JButton editButton = new JButton("Edit Dish");
        editButton.addActionListener(e -> showEditDishDialog());
        toolbar.add(editButton);

        // Delete dish button
        JButton deleteButton = new JButton("Delete Dish");
        deleteButton.addActionListener(e -> deleteDish());
        toolbar.add(deleteButton);

        toolbar.addSeparator();

        // Category filter
        toolbar.add(new JLabel("Category: "));
        categoryFilter = new JComboBox<>();
        categoryFilter.addItem("All Categories");
        // Add categories from database
        List<String> categories = controller.getAllCategories();
        for (String category : categories) {
            categoryFilter.addItem(category);
        }
        categoryFilter.addActionListener(e -> refreshTable());
        toolbar.add(categoryFilter);

        toolbar.addSeparator();

        // Show unavailable checkbox
        showUnavailableCheckbox = new JCheckBox("Show Unavailable");
        showUnavailableCheckbox.addActionListener(e -> refreshTable());
        toolbar.add(showUnavailableCheckbox);

        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshTable());
        toolbar.add(refreshButton);

        add(toolbar, BorderLayout.NORTH);

        // Create table
        String[] columnNames = {"ID", "Name", "Category", "Price", "Available"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dishTable = new JTable(tableModel);
        dishTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        dishTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        dishTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        dishTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        dishTable.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(dishTable);
        add(scrollPane, BorderLayout.CENTER);

        // Initial table population
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Dish> dishes;
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        
        if ("All Categories".equals(selectedCategory)) {
            dishes = controller.getAllDishes();
        } else {
            dishes = controller.getDishesByCategory(selectedCategory);
        }

        for (Dish dish : dishes) {
            if (showUnavailableCheckbox.isSelected() || dish.isAvailable()) {
                Object[] row = {
                    dish.getDishId(),
                    dish.getName(),
                    dish.getCategoryName(),
                    String.format("%.2f", dish.getSellingPrice()),
                    dish.isAvailable() ? "Yes" : "No"
                };
                tableModel.addRow(row);
            }
        }
    }

    private void showAddDishDialog() {
        AddDishDialog dialog = new AddDishDialog(SwingUtilities.getWindowAncestor(this), controller);
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

        int dishId = (int) dishTable.getValueAt(selectedRow, 0);
        Dish dish = controller.getDishById(dishId);
        if (dish != null) {
            EditDishDialog dialog = new EditDishDialog(SwingUtilities.getWindowAncestor(this), controller, dish);
            dialog.setVisible(true);
            if (dialog.isDishUpdated()) {
                refreshTable();
            }
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

        int dishId = (int) dishTable.getValueAt(selectedRow, 0);
        String dishName = (String) dishTable.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete " + dishName + "?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.deleteDish(dishId)) {
                refreshTable();
                JOptionPane.showMessageDialog(this,
                    "Dish deleted successfully.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
} 