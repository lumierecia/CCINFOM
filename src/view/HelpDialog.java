package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Map;

public class HelpDialog extends JDialog {
    public HelpDialog(Frame owner, String title, String overview, Map<String, String> features, Map<String, String> shortcuts) {
        super(owner, title, true);
        setLayout(new BorderLayout(10, 10));
        
        // Create tabbed pane for different help sections
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Overview tab
        JPanel overviewPanel = new JPanel(new BorderLayout(5, 5));
        JTextArea overviewText = createTextArea(overview);
        overviewPanel.add(new JScrollPane(overviewText), BorderLayout.CENTER);
        tabbedPane.addTab("Overview", new ImageIcon(), overviewPanel, "General information about this panel");
        
        // Features tab
        if (features != null && !features.isEmpty()) {
            JPanel featuresPanel = new JPanel(new BorderLayout(5, 5));
            StringBuilder featuresText = new StringBuilder();
            features.forEach((feature, description) -> 
                featuresText.append(feature).append("\n").append(description).append("\n\n")
            );
            JTextArea featuresArea = createTextArea(featuresText.toString());
            featuresPanel.add(new JScrollPane(featuresArea), BorderLayout.CENTER);
            tabbedPane.addTab("Features", new ImageIcon(), featuresPanel, "Detailed feature descriptions");
        }
        
        // Shortcuts tab
        if (shortcuts != null && !shortcuts.isEmpty()) {
            JPanel shortcutsPanel = new JPanel(new BorderLayout(5, 5));
            StringBuilder shortcutsText = new StringBuilder();
            shortcuts.forEach((key, action) -> 
                shortcutsText.append(key).append(" - ").append(action).append("\n")
            );
            JTextArea shortcutsArea = createTextArea(shortcutsText.toString());
            shortcutsPanel.add(new JScrollPane(shortcutsArea), BorderLayout.CENTER);
            tabbedPane.addTab("Shortcuts", new ImageIcon(), shortcutsPanel, "Keyboard shortcuts");
        }
        
        // Add tabbed pane to dialog
        add(tabbedPane, BorderLayout.CENTER);
        
        // Add close button
        JButton closeButton = new JButton("Close");
        closeButton.setMnemonic(KeyEvent.VK_C);
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Set dialog properties
        setSize(500, 400);
        setLocationRelativeTo(owner);
    }
    
    private JTextArea createTextArea(String text) {
        JTextArea textArea = new JTextArea(text);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(5, 5, 5, 5));
        textArea.setFont(new Font("Arial", Font.PLAIN, 12));
        return textArea;
    }
} 