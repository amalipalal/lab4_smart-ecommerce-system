package org.example.controller.shell;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import org.example.ui.Router;

public class AdminShellController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        contentArea.getChildren().setAll(
                Router.loadAdminContent("admin-product.fxml")
        );
    }

    @FXML
    private void goProducts() {
        contentArea.getChildren().setAll(
                Router.loadAdminContent("admin-product.fxml")
        );
    }

    @FXML
    private void goCategories() {
        contentArea.getChildren().setAll(
                Router.loadAdminContent("admin-category.fxml")
        );
    }

    @FXML
    private void goBuyer() {
        Router.goToBuyer();
    }
}