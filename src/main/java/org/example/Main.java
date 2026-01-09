package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.ui.Router;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        ApplicationContext  context = ApplicationContext.getInstance();
        ApplicationControllerFactory factory = new ApplicationControllerFactory(context);

        Router.init(factory);

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/admin/admin-shell.fxml")
        );
        loader.setControllerFactory(factory);
        Parent root = loader.load();

        stage.setTitle("E-commerce App");
        stage.setScene(new Scene(root, 1000, 600));

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}