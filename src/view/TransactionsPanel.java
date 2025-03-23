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
    private final JButton helpButton;

    public TransactionsPanel(RestaurantController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        // Create help button
        helpButton = new JButton("Help");
        helpButton.setIcon(new ImageIcon(getClass().getResource("/icons/help.png")));
        helpButton.addActionListener(e -> showHelp());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.add(helpButton);
        add(topPanel, BorderLayout.NORTH);

        // Initialize panels
        orderPanel = new OrderPanel(controller);
        paymentPanel = new PaymentPanel(controller);
        shiftPanel = new ShiftManagementPanel(controller);

        // Create tabbed pane with custom styling
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font(tabbedPane.getFont().getName(), Font.BOLD, 14));
        tabbedPane.setBackground(new Color(240, 240, 240));
        tabbedPane.setForeground(new Color(50, 50, 50));
        
        // Add tabs without icons
        tabbedPane.addTab("Orders", orderPanel);
        tabbedPane.addTab("Payments", paymentPanel);
        tabbedPane.addTab("Shifts", shiftPanel);

        // Add components
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void showHelp() {
        HelpDialog helpDialog = new HelpDialog(SwingUtilities.getWindowAncestor(this), "Transactions");
        helpDialog.setVisible(true);
    }
} 