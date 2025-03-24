package controller;

import dao.LoginDAO;
import model.UserCredentials;

import java.sql.SQLException;

public class LoginController {
    private LoginDAO loginDAO;

    public LoginController() throws SQLException {
        this.loginDAO = new LoginDAO();
    }

    public UserCredentials authenticateUser(String username, String password) throws SQLException {
        return loginDAO.authenticateUser(username, password);
    }

    public boolean isUserActive(UserCredentials user) {
        return user != null && user.isActive();
    }
} 