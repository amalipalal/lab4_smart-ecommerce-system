package org.example.controller.product;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dto.product.ProductResponse;
import org.example.service.CategoryService;
import org.example.service.ProductService;
import org.example.util.DialogUtil;
import org.example.util.FormatUtil;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.UUID;

public class AdminProductController {

    @FXML
    private TableView<ProductResponse> productTable;
    @FXML
    private TableColumn<ProductResponse, String> idColumn;
    @FXML
    private TableColumn<ProductResponse, String> nameColumn;
    @FXML
    private TableColumn<ProductResponse, String> descColumn;
    @FXML
    private TableColumn<ProductResponse, String> priceColumn;
    @FXML
    private TableColumn<ProductResponse, String> stockColumn;
    @FXML
    private TableColumn<ProductResponse, String> categoryColumn;
    @FXML
    private TableColumn<ProductResponse, String> createdAtColumn;
    @FXML
    private TableColumn<ProductResponse, Void> actionsColumn;

    @FXML
    private Button addProductBtn;
    @FXML
    private TextField searchField;

    @FXML
    private Pagination pagination;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ObservableList<ProductResponse> products = FXCollections.observableArrayList();

    private final int PAGE_SIZE = 5;
    private String currentSearchQuery = "";

    public AdminProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @FXML
    protected void initialize() {
        setupColumns();
        setupActionsColumn();
        setupPagination();
    }

    private void setupColumns() {
        idColumn.setCellValueFactory(c ->
                new SimpleStringProperty(FormatUtil.shortId(c.getValue().productId())));

        nameColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().name()));

        descColumn.setCellValueFactory(c ->
                new SimpleStringProperty(FormatUtil.truncate(c.getValue().description(), 50)));

        priceColumn.setCellValueFactory(c ->
                new SimpleStringProperty(FormatUtil.currency(c.getValue().price())));

        stockColumn.setCellValueFactory(c ->
                new SimpleStringProperty(Integer.toString(c.getValue().stock())));

        categoryColumn.setCellValueFactory(c ->
                new SimpleStringProperty(getCategoryName(c.getValue().categoryId())));

        createdAtColumn.setCellValueFactory(c ->
                new SimpleStringProperty(FormatUtil.format(c.getValue().updatedAt())));
    }

    private String getCategoryName(UUID categoryId) {
        return this.categoryService.getCategory(categoryId).name();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("");
            private final HBox container = new HBox(5, editBtn);

            {
                FontIcon icon = new FontIcon("fas-edit");
                icon.setIconSize(14);
                editBtn.setGraphic(icon);
                editBtn.getStyleClass().add("icon-btn");
                editBtn.setOnAction( e -> {
                    handleUpdateProduct(getTableView().getItems().get(getIndex()));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    protected  void handleUpdateProduct(ProductResponse product) {
        openProductModal("Update product", product);
    }

    private void handleDeleteProduct(ProductResponse product) {
        boolean confirmed = DialogUtil.showConfirm(
                "Delete Product",
                "Are you sure you want to delete \"" + product.name() + "\"?"
        );

        if (!confirmed) return;

        try {
            productService.deleteProduct(product.productId());
            refreshPagination();
        } catch (Exception e) {
            DialogUtil.showError("Error", "Failed to delete product");
        }
    }

    private void setupPagination() {
        int totalItems = this.currentSearchQuery.isBlank()
                ? productService.getProductCount()
                : productService.countProductsByName(currentSearchQuery);

        int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

        pagination.setPageCount(Math.max(totalPages, 1));
        pagination.setCurrentPageIndex(0);

        loadProducts(PAGE_SIZE, 0);

        // Listen for page changes
        pagination.currentPageIndexProperty().addListener(
                (obs, oldIndex, newIndex) -> {
                    loadProducts(PAGE_SIZE, newIndex.intValue() * PAGE_SIZE);
                }
        );
    }

    private void loadProducts(int limit, int offset) {
        try {
            products.clear();

            List<ProductResponse> result =
                    currentSearchQuery.isBlank()
                            ? productService.getAllProducts(limit, offset)
                            : productService.searchProducts(currentSearchQuery, limit, offset);

            products.addAll(result);
            productTable.setItems(products);
        } catch (Exception e) {
            DialogUtil.showError("Error", "Failed to load products");
        }
    }

    @FXML
    protected void handleAddProduct() {
        openProductModal("Add Product", null);
    }

    private void openProductModal(String title, ProductResponse product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin-product-modal.fxml"));

            ProductModalController controller = new ProductModalController(this.productService, this.categoryService);

            loader.setController(controller);

            Stage modal = new Stage();
            modal.setTitle(title);
            modal.setScene(new Scene(loader.load()));
            modal.initModality(Modality.APPLICATION_MODAL);

            if(product != null)
                controller.setProduct(product);

            modal.showAndWait();
            refreshPagination();
        } catch (Exception e) {
            DialogUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    protected  void handleSearchAction() {
        currentSearchQuery = searchField.getText().trim();
        pagination.setCurrentPageIndex(0);
        setupPagination();
    }

    private void refreshPagination() {
        setupPagination();
    }

}
