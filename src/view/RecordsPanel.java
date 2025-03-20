package view;

import controller.RestaurantController;
import model.Restaurant;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class RecordsPanel extends JPanel {
    private JTable recordsTable;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JComboBox<Restaurant> restaurantComboBox;
    private RestaurantController controller;
    private DefaultTableModel tableModel;

    public RecordsPanel() {
        controller = new RestaurantController();
        setLayout(new BorderLayout());
        initComponents();
        loadData();
    }

    private void initComponents() {
        // Create toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        
        // Create dropdown for restaurants
        restaurantComboBox = new JComboBox<>();
        restaurantComboBox.setPreferredSize(new Dimension(300, 25));
        restaurantComboBox.addItem(null); // Add empty option for "Show All"
        
        toolBar.add(addButton);
        toolBar.add(editButton);
        toolBar.add(deleteButton);
        toolBar.add(new JLabel("Select Restaurant: "));
        toolBar.add(restaurantComboBox);

        // Create table
        String[] columnNames = {"ID", "Name", "Location", "Cuisine", "Rating"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        recordsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(recordsTable);

        // Add components to panel
        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add button listeners
        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteRecord());
        
        // Add combobox listener
        restaurantComboBox.addActionListener(e -> {
            Restaurant selected = (Restaurant) restaurantComboBox.getSelectedItem();
            if (selected == null) {
                loadData(); // Show all restaurants
            } else {
                filterByRestaurant(selected);
            }
        });
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Restaurant> restaurants = controller.getAllRestaurants();
        
        // Update combobox items
        restaurantComboBox.removeAllItems();
        restaurantComboBox.addItem(null); // "Show All" option
        for (Restaurant restaurant : restaurants) {
            restaurantComboBox.addItem(restaurant);
        }
        
        // Update table
        for (Restaurant restaurant : restaurants) {
            addRestaurantToTable(restaurant);
        }
    }

    private void filterByRestaurant(Restaurant selected) {
        tableModel.setRowCount(0);
        addRestaurantToTable(selected);
    }

    private void addRestaurantToTable(Restaurant restaurant) {
        Object[] row = {
            restaurant.getRestaurantID(),
            restaurant.getName(),
            restaurant.getLocation(),
            restaurant.getCuisine(),
            restaurant.getRating()
        };
        tableModel.addRow(row);
    }

    private void showAddDialog() {
        JTextField nameField = new JTextField();
        JTextField locationField = new JTextField();
        JTextField cuisineField = new JTextField();
        JTextField ratingField = new JTextField();

        Object[] message = {
            "Name:", nameField,
            "Location:", locationField,
            "Cuisine:", cuisineField,
            "Rating:", ratingField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Restaurant", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                String location = locationField.getText();
                String cuisine = cuisineField.getText();
                double rating = Double.parseDouble(ratingField.getText());

                if (controller.addRestaurant(name, location, cuisine, rating)) {
                    loadData();
                    JOptionPane.showMessageDialog(this, "Restaurant added successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add restaurant.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid rating format.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditDialog() {
        int selectedRow = recordsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a restaurant to edit.", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        Restaurant restaurant = controller.getRestaurantById(id);

        if (restaurant != null) {
            JTextField nameField = new JTextField(restaurant.getName());
            JTextField locationField = new JTextField(restaurant.getLocation());
            JTextField cuisineField = new JTextField(restaurant.getCuisine());
            JTextField ratingField = new JTextField(String.valueOf(restaurant.getRating()));

            Object[] message = {
                "Name:", nameField,
                "Location:", locationField,
                "Cuisine:", cuisineField,
                "Rating:", ratingField
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Edit Restaurant", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (option == JOptionPane.OK_OPTION) {
                try {
                    String name = nameField.getText();
                    String location = locationField.getText();
                    String cuisine = cuisineField.getText();
                    double rating = Double.parseDouble(ratingField.getText());

                    if (controller.updateRestaurant(id, name, location, cuisine, rating)) {
                        loadData();
                        JOptionPane.showMessageDialog(this, "Restaurant updated successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update restaurant.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid rating format.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteRecord() {
        int selectedRow = recordsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a restaurant to delete.", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this restaurant?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.deleteRestaurant(id)) {
                loadData();
                JOptionPane.showMessageDialog(this, "Restaurant deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete restaurant.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 