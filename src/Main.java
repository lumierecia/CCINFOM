import view.LoginView;

public class Main {
    public static void main(String[] args) {
        // Run on the Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
} 