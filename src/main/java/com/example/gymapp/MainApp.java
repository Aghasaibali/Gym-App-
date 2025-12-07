package com.example.gymapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.nio.file.Path;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/example/gymapp/views/login-view.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/gymapp/styles/styles.css").toExternalForm());
        primaryStage.setTitle("Gym Manager");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Keep the JavaFX native cache inside the project to avoid permission issues
        System.setProperty("javafx.cachedir",
                Path.of(System.getProperty("user.dir"), ".openjfx-cache").toString());
        launch(args);
    }
}
