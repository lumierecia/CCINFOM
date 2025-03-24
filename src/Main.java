import view.LoginView;

public class Main {
    public static void main(String[] args) {
        // Run on the Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                LoginView loginView = new LoginView();
                loginView.setVisible(true);
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Error starting application: " + e.getMessage(),
                        "Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
} 