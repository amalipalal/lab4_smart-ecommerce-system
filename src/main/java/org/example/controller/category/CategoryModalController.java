package org.example.controller.category;

import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.dto.category.CreateCategoryRequest;
import org.example.service.CategoryService;

public class CategoryModalController {
    public TextField nameField;
    public TextArea descField;
    public Button saveBtn;

    private final CategoryService categoryService;

    public CategoryModalController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void handleSave() {
        try {
            String name = nameField.getText();
            String desc = descField.getText();

            if(name.isBlank()) return;

            CreateCategoryRequest category = new CreateCategoryRequest(name, desc);
            categoryService.createCategory(category);
            // To ensure the same stage that was initialized is closed
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to add category", e.getMessage());
        }
    }

    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
