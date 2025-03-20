package view;

import controller.RestaurantController;
import model.Inventory;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class RecordsPanel extends JPanel {
    private RestaurantController controller;
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> categoryComboBox;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton helpButton;

    public RecordsPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());
        initComponents();
        loadData();
    }

    private void initComponents() {
        // Create toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        // Initialize category combo box with categories from database
        categoryComboBox = new JComboBox<>(controller.getAllCategories().toArray(new String[0]));
        categoryComboBox.setPreferredSize(new Dimension(300, 25));
        categoryComboBox.setToolTipText("Filter items by category");

        // Add components to toolbar
        toolBar.add(new JLabel("Category: "));
        toolBar.add(categoryComboBox);
        toolBar.addSeparator(new Dimension(10, 0));

        // Initialize buttons
        addButton = new JButton("Add Item");
        editButton = new JButton("Edit Item");
        deleteButton = new JButton("Delete Item");
        helpButton = new JButton("Help");

        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.add(helpButton);

        // Initialize table
        String[] columnNames = {"ID", "Name", "Category", "Make Price", "Sell Price", "Quantity", "Status"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        // Add components to panel
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add action listeners
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteSelectedItem());
        helpButton.addActionListener(e -> showHelpDialog());
        categoryComboBox.addActionListener(e -> {
            String selected = (String) categoryComboBox.getSelectedItem();
            if (selected != null) {
                filterByCategory(selected);
            }
        });
    }

    private void loadData() {
        model.setRowCount(0);
        List<Inventory> items = controller.getAllInventoryItems();
        for (Inventory item : items) {
            model.addRow(new Object[]{
                item.getProductId(),
                item.getProductName(),
                item.getCategoryId(),
                item.getMakePrice(),
                item.getSellPrice(),
                item.getQuantity()
            });
        }
    }

    private void showHelpDialog() {
        String helpMessage = "Welcome to the Records Panel!\n\n" +
                "This panel allows you to manage your inventory items. " +
                "You can add, edit, and delete items, as well as filter them by category.\n\n" +
                "Features:\n" +
                "- Use the Category dropdown to filter items by their category. " +
                "- Click 'Add Item' to create a new inventory item.\n" +
                "- Select an item and click 'Edit' to modify it.\n" +
                "- Select an item and click 'Delete' to remove it.\n\n" +
                "The table shows: ID, Name, Category, Make Price, Sell Price, " +
                "Quantity, and Status of each item.";

        JOptionPane.showMessageDialog(this,
                helpMessage,
                "Help - Records Panel",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void filterByCategory(String category) {
        model.setRowCount(0);
        System.out.println("Filtering by category: " + category); // Debug message
        List<Inventory> items = controller.getInventoryItemsByCategory(category);
        if (items.isEmpty()) {
            System.out.println("No items found for category: " + category); // Debug message
            JOptionPane.showMessageDialog(this,
                    "No items found in category: " + category,
                    "No Results",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for (Inventory item : items) {
            model.addRow(new Object[]{
                item.getProductId(),
                item.getProductName(),
                item.getCategoryId(),
                item.getMakePrice(),
                item.getSellPrice(),
                item.getQuantity()
            });
        }
    }

    private void showAddDialog() {
        JTextField nameField = new JTextField(20);
        JComboBox<String> categoryField = new JComboBox<>(controller.getAllCategories().toArray(new String[0]));
        JTextField makePriceField = new JTextField(10);
        JTextField sellPriceField = new JTextField(10);
        JTextField quantityField = new JTextField(10);
        JTextArea recipeField = new JTextArea(5, 20);
        recipeField.setLineWrap(true);
        recipeField.setWrapStyleWord(true);
        JScrollPane recipeScrollPane = new JScrollPane(recipeField);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Make Price:"));
        panel.add(makePriceField);
        panel.add(new JLabel("Sell Price:"));
        panel.add(sellPriceField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("Recipe:"));
        panel.add(recipeScrollPane);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Add New Item", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                String category = (String) categoryField.getSelectedItem();
                double makePrice = Double.parseDouble(makePriceField.getText());
                double sellPrice = Double.parseDouble(sellPriceField.getText());
                int quantity = Integer.parseInt(quantityField.getText());
                String recipe = recipeField.getText();

                if (controller.addInventoryItem(name, category, makePrice, sellPrice, quantity, recipe)) {
                    loadData();
                    JOptionPane.showMessageDialog(this,
                            "Item added successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to add item.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numbers for prices and quantity.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) table.getValueAt(selectedRow, 0);
        Inventory item = controller.getInventoryItemById(id);
        if (item == null) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load item details.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField nameField = new JTextField(item.getProductName(), 20);
        JComboBox<String> categoryField = new JComboBox<>(controller.getAllCategories().toArray(new String[0]));
        categoryField.setSelectedItem(item.getCategoryId());
        JTextField makePriceField = new JTextField(String.valueOf(item.getMakePrice()), 10);
        JTextField sellPriceField = new JTextField(String.valueOf(item.getSellPrice()), 10);
        JTextField quantityField = new JTextField(String.valueOf(item.getQuantity()), 10);
        JTextArea recipeField = new JTextArea(item.getRecipeInstructions(), 5, 20);
        recipeField.setLineWrap(true);
        recipeField.setWrapStyleWord(true);
        JScrollPane recipeScrollPane = new JScrollPane(recipeField);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Category:"));
        panel.add(categoryField);
        panel.add(new JLabel("Make Price:"));
        panel.add(makePriceField);
        panel.add(new JLabel("Sell Price:"));
        panel.add(sellPriceField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);
        panel.add(new JLabel("Recipe:"));
        panel.add(recipeScrollPane);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Edit Item", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                String category = (String) categoryField.getSelectedItem();
                double makePrice = Double.parseDouble(makePriceField.getText());
                double sellPrice = Double.parseDouble(sellPriceField.getText());
                int quantity = Integer.parseInt(quantityField.getText());
                String recipe = recipeField.getText();

                if (controller.updateInventoryItem(id, name, category, makePrice, sellPrice, quantity, recipe)) {
                    loadData();
                    JOptionPane.showMessageDialog(this,
                            "Item updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to update item.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numbers for prices and quantity.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an item to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) table.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this item?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.deleteInventoryItem(id)) {
                loadData();
                JOptionPane.showMessageDialog(this,
                        "Item deleted successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete item.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 