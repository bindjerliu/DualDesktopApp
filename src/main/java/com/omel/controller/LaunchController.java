package com.omel.controller;

import com.omel.OmelApp;
import javafx.fxml.FXML;

public class LaunchController {
    @FXML private void openUserApp() { OmelApp.setRoot("UserView", "Omel User App"); }
    @FXML private void openAdminLogin() { OmelApp.setRoot("AdminLoginView", "Omel Admin Login"); }
}
