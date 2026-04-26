package com.library.client.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.library.client.MainApp;
import com.library.client.api.ApiClient;
import com.library.client.session.SessionManager;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.debug("LoginController initialized");
    }

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

        Task<String> loginTask = ApiClient.getInstance().login(username, password);

        loginTask.setOnSucceeded(event -> {
            String token = (String) event.getSource().getValue();
            SessionManager.getInstance().setToken(token);
            logger.info("Login successful");
            try {
                MainApp.switchScene("/fxml/dashboard.fxml");
            } catch (IOException e) {
                showError("Failed to load dashboard: " + e.getMessage());
                loginButton.setDisable(false);
            }
        });

        loginTask.setOnFailed(event -> {
            Throwable exception = event.getSource().getException();
            logger.error("Login failed", exception);
            showError("Login failed: " + exception.getMessage());
            loginButton.setDisable(false);
        });

        new Thread(loginTask).start();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }
}