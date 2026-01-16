package org.example.controller.product;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dto.product.ProductResponse;
import org.example.model.ProductFilter;
import org.example.service.CategoryService;
import org.example.service.ProductService;
import org.example.ui.ActionCell;
import org.example.ui.ActionDefinition;
import org.example.util.DialogUtil;
import org.example.util.FormatUtil;

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
        actionsColumn.setCellFactory(col -> new ActionCell<>(
                List.of(
                        new ActionDefinition<>(
                                "fas-edit",
                                14,
                                "icon-btn",
                                this::handleUpdateProduct),
                        new ActionDefinition<>(
                                "fas-trash",
                                14,
                                "icon-btn danger",
                                this::handleDeleteProduct)
                )
        ));
    }

    private void handleUpdateProduct(ProductResponse product) {
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
        ProductFilter filter = buildFilter();

        int totalItems = this.productService.countProductsByFilter(filter);
        int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

        pagination.setPageCount(Math.max(totalPages, 1));
        pagination.setCurrentPageIndex(0);

        loadProducts(filter, 0);

        // Listen for page changes
        pagination.currentPageIndexProperty().addListener(
                (obs, oldIndex, newIndex) -> {
                    loadProducts(filter, newIndex.intValue() * PAGE_SIZE);
                }
        );
    }

    private ProductFilter buildFilter() {
        String search = searchField.getText().trim();

        return new ProductFilter(
                search.isBlank() ? null : search,
                null
        );
    }

    private void loadProducts(ProductFilter filter, int offset) {
        try {
            products.clear();

            List<ProductResponse> result = this.productService.searchProducts(filter, PAGE_SIZE, offset);

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/admin-product-modal.fxml"));

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
        pagination.setCurrentPageIndex(0);
        setupPagination();
    }

    @FXML
    protected void handleRefresh(){
        setupPagination();
    }

    private void refreshPagination() {
        setupPagination();
    }

}
