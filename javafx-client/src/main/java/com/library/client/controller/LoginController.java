package com.library.client.controller;

import com.library.client.MainApp;
import com.library.client.api.ApiClient;
import com.library.client.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the login screen.
 * Handles user authentication and stores JWT token in SessionManager.
 */
public class LoginController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.debug("LoginController initialized");
    }

    /**
     * Handles login button click.
     * Calls ApiClient.login() on background thread.
     * On success: stores token and navigates to dashboard.
     * On failure: displays error alert.
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }

        loginButton.setDisable(true);
        errorLabel.setText("");

        // Call login on background thread
        ApiClient.getInstance().login(username, password)
                .setOnSucceeded(event -> {
                    String token = (String) event.getSource().getValue();
                    SessionManager.getInstance().setToken(token);
                    logger.info("Login successful");
                    try {
                        MainApp.switchScene("/fxml/dashboard.fxml");
                    } catch (IOException e) {
                        showError("Failed to load dashboard: " + e.getMessage());
                        loginButton.setDisable(false);
                    }
                })
                .setOnFailed(event -> {
                    Throwable exception = event.getSource().getException();
                    logger.error("Login failed", exception);
                    showError("Login failed: " + exception.getMessage());
                    loginButton.setDisable(false);
                });

        // Execute task
        new Thread(ApiClient.getInstance().login(username, password)).start();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }
}
