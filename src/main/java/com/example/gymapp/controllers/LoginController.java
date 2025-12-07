package com.example.gymapp.controllers;

import com.example.gymapp.db.GymRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private TextField newUsernameField;

    @FXML
    private TextField registerEmailField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label registerStatusLabel;

    private GymRepository repository;

    @FXML
    private void initialize() {
        try {
            repository = new GymRepository();
        } catch (Exception e) {
            errorLabel.setText("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        attemptLogin(username, password);
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        registerStatusLabel.setText("");
        errorLabel.setText("");

        if (repository == null) {
            registerStatusLabel.setText("Database not available.");
            return;
        }

        String newUser = newUsernameField.getText();
        String email = registerEmailField.getText();
        String newPass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (newUser == null || newUser.isBlank() || newPass == null || newPass.isBlank() ||
                email == null || email.isBlank() || confirm == null || confirm.isBlank()) {
            registerStatusLabel.setText("All fields are required.");
            return;
        }

        if (!newPass.equals(confirm)) {
            registerStatusLabel.setText("Passwords do not match.");
            return;
        }

        try {
            repository.registerUser(newUser.trim(), email.trim(), newPass);
            registerStatusLabel.setText("Registered! You can log in now.");
            usernameField.setText(newUser.trim());
            passwordField.clear();
            confirmPasswordField.clear();
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("duplicate")) {
                registerStatusLabel.setText("Username already exists. Choose another.");
            } else {
                registerStatusLabel.setText("Could not register: " + e.getMessage());
            }
        }
    }

    private void openDashboard(String username, String role) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/gymapp/views/dashboard-view.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.setCurrentUser(username, role);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/example/gymapp/styles/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Could not open dashboard");
        }
    }

    private void attemptLogin(String username, String password) {
        errorLabel.setText("");
        registerStatusLabel.setText("");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            errorLabel.setText("Username and password are required.");
            return;
        }

        if (repository == null) {
            errorLabel.setText("Database not available.");
            return;
        }

        try {
            String role = repository.authenticate(username.trim(), password);
            if (role != null) {
                openDashboard(username.trim(), role);
            } else {
                errorLabel.setText("Invalid username or password");
            }
        } catch (SQLException e) {
            errorLabel.setText("Login failed: " + e.getMessage());
        }
    }
}
