package org.example.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.application.ApplicationControllerFactory;

public class Router {

    private static Stage primaryStage;
    private static ApplicationControllerFactory controllerFactory;

    private Router() {}

    public static void init(Stage stage, ApplicationControllerFactory factory) {
        controllerFactory = factory;
        primaryStage = stage;
    }

    public static Parent loadAdminContent(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Router.class.getResource("/fxml/admin/" + fxml)
            );
            loader.setControllerFactory(controllerFactory);
            return loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load admin content: " + fxml, e);
        }
    }

    public static void goToAdminShell() {
        loadScene("/fxml/admin/admin-shell.fxml");
    }

    public static void goToBuyer() {
        loadScene("/fxml/buyer/buyer-home.fxml");
    }

    private static void loadScene(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(Router.class.getResource(path));
            loader.setControllerFactory(controllerFactory);

            Parent root = loader.load();
            primaryStage.setScene(new Scene(root, 1000, 600));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load scene: " + path, e);
        }
    }
}
