package view.dialogs;

import controller.RestaurantController;
import model.IngredientBatch;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import java.util.Calendar;

public class EditIngredientBatchDialog extends JDialog {
    private final RestaurantController controller;
    private final IngredientBatch batch;
    private JSpinner quantitySpinner;
    private JSpinner priceSpinner;
    private JSpinner purchaseDateSpinner;
    private JSpinner expiryDateSpinner;
    private boolean confirmed = false;

    public EditIngredientBatchDialog(Frame owner, RestaurantController controller, IngredientBatch batch) {
        super(owner, "Edit Ingredient Batch", true);
        this.controller = controller;
        this.batch = batch;
        initComponents();
        loadBatchData();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Create main panel with GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Ingredient label
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Ingredient:"), gbc);
        
        gbc.gridx = 1;
        mainPanel.add(new JLabel(batch.getIngredientName()), gbc);

        // Supplier label
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Supplier:"), gbc);
        
        gbc.gridx = 1;
        mainPanel.add(new JLabel(batch.getSupplierName()), gbc);

        // Quantity spinner
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Remaining Quantity:"), gbc);
        
        gbc.gridx = 1;
        SpinnerNumberModel quantityModel = new SpinnerNumberModel(0.0, 0.0, batch.getQuantity(), 0.1);
        quantitySpinner = new JSpinner(quantityModel);
        mainPanel.add(quantitySpinner, gbc);

        // Purchase price spinner
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Purchase Price:"), gbc);
        
        gbc.gridx = 1;
        SpinnerNumberModel priceModel = new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.1);
        priceSpinner = new JSpinner(priceModel);
        mainPanel.add(priceSpinner, gbc);

        // Purchase date spinner
        gbc.gridx = 0;
        gbc.gridy = 4;
        mainPanel.add(new JLabel("Purchase Date:"), gbc);
        
        gbc.gridx = 1;
        Calendar calendar = Calendar.getInstance();
        Date initDate = calendar.getTime();
        calendar.add(Calendar.YEAR, -100);
        Date earliestDate = calendar.getTime();
        calendar.add(Calendar.YEAR, 200);
        Date latestDate = calendar.getTime();
        SpinnerDateModel purchaseDateModel = new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.DAY_OF_MONTH);
        purchaseDateSpinner = new JSpinner(purchaseDateModel);
        purchaseDateSpinner.setEditor(new JSpinner.DateEditor(purchaseDateSpinner, "yyyy-MM-dd"));
        mainPanel.add(purchaseDateSpinner, gbc);

        // Expiry date spinner
        gbc.gridx = 0;
        gbc.gridy = 5;
        mainPanel.add(new JLabel("Expiry Date:"), gbc);
        
        gbc.gridx = 1;
        SpinnerDateModel expiryDateModel = new SpinnerDateModel(initDate, earliestDate, latestDate, Calendar.DAY_OF_MONTH);
        expiryDateSpinner = new JSpinner(expiryDateModel);
        expiryDateSpinner.setEditor(new JSpinner.DateEditor(expiryDateSpinner, "yyyy-MM-dd"));
        mainPanel.add(expiryDateSpinner, gbc);

        // Status label
        gbc.gridx = 0;
        gbc.gridy = 6;
        mainPanel.add(new JLabel("Status:"), gbc);
        
        gbc.gridx = 1;
        mainPanel.add(new JLabel(batch.getStatus()), gbc);

        // Add main panel
        add(mainPanel, BorderLayout.CENTER);

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Set default button
        getRootPane().setDefaultButton(saveButton);

        // Add window listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmed = false;
                dispose();
            }
        });
    }

    private void loadBatchData() {
        quantitySpinner.setValue(batch.getRemainingQuantity());
        priceSpinner.setValue(batch.getPurchasePrice());
        purchaseDateSpinner.setValue(batch.getPurchaseDate());
        expiryDateSpinner.setValue(batch.getExpiryDate());
    }

    private boolean validateInput() {
        double quantity = (Double) quantitySpinner.getValue();
        if (quantity < 0 || quantity > batch.getQuantity()) {
            JOptionPane.showMessageDialog(this,
                "Remaining quantity must be between 0 and initial quantity.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }

        double price = (Double) priceSpinner.getValue();
        if (price <= 0) {
            JOptionPane.showMessageDialog(this,
                "Purchase price must be greater than 0.",
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

    public IngredientBatch getUpdatedBatch() {
        if (!confirmed) return null;

        batch.setRemainingQuantity((Double) quantitySpinner.getValue());
        batch.setPurchasePrice((Double) priceSpinner.getValue());
        batch.setPurchaseDate((Date) purchaseDateSpinner.getValue());
        batch.setExpiryDate((Date) expiryDateSpinner.getValue());

        // Update status if remaining quantity is 0
        if (batch.getRemainingQuantity() == 0) {
            batch.setStatus("DEPLETED");
        }

        return batch;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
} 