package org.example.controller.order;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.example.dto.order.OrderResponse;
import org.example.service.PurchaseService;
import org.example.util.DialogUtil;
import org.example.util.FormatUtil;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminOrderController implements Initializable {

    public TableView<OrderResponse> orderTable;
    public TableColumn<OrderResponse, String> idColumn;
    public TableColumn<OrderResponse, String> customerColumn;
    public TableColumn<OrderResponse, String> totalColumn;
    public TableColumn<OrderResponse, String> dateColumn;
    public TableColumn<OrderResponse, String> shippingColumn;
    public Pagination pagination;

    private final PurchaseService purchaseService;
    private final ObservableList<OrderResponse> orders = FXCollections.observableArrayList();

    private static final int PAGE_SIZE = 5;

    public AdminOrderController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        setupPagination();
    }

    private void setupColumns() {
        idColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(FormatUtil.shortId(cellData.getValue().orderId())));
        customerColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().customerEmail()));
        totalColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(FormatUtil.currency(cellData.getValue().totalAmount())));
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(FormatUtil.format(cellData.getValue().orderDate())));
        shippingColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().shippingCountry() + " / " + cellData.getValue().shippingCity()));
        shippingColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
            }
        });
    }

    private void setupPagination() {
        int totalItems = Math.max(1, purchaseService.countPurchases());
        int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

        pagination.setPageCount(Math.max(totalPages, 1));
        pagination.setCurrentPageIndex(0);

        loadOrders(0);

        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) ->
                loadOrders(newIndex.intValue() * PAGE_SIZE));
    }

    private void loadOrders(int offset) {
        try {
            orders.clear();
            List<OrderResponse> result = purchaseService.getPurchaseHistory(PAGE_SIZE, offset);
            orders.addAll(result);
            orderTable.setItems(orders);
        } catch (Exception e) {
            DialogUtil.showError("Failed to load orders", e.getMessage());
        }
    }
}
