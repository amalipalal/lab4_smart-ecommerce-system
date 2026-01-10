package org.example.controller;

import javafx.fxml.FXML;
import org.example.ui.Router;

public class BuyerViewController {

    @FXML
    private void goAdminView() {
        Router.goToAdminShell();
    }
}
