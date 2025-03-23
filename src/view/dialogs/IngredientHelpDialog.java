package view.dialogs;

import javax.swing.*;
import java.awt.*;

public class IngredientHelpDialog extends JDialog {
    public IngredientHelpDialog(Frame parent) {
        super(parent, "Ingredient Management Help", true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Create a styled text area for the help content
        JTextArea helpText = new JTextArea();
        helpText.setEditable(false);
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        helpText.setFont(new Font("Arial", Font.PLAIN, 14));
        helpText.setMargin(new Insets(10, 10, 10, 10));
        
        // Add the help content
        helpText.setText("""
            Ingredient Management System Help
            ===============================
            
            Overview:
            The ingredient management system allows you to track and manage all ingredients used in your restaurant's dishes.
            
            Main Features:
            1. View Ingredients
               - See all ingredients with their current stock levels
               - Filter low stock ingredients
               - View ingredient details including unit price and supplier information
            
            2. Manage Batches
               - Track ingredient batches with expiry dates
               - Monitor remaining quantities
               - View batch status (Available, Low, Expired, Depleted)
            
            3. Add New Ingredients
               - Click "Add Ingredient" to create a new ingredient
               - Set minimum stock levels for automatic alerts
               - Assign suppliers and unit prices
            
            4. Edit Ingredients
               - Update ingredient details
               - Modify stock levels and prices
               - Change supplier information
            
            5. Delete Ingredients
               - Safely remove ingredients no longer in use
               - System prevents deletion if ingredient is used in recipes
            
            6. Batch Management
               - Add new batches of ingredients
               - Track expiry dates
               - Monitor usage with FIFO (First In, First Out) system
            
            Tips:
            - Regularly check the "Low Stock" filter to manage inventory
            - Monitor expiring batches to prevent waste
            - Update minimum stock levels based on usage patterns
            - Keep supplier information current for efficient restocking
            
            For additional assistance, please contact the system administrator.
            """);
        
        JScrollPane scrollPane = new JScrollPane(helpText);
        add(scrollPane, BorderLayout.CENTER);
        
        // Add close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set dialog size and location
        setSize(600, 700);
        setLocationRelativeTo(getParent());
    }
} 