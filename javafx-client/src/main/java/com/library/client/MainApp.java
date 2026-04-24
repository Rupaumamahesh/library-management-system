package com.library.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main entry point for the Library Management System JavaFX desktop application.
 * Extends Application and loads the login screen on startup.
 * Provides a static switchScene() method for all controllers to navigate between screens.
 */
public class MainApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Library Management System");
        primaryStage.setWidth(900);
        primaryStage.setHeight(700);

        // Load and display login screen
        switchScene("/fxml/login.fxml");
        primaryStage.show();

        logger.info("Application started");
    }

    /**
     * Static helper method for switching scenes.
     * Called by controllers to navigate between screens.
     *
     * @param fxmlPath Path to FXML file (e.g., "/fxml/dashboard.fxml")
     * @throws IOException if FXML file not found
     */
    public static void switchScene(String fxmlPath) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
        Scene scene = new Scene(fxmlLoader.load());
        
        // Load CSS stylesheet if available
        String css = MainApp.class.getResource("/css/styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        
        primaryStage.setScene(scene);
    }

    /**
     * Gets the primary stage.
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
