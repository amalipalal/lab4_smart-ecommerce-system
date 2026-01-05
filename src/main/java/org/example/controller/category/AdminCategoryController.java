package org.example.controller.category;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dto.category.CreateCategoryResponse;
import org.example.service.CategoryService;
import org.example.util.FormatUtil;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;

public class AdminCategoryController {

    public TableView<CreateCategoryResponse> categoryTable;
    public TableColumn<CreateCategoryResponse, String> idColumn;
    public TableColumn<CreateCategoryResponse, String> nameColumn;
    public TableColumn<CreateCategoryResponse, String> descColumn;
    public TableColumn<CreateCategoryResponse, String> createdAtColumn;
    public TableColumn<CreateCategoryResponse, Void> actionsColumn;
    public Button addCategoryBtn;

    private final CategoryService categoryService;

    private final ObservableList<CreateCategoryResponse> categories = FXCollections.observableArrayList();

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public void initialize() {
        idColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(FormatUtil.shortId(cellData.getValue().categoryId())));
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().name()));
        descColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(FormatUtil.truncate(cellData.getValue().description(), 50)));

        createdAtColumn.setCellValueFactory(c ->
                new SimpleStringProperty(FormatUtil.format(c.getValue().createdAt()))
        );

        setupActionsColumn();
        loadCategories(20, 0);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button updateBtn = new Button("");
            private final Button deleteBtn = new Button("");
            private final HBox container = new HBox(5, updateBtn, deleteBtn);

            {
                FontIcon editIcon = new FontIcon("fas-edit");
                editIcon.setIconSize(14);
                updateBtn.setGraphic(editIcon);
                updateBtn.getStyleClass().add("icon-btn");

                FontIcon deleteIcon = new FontIcon("fas-trash");
                deleteIcon.setIconSize(14);
                deleteBtn.setGraphic(deleteIcon);
                deleteBtn.getStyleClass().add("icon-btn danger");

                updateBtn.setOnAction(e -> {
                    CreateCategoryResponse category =
                            getTableView().getItems().get(getIndex());
                    // TODO: open update modal
                });

                deleteBtn.setOnAction(e -> {
                    CreateCategoryResponse category =
                            getTableView().getItems().get(getIndex());
                    // TODO: delete confirmation + service call
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    public void handleAddCategory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/category-modal.fxml"));
            loader.setControllerFactory(cls -> new CategoryModalController(categoryService));
            Stage modal = new Stage();
            modal.setTitle("Add Category");
            modal.setScene(new Scene(loader.load()));
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.showAndWait();

            loadCategories(20, 0);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to add category", "Could not add this category.\nTry again later.");
        }
    }

    private void loadCategories(int limit, int offset) {
        try {
            categories.clear();
            List<CreateCategoryResponse> all = categoryService.getAllCategories(limit, offset);
            categories.addAll(all);
            categoryTable.setItems(categories);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load categories", "Could not fetcch categories form te database.");
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
