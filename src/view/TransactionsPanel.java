package view;

import controller.RestaurantController;
import javax.swing.*;
import java.awt.*;

public class TransactionsPanel extends JPanel {
    private final RestaurantController controller;
    private final JTabbedPane tabbedPane;
    private final OrderPanel orderPanel;
    private final PaymentPanel paymentPanel;
    private final ShiftManagementPanel shiftPanel;

    public TransactionsPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        // Initialize panels
        orderPanel = new OrderPanel(controller);
        paymentPanel = new PaymentPanel(controller);
        shiftPanel = new ShiftManagementPanel(controller);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Orders", orderPanel);
        tabbedPane.addTab("Payments", paymentPanel);
        tabbedPane.addTab("Shifts", shiftPanel);

        // Add help button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton helpButton = new JButton("Help");
        helpButton.setToolTipText("Show help information");
        helpButton.addActionListener(e -> showHelp());
        topPanel.add(helpButton);

        // Add components
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void showHelp() {
        String helpText = """
            Transactions Panel Help
            
            Orders Tab:
            - Create and manage orders
            - Add/remove items from orders
            - Select customers and assign employees
            
            Payments Tab:
            - Process payments for orders
            - View payment history
            - Handle cash and card payments
            
            Shifts Tab:
            - Manage employee shifts
            - Assign and remove shifts
            - View current shift assignments
            """;

        JOptionPane.showMessageDialog(this,
            helpText,
            "Help",
            JOptionPane.INFORMATION_MESSAGE);
    }
} 