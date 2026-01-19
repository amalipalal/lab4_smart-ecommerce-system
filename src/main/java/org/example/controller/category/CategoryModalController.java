package org.example.controller.category;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.dto.category.CreateCategoryRequest;
import org.example.dto.category.CategoryResponse;
import org.example.dto.category.UpdateCategoryRequest;
import org.example.service.CategoryService;
import org.example.util.DialogUtil;

public class CategoryModalController {
    @FXML
    public TextField nameField;
    @FXML
    public TextArea descField;
    @FXML
    public Button saveBtn;

    private final CategoryService categoryService;
    private CategoryResponse categoryToUpdate;

    public CategoryModalController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void setCategory(CategoryResponse category) {
        this.categoryToUpdate = category;

        nameField.setText(category.name());
        descField.setText(category.description());
        saveBtn.setText("Update Category");
    }

    @FXML
    protected void handleSave() {
        try {
            String name = nameField.getText();
            String desc = descField.getText();
            if(name.isBlank()) {
                showError("Validation Error", "Category name is required");
                return;
            }

            if(categoryToUpdate == null) {
                CreateCategoryRequest category = new CreateCategoryRequest(name, desc);
                categoryService.createCategory(category);
            } else {
                UpdateCategoryRequest request = new UpdateCategoryRequest(
                        categoryToUpdate.categoryId(),
                        name,
                        desc
                );
                categoryService.updateCategory(request);
            }

            closeStage();
        } catch (Exception e) {
            showError("Failed to add category", e.getMessage());
        }
    }

    private void closeStage() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        DialogUtil.showError(title, message);
    }
}
