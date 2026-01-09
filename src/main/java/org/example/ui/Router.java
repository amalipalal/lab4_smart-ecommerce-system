package org.example.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.example.ApplicationControllerFactory;

public class Router {

    private static ApplicationControllerFactory controllerFactory;

    private Router() {}

    public static void init(ApplicationControllerFactory factory) {
        controllerFactory = factory;
    }

    public static Parent goToAdmin(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Router.class.getResource("/fxml/admin/" + fxml)
            );
            loader.setControllerFactory(controllerFactory);
            return loader.load();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load admin page: " + fxml, e);
        }
    }

    public static Parent goToBuyer(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Router.class.getResource("/fxml/buyer/" + fxml)
            );
            loader.setControllerFactory(controllerFactory);
            return loader.load();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load buyer page: " + fxml, e);
        }
    }
}
