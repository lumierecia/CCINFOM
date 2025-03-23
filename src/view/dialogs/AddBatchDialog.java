package view.dialogs;

import controller.RestaurantController;
import model.Ingredient;
import model.Supplier;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.Calendar;
import java.util.List;

public class AddBatchDialog extends JDialog {
    private final RestaurantController controller;
    private JComboBox<Ingredient> ingredientCombo;
    private JComboBox<Supplier> supplierCombo;
    private JSpinner quantitySpinner;
    private JSpinner purchaseDateSpinner;
    private JSpinner expiryDateSpinner;
    private boolean confirmed = false;

    public AddBatchDialog(Frame parent, RestaurantController controller) {
        super(parent, "Add New Batch", true);
        this.controller = controller;
        
        // Set up dialog
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Initialize components
        initComponents();
        
        // Pack and center
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // Create main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Ingredient selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Ingredient:"), gbc);
        
        gbc.gridx = 1;
        List<Ingredient> ingredients = controller.getAllIngredients();
        ingredientCombo = new JComboBox<>(ingredients.toArray(new Ingredient[0]));
        mainPanel.add(ingredientCombo, gbc);

        // Supplier selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Supplier:"), gbc);
        
        gbc.gridx = 1;
        List<Supplier> suppliers = controller.getAllSuppliers();
        supplierCombo = new JComboBox<>(suppliers.toArray(new Supplier[0]));
        mainPanel.add(supplierCombo, gbc);

        // Quantity spinner
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Quantity:"), gbc);
        
        gbc.gridx = 1;
        SpinnerNumberModel quantityModel = new SpinnerNumberModel(1.0, 0.1, 10000.0, 0.1);
        quantitySpinner = new JSpinner(quantityModel);
        mainPanel.add(quantitySpinner, gbc);

        // Purchase date
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Purchase Date:"), gbc);
        
        gbc.gridx = 1;
        Calendar calendar = Calendar.getInstance();
        Date initDate = calendar.getTime();
        calendar.add(Calendar.YEAR, -1);
        Date earliestDate = calendar.getTime();
        calendar.add(Calendar.YEAR, 2);
        Date latestDate = calendar.getTime();
        SpinnerDateModel purchaseDateModel = new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.DAY_OF_MONTH);
        purchaseDateSpinner = new JSpinner(purchaseDateModel);
        purchaseDateSpinner.setEditor(new JSpinner.DateEditor(purchaseDateSpinner, "yyyy-MM-dd"));
        mainPanel.add(purchaseDateSpinner, gbc);

        // Expiry date
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("Expiry Date:"), gbc);
        
        gbc.gridx = 1;
        calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1); // Default expiry is one month from now
        initDate = calendar.getTime();
        calendar.add(Calendar.YEAR, -1);
        earliestDate = calendar.getTime();
        calendar.add(Calendar.YEAR, 5);
        latestDate = calendar.getTime();
        SpinnerDateModel expiryDateModel = new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.DAY_OF_MONTH);
        expiryDateSpinner = new JSpinner(expiryDateModel);
        expiryDateSpinner.setEditor(new JSpinner.DateEditor(expiryDateSpinner, "yyyy-MM-dd"));
        mainPanel.add(expiryDateSpinner, gbc);

        // Add main panel
        add(mainPanel, BorderLayout.CENTER);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");

        addButton.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Set default button
        getRootPane().setDefaultButton(addButton);

        // Add window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmed = false;
                dispose();
            }
        });
    }

    private boolean validateInput() {
        if (ingredientCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                "Please select an ingredient.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (supplierCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a supplier.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Date purchaseDate = (Date) purchaseDateSpinner.getValue();
        Date expiryDate = (Date) expiryDateSpinner.getValue();

        if (expiryDate.before(purchaseDate)) {
            JOptionPane.showMessageDialog(this,
                "Expiry date cannot be before purchase date.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Ingredient getSelectedIngredient() {
        return (Ingredient) ingredientCombo.getSelectedItem();
    }

    public Supplier getSelectedSupplier() {
        return (Supplier) supplierCombo.getSelectedItem();
    }

    public double getQuantity() {
        return ((Number) quantitySpinner.getValue()).doubleValue();
    }

    public Date getPurchaseDate() {
        return (Date) purchaseDateSpinner.getValue();
    }

    public Date getExpiryDate() {
        return (Date) expiryDateSpinner.getValue();
    }
} 