package org.example.controller.product;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dto.category.CategoryResponse;
import org.example.dto.product.CreateProductRequest;
import org.example.dto.product.ProductResponse;
import org.example.dto.product.UpdateProductRequest;
import org.example.service.CategoryService;
import org.example.service.ProductService;
import org.example.util.DialogUtil;
import org.example.util.FormatUtil;

import java.util.UUID;

public class ProductModalController {

    public ComboBox<CategoryResponse> categoryBox;
    public TextField nameField;
    public TextField stockField;
    public TextField priceField;
    public TextArea descField;
    public Button saveBtn;

    private final ProductService productService;
    private final CategoryService categoryService;
    private ProductResponse productToUpdate;

    public ProductModalController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    public void initialize() {
        loadCategories();
    }

    private void loadCategories() {
        try {
            categoryBox.setItems(
                    FXCollections.observableList(
                            categoryService.getAllCategories(20, 0)
                    )
            );

            categoryBox.setCellFactory(cb -> new ListCell<>() {
                @Override
                protected  void updateItem(CategoryResponse item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.name());
                }
            });

            categoryBox.setButtonCell(categoryBox.getCellFactory().call(null));

        } catch (Exception e) {
            DialogUtil.showError("Error", "Failed to load categories");
        }
    }

    public void setProduct(ProductResponse product) {
        this.productToUpdate = product;

        nameField.setText(product.name());
        priceField.setText(FormatUtil.currency(product.price()));
        stockField.setText(Integer.toString(product.stock()));
        descField.setText(product.description());

        // Preselect the category of the product being updated
        categoryBox.getItems().stream()
                .filter(c -> c.categoryId().equals(product.categoryId()))
                .findFirst()
                .ifPresent(categoryBox::setValue);

        saveBtn.setText("Update product");
    }

    public void handleSave() {
        try {
            if(!validateInputs()) return;

            var request = buildRequest();

            if(productToUpdate == null)
                productService.createProduct((CreateProductRequest) request);
            else
                productService.updateProduct(productToUpdate.productId(), (UpdateProductRequest) request);

            close();
        } catch (Exception e) {
            DialogUtil.showError("Error", e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (categoryBox.getValue() == null) {
            DialogUtil.showError("Validation", "Category is required");
            return false;
        }
        if (nameField.getText().isBlank()) {
            DialogUtil.showError("Validation", "Product name is required");
            return false;
        }
        return true;
    }

    private Object buildRequest() {
        String name = nameField.getText();
        int stock = Integer.parseInt(stockField.getText());
        double price = FormatUtil.currency(priceField.getText());
        String desc = descField.getText();
        UUID categoryId = categoryBox.getValue().categoryId();

        if (productToUpdate == null) {
            return new CreateProductRequest(name, desc, price, stock, categoryId);
        } else {
            return new UpdateProductRequest(name, desc, price, categoryId, stock);
        }
    }

    private void close() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}
