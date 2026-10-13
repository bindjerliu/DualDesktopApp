package com.omel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class OmelApp extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        stage.setTitle("Omel Ordering App");
        showLaunch();
        stage.show();
    }

    public static void setRoot(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(OmelApp.class.getResource("/com/omel/view/" + fxml + ".fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1180, 760);
            scene.getStylesheets().add(OmelApp.class.getResource("/com/omel/styles/premium.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.setMinWidth(1050);
            primaryStage.setMinHeight(700);
        } catch (IOException e) {
            throw new RuntimeException("Could not load view: " + fxml, e);
        }
    }

    public static void showLaunch() {
        setRoot("LaunchView", "Omel Ordering App");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
