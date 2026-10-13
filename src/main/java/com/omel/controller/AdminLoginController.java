package com.omel.controller;

import com.omel.OmelApp;
import com.omel.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.logging.Logger;

public class AdminLoginController {
    private static final Logger LOG = Logger.getLogger(AdminLoginController.class.getName());
    private static final AuthService AUTH = AuthService.fromEnvironment(System.getenv());

    static {
        if (AUTH.isDemoCredentials()) {
            LOG.warning("Using the demo admin login. Set " + AuthService.ENV_USER + " and "
                    + AuthService.ENV_PASSWORD + " to choose your own.");
        }
    }

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML private void login() {
        if (AUTH.authenticate(usernameField.getText(), passwordField.getText())) {
            OmelApp.setRoot("AdminView", "Omel Admin Dashboard");
        } else {
            errorLabel.setText("Invalid username or password.");
            passwordField.clear();
        }
    }

    @FXML private void back() { OmelApp.showLaunch(); }
}
