package org.example.controller.order;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dto.order.CustomerDetails;
import org.example.dto.order.OrderRequest;
import org.example.dto.product.ProductResponse;
import org.example.service.OrderService;
import org.example.util.ui.DialogUtil;


public class OrderModalController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;

    @FXML
    private TextField quantityField;
    @FXML
    private TextField shippingCountryField;
    @FXML
    private TextField shippingCityField;
    @FXML
    private TextField shippingCodeField;
    @FXML
    private TextField postalCodeField;

    @FXML
    private Button placeOrderBtn;

    private final OrderService orderService;
    private ProductResponse product;

    public OrderModalController(OrderService orderService) {
        this.orderService = orderService;
    }

    public void setProduct(ProductResponse product) {
        this.product = product;
    }

    @FXML
    protected void handlePlaceOrder() {
        try {
            if (!validateInputs()) return;

            OrderRequest orderRequest = buildOrderRequest();
            CustomerDetails customerDetails = buildCustomerDetails();

            orderService.placeOrder(orderRequest, customerDetails);

            DialogUtil.showInfo("Success", "Order placed successfully");
            close();
        } catch (Exception e) {
            DialogUtil.showError("Order Failed", e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (product == null) {
            DialogUtil.showError("Error", "No product selected");
            return false;
        }
        if (firstNameField.getText().isBlank()
                || lastNameField.getText().isBlank()
                || emailField.getText().isBlank()
                || quantityField.getText().isBlank()) {

            DialogUtil.showError("Validation", "Please fill all required fields");
            return false;
        }
        return true;
    }

    private OrderRequest buildOrderRequest() {
        int quantity = Integer.parseInt(quantityField.getText());

        return new OrderRequest(
                product.productId(),
                quantity,
                shippingCountryField.getText(),
                shippingCityField.getText(),
                shippingCodeField.getText(),
                postalCodeField.getText()
        );
    }

    private CustomerDetails buildCustomerDetails() {
        return new CustomerDetails(
                firstNameField.getText(),
                lastNameField.getText(),
                emailField.getText(),
                phoneField.getText()
        );
    }

    private void close() {
        ((Stage) placeOrderBtn.getScene().getWindow()).close();
    }
}
