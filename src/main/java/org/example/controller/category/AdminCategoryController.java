package org.example.controller.category;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dto.category.CategoryResponse;
import org.example.service.CategoryService;
import org.example.util.ui.DialogUtil;
import org.example.util.ui.FormatUtil;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class AdminCategoryController {

    public TableView<CategoryResponse> categoryTable;
    public TableColumn<CategoryResponse, String> idColumn;
    public TableColumn<CategoryResponse, String> nameColumn;
    public TableColumn<CategoryResponse, String> descColumn;
    public TableColumn<CategoryResponse, String> createdAtColumn;
    public TableColumn<CategoryResponse, Void> actionsColumn;
    public Button addCategoryBtn;
    public Pagination pagination;
    public TextField searchField;

    private final CategoryService categoryService;

    private final ObservableList<CategoryResponse> categories = FXCollections.observableArrayList();

    private static final int PAGE_SIZE = 5;
    private String currentSearchQuery = "";

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void initialize() {
        setupColumns();
        setupActionsColumn();
        setupPagination();
    }

    private void setupColumns() {
        idColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(FormatUtil.shortId(cellData.getValue().categoryId())));
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().name()));
        descColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(FormatUtil.truncate(cellData.getValue().description(), 50)));

        createdAtColumn.setCellValueFactory(c ->
                new SimpleStringProperty(FormatUtil.format(c.getValue().createdAt()))
        );
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button updateBtn = new Button("");
            private final HBox container = new HBox(5, updateBtn);

            {
                FontIcon editIcon = new FontIcon("fas-edit");
                editIcon.setIconSize(14);
                updateBtn.setGraphic(editIcon);
                updateBtn.getStyleClass().add("icon-btn");

                updateBtn.setOnAction(e -> {
                    CategoryResponse category =
                            getTableView().getItems().get(getIndex());

                    handleUpdateCategory(category);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void setupPagination() {
        int totalItems = this.currentSearchQuery.isBlank()
                ? categoryService.getCategoryCount()
                : categoryService.countCategoriesByName(currentSearchQuery);

        int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

        pagination.setPageCount(Math.max(totalPages, 1));
        pagination.setCurrentPageIndex(0);

        loadCategories(PAGE_SIZE, 0);

        // Listen for page changes
        pagination.currentPageIndexProperty().addListener(
                (obs, oldIndex, newIndex) -> {
                    loadCategories(PAGE_SIZE, newIndex.intValue() * PAGE_SIZE);
                }
        );
    }

    private void loadCategories(int limit, int offset) {
        try {
            categories.clear();

            List<CategoryResponse> result =
                    currentSearchQuery.isBlank()
                            ? categoryService.getAllCategories(limit, offset)
                            : categoryService.getCategory(currentSearchQuery, limit, offset);

            categories.addAll(result);
            categoryTable.setItems(categories);
        } catch (Exception e) {
            showError("Failed to load categories", "Could not fetcch categories form te database.");
        }
    }

    private void showError(String title, String message) {
        DialogUtil.showError(title, message);
    }

    public void handleSearchAction() {
        currentSearchQuery = searchField.getText().trim();
        pagination.setCurrentPageIndex(0);
        setupPagination();
    }

    public void handleAddCategory() {
        openCategoryModal("Add Category", null);
    }

    public void handleUpdateCategory(CategoryResponse category) {
        openCategoryModal("Update category", category);
    }

    private void openCategoryModal(String title, CategoryResponse category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/category-modal.fxml"));
            CategoryModalController controller = new CategoryModalController(categoryService);

            loader.setController(controller);

            Stage modal = new Stage();
            modal.setTitle(title);
            modal.setScene(new Scene(loader.load()));
            modal.initModality(Modality.APPLICATION_MODAL);

            if(category != null)
                controller.setCategory(category);

            modal.showAndWait();
            refreshPagination();
        } catch (Exception e) {
            showError("Failed to open modal", e.getMessage());
        }
    }

    private void refreshPagination() {
        setupPagination();
    }
}
