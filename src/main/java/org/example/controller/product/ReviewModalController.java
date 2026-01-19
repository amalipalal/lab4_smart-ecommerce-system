package org.example.controller.product;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dto.review.CreateReviewRequest;
import org.example.dto.review.ReviewResponse;
import org.example.service.ReviewService;
import org.example.util.DialogUtil;
import org.example.util.FormatUtil;

import java.util.List;
import java.util.UUID;

public class ReviewModalController {
    @FXML private TableView<ReviewResponse> reviewTable;
    @FXML private TableColumn<ReviewResponse, String> ratingColumn;
    @FXML private TableColumn<ReviewResponse, String> commentColumn;
    @FXML private TableColumn<ReviewResponse, String> dateColumn;

    @FXML private TextField emailField;
    @FXML private Spinner<Integer> ratingSpinner;
    @FXML private TextArea commentArea;
    @FXML private Pagination pagination;

    private final ReviewService reviewService;
    private final ObservableList<ReviewResponse> reviews = FXCollections.observableArrayList();
    private UUID productId;
    private final int PAGE_SIZE = 5;

    public ReviewModalController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @FXML
    protected void initialize() {
        setupColumns();
        setupRatingSpinner();
    }

    @FXML
    public void setProduct(UUID productId) {
        this.productId = productId;
        setupPagination();
    }

    private void setupColumns() {
        ratingColumn.setCellValueFactory(c ->
            new SimpleStringProperty("★".repeat(c.getValue().rating())));

        commentColumn.setCellValueFactory(c ->
            new SimpleStringProperty(FormatUtil.truncate(c.getValue().comment(), 50)));

        dateColumn.setCellValueFactory(c ->
            new SimpleStringProperty(FormatUtil.format(c.getValue().createdAt())));
    }

    private void setupRatingSpinner() {
        SpinnerValueFactory<Integer> valueFactory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 5);
        ratingSpinner.setValueFactory(valueFactory);
    }

    private void setupPagination() {
        int totalItems = reviewService.countProductReviews(productId);
        int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

        pagination.setPageCount(Math.max(totalPages, 1));
        pagination.setCurrentPageIndex(0);

        loadReviews(0);

        pagination.currentPageIndexProperty().addListener(
            (obs, oldIndex, newIndex) -> loadReviews(newIndex.intValue() * PAGE_SIZE)
        );
    }

    private void loadReviews(int offset) {
        try {
            reviews.clear();
            List<ReviewResponse> result = reviewService.getProductReviews(productId, PAGE_SIZE, offset);
            reviews.addAll(result);
            reviewTable.setItems(reviews);
        } catch (Exception e) {
            DialogUtil.showError("Error", "Failed to load reviews");
        }
    }

    @FXML
    protected void handleSubmitReview() {
        String email = emailField.getText().trim();
        String comment = commentArea.getText().trim();
        int rating = ratingSpinner.getValue();

        validateEmailAndComment();

        try {
            CreateReviewRequest request = new CreateReviewRequest(productId, email, rating, comment);
            reviewService.createReview(request);

            clearForm();
            setupPagination();
            DialogUtil.showInfo("Success", "Review submitted successfully");
        } catch (IllegalArgumentException e) {
            DialogUtil.showError("Error", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            DialogUtil.showError("Error", "Failed to submit review");
        }
    }

    private void clearForm() {
        emailField.clear();
        commentArea.clear();
        ratingSpinner.getValueFactory().setValue(5);
    }

    private void validateEmailAndComment() {
        String email = emailField.getText().trim();
        String comment = commentArea.getText().trim();
        if (email.isEmpty() || comment.isEmpty()) {
            DialogUtil.showError("Validation Error", "Email and comment are required");
        }
    }

    @FXML
    protected void handleClose() {
        ((Stage) emailField.getScene().getWindow()).close();
    }
}