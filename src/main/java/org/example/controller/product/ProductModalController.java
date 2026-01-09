package org.example.controller.product;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dto.category.CategoryResponse;
import org.example.dto.product.CreateProductRequest;
import org.example.dto.product.ProductResponse;
import org.example.service.CategoryService;
import org.example.service.ProductService;
import org.example.util.DialogUtil;
import org.example.util.FormatUtil;

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
            CategoryResponse selectedCategory = categoryBox.getValue();

            if(selectedCategory == null) {
                DialogUtil.showError("Validation", "Category is required");
                return;
            }

            String name = nameField.getText();
            int stock = Integer.parseInt(stockField.getText());
            double price = Double.parseDouble(priceField.getText());
            String desc = descField.getText();

            if(name.isBlank()) {
                DialogUtil.showError("Validation", "Product name is required");
                return;
            }

            if (productToUpdate == null) {
                productService.createProduct(
                        new CreateProductRequest(
                                name,
                                desc,
                                price,
                                stock,
                                selectedCategory.categoryId()
                        )
                );
            }

            close();
        } catch (Exception e) {
            DialogUtil.showError("Error", e.getMessage());
        }
    }

    private void close() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}
