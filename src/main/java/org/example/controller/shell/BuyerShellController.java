package org.example.controller.shell;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.controller.order.OrderModalController;
import org.example.dto.category.CategoryResponse;
import org.example.dto.product.ProductResponse;
import org.example.model.ProductFilter;
import org.example.service.CategoryService;
import org.example.service.OrderService;
import org.example.service.ProductService;
import org.example.ui.ActionCell;
import org.example.ui.ActionDefinition;
import org.example.ui.Router;
import org.example.util.ui.DialogUtil;
import org.example.util.ui.FormatUtil;

import java.util.List;

public class BuyerShellController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<CategoryResponse> categoryFilter;
    @FXML
    private TableView<ProductResponse> productTable;
    @FXML
    private TableColumn<ProductResponse, String> nameColumn;
    @FXML
    private TableColumn<ProductResponse, String> descColumn;
    @FXML
    private TableColumn<ProductResponse, String> priceColumn;
    @FXML
    private TableColumn<ProductResponse, Void> actionsColumn;

    @FXML
    private Pagination pagination;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final ObservableList<ProductResponse> products = FXCollections.observableArrayList();
    private final ObservableList<CategoryResponse> categories = FXCollections.observableArrayList();

    private final int PAGE_SIZE = 5;

    public BuyerShellController(
            ProductService productService,
            CategoryService categoryService,
            OrderService orderService
    ) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.orderService = orderService;
    }

    @FXML
    private void initialize() {
        setupColumns();
        setupActionsColumn();
        setupCategoryFilter();
        setupPagination();
    }

    @FXML
    private void goAdminView() {
        Router.goToAdminShell();
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().name()));

        descColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().description()));

        priceColumn.setCellValueFactory(c ->
                new SimpleStringProperty(FormatUtil.currency(c.getValue().price())));
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new ActionCell<>(
                List.of(
                        new ActionDefinition<>(
                                "fas-shopping-cart",
                                14,
                                "icon-btn",
                                this::openOrderModal
                        ),
                        new ActionDefinition<>(
                                "fas-comments",
                                14,
                                "icon-btn",
                                null
                        )
                )
        ));
    }

    private void setupCategoryFilter() {
        try {
            categories.add(null);
            categories.addAll(categoryService.getAllCategories(100,0));

            categoryFilter.setItems(categories);

            setCategoryCellFactory();

            categoryFilter.setButtonCell(categoryFilter.getCellFactory().call(null));

            setCategoryListener();
        } catch (Exception e) {
            DialogUtil.showError("Error", "Failed to load categories");
        }
    }

    private void setCategoryCellFactory() {
        categoryFilter.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(CategoryResponse item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty
                        ? ""
                        : item == null ? "All Categories" : item.name());
            }
        });
    }

    private void setCategoryListener() {
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            pagination.setCurrentPageIndex(0);
            setupPagination();
        });
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
        CategoryResponse selectedCategory = categoryFilter.getValue();

        return new ProductFilter(
                search.isBlank() ? null : search,
                selectedCategory == null ? null : selectedCategory.categoryId()
        );
    }

    private void loadProducts(ProductFilter filter, int offset) {
        try{
            products.clear();

            List<ProductResponse> result = this.productService.searchProducts(filter, PAGE_SIZE, offset);

            products.addAll(result);
            productTable.setItems(products);
        } catch (Exception e) {
            DialogUtil.showError("Error", "Failed to load products");
        }
    }

    @FXML
    private void handleSearchAction() {
        pagination.setCurrentPageIndex(0);
        setupPagination();
    }

    public void openOrderModal(ProductResponse product) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/buyer/order-modal.fxml")
            );
            OrderModalController orderModalController = new OrderModalController(this.orderService);

            loader.setController(orderModalController);

            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Place Order");
            stage.initModality(Modality.APPLICATION_MODAL);

            OrderModalController controller = loader.getController();
            controller.setProduct(product);

            stage.showAndWait();
        } catch (Exception e) {
            DialogUtil.showError("Failed to initiate purchase", e.getMessage());
            e.printStackTrace();
        }
    }

}
