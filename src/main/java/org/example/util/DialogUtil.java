package org.example.util;

import javafx.scene.control.Alert;

public class DialogUtil {

    private DialogUtil() {}

    public static void showError(String title, String message) {
        Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
