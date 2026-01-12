package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.ui.Router;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        ApplicationContext  context = ApplicationContext.getInstance();
        ApplicationControllerFactory factory = new ApplicationControllerFactory(context);

        Router.init(stage, factory);
        Router.goToBuyer();

        stage.setTitle("E-commerce App");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}