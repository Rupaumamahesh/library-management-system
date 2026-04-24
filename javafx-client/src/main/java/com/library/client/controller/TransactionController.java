package com.library.client.controller;

import com.library.client.MainApp;
import com.library.client.api.ApiClient;
import com.library.client.model.Transaction;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the transactions screen.
 * Allows borrowing and returning books.
 * Displays transaction history with filtering.
 */
public class TransactionController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @FXML
    private TextField memberIdField;

    @FXML
    private TextField bookIdField;

    @FXML
    private Button borrowButton;

    @FXML
    private TableView<Transaction> historyTable;

    @FXML
    private TableColumn<Transaction, String> recordIdColumn;

    @FXML
    private TableColumn<Transaction, String> bookIdColumn;

    @FXML
    private TableColumn<Transaction, String> memberIdColumn;

    @FXML
    private TableColumn<Transaction, String> borrowDateColumn;

    @FXML
    private TableColumn<Transaction, String> returnDateColumn;

    @FXML
    private TableColumn<Transaction, String> statusColumn;

    @FXML
    private ComboBox<String> statusFilterCombo;

    @FXML
    private Button returnButton;

    @FXML
    private Button backButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.debug("TransactionController initialized");
        setupTableColumns();
        statusFilterCombo.setItems(FXCollections.observableArrayList("All", "BORROWED", "RETURNED"));
        statusFilterCombo.setValue("All");
        loadTransactions();
    }

    private void setupTableColumns() {
        recordIdColumn.setCellValueFactory(new PropertyValueFactory<>("recordId"));
        bookIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        memberIdColumn.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadTransactions() {
        ApiClient.getInstance().getTransactions()
                .setOnSucceeded(event -> {
                    List<Transaction> transactions = (List<Transaction>) event.getSource().getValue();
                    ObservableList<Transaction> items = FXCollections.observableArrayList(transactions);
                    Platform.runLater(() -> historyTable.setItems(items));
                    logger.info("Loaded {} transactions", transactions.size());
                })
                .setOnFailed(event -> {
                    logger.error("Failed to load transactions", event.getSource().getException());
                    showAlert("Error", "Failed to load transactions", Alert.AlertType.ERROR);
                });
        new Thread(ApiClient.getInstance().getTransactions()).start();
    }

    @FXML
    private void handleBorrow() {
        String memberId = memberIdField.getText().trim();
        String bookId = bookIdField.getText().trim();

        if (memberId.isEmpty() || bookId.isEmpty()) {
            showAlert("Error", "Please enter member ID and book ID", Alert.AlertType.WARNING);
            return;
        }

        borrowButton.setDisable(true);

        ApiClient.getInstance().borrowBook(memberId, bookId)
                .setOnSucceeded(event -> {
                    Transaction transaction = (Transaction) event.getSource().getValue();
                    Platform.runLater(() -> {
                        showAlert("Success", "Book borrowed successfully", Alert.AlertType.INFORMATION);
                        memberIdField.clear();
                        bookIdField.clear();
                        loadTransactions();
                        borrowButton.setDisable(false);
                    });
                    logger.info("Book borrowed: {}", transaction.getRecordId());
                })
                .setOnFailed(event -> {
                    logger.error("Failed to borrow book", event.getSource().getException());
                    Platform.runLater(() -> {
                        showAlert("Error", "Failed to borrow book: " + event.getSource().getException().getMessage(), Alert.AlertType.ERROR);
                        borrowButton.setDisable(false);
                    });
                });
        new Thread(ApiClient.getInstance().borrowBook(memberId, bookId)).start();
    }

    @FXML
    private void handleReturn() {
        Transaction selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a transaction to return", Alert.AlertType.WARNING);
            return;
        }

        if (!"BORROWED".equals(selected.getStatus())) {
            showAlert("Error", "Only BORROWED books can be returned", Alert.AlertType.WARNING);
            return;
        }

        returnButton.setDisable(true);

        ApiClient.getInstance().returnBook(selected.getId())
                .setOnSucceeded(event -> {
                    Platform.runLater(() -> {
                        showAlert("Success", "Book returned successfully", Alert.AlertType.INFORMATION);
                        loadTransactions();
                        returnButton.setDisable(false);
                    });
                    logger.info("Book returned: {}", selected.getRecordId());
                })
                .setOnFailed(event -> {
                    logger.error("Failed to return book", event.getSource().getException());
                    Platform.runLater(() -> {
                        showAlert("Error", "Failed to return book: " + event.getSource().getException().getMessage(), Alert.AlertType.ERROR);
                        returnButton.setDisable(false);
                    });
                });
        new Thread(ApiClient.getInstance().returnBook(selected.getId())).start();
    }

    @FXML
    private void handleFilterChange() {
        // Filter logic can be added here
        logger.debug("Filter changed to: {}", statusFilterCombo.getValue());
    }

    @FXML
    private void handleBack() throws IOException {
        MainApp.switchScene("/fxml/dashboard.fxml");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
