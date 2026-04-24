package com.library.client.controller;

import com.library.client.MainApp;
import com.library.client.api.ApiClient;
import com.library.client.model.Book;
import com.library.client.model.Member;
import com.library.client.model.Transaction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the dashboard screen.
 * Displays summary statistics (total books, members, active borrows).
 * Shows recent transactions in a TableView.
 * Navigation sidebar provides links to all other screens.
 */
public class DashboardController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @FXML
    private Label totalBooksLabel;

    @FXML
    private Label totalMembersLabel;

    @FXML
    private Label activeBorrowsLabel;

    @FXML
    private TableView<Transaction> recentTransactionsTable;

    @FXML
    private TableColumn<Transaction, String> recordIdColumn;

    @FXML
    private TableColumn<Transaction, String> bookIdColumn;

    @FXML
    private TableColumn<Transaction, String> memberIdColumn;

    @FXML
    private TableColumn<Transaction, String> borrowDateColumn;

    @FXML
    private TableColumn<Transaction, String> statusColumn;

    @FXML
    private Button booksButton;

    @FXML
    private Button membersButton;

    @FXML
    private Button transactionsButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.debug("DashboardController initialized");
        setupTableColumns();
        loadDashboardData();
    }

    private void setupTableColumns() {
        recordIdColumn.setCellValueFactory(new PropertyValueFactory<>("recordId"));
        bookIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        memberIdColumn.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadDashboardData() {
        // Load books count
        ApiClient.getInstance().getBooks()
                .setOnSucceeded(event -> {
                    List<Book> books = (List<Book>) event.getSource().getValue();
                    Platform.runLater(() -> totalBooksLabel.setText(String.valueOf(books.size())));
                })
                .setOnFailed(event -> logger.error("Failed to load books", event.getSource().getException()));
        new Thread(ApiClient.getInstance().getBooks()).start();

        // Load members count
        ApiClient.getInstance().getMembers()
                .setOnSucceeded(event -> {
                    List<Member> members = (List<Member>) event.getSource().getValue();
                    Platform.runLater(() -> totalMembersLabel.setText(String.valueOf(members.size())));
                })
                .setOnFailed(event -> logger.error("Failed to load members", event.getSource().getException()));
        new Thread(ApiClient.getInstance().getMembers()).start();

        // Load transactions (recent)
        ApiClient.getInstance().getTransactions()
                .setOnSucceeded(event -> {
                    List<Transaction> transactions = (List<Transaction>) event.getSource().getValue();
                    long activeBorrows = transactions.stream()
                            .filter(t -> "BORROWED".equals(t.getStatus()))
                            .count();
                    Platform.runLater(() -> {
                        activeBorrowsLabel.setText(String.valueOf(activeBorrows));
                        recentTransactionsTable.getItems().addAll(transactions);
                    });
                })
                .setOnFailed(event -> logger.error("Failed to load transactions", event.getSource().getException()));
        new Thread(ApiClient.getInstance().getTransactions()).start();
    }

    @FXML
    private void navigateToBooks() throws IOException {
        MainApp.switchScene("/fxml/books.fxml");
    }

    @FXML
    private void navigateToMembers() throws IOException {
        MainApp.switchScene("/fxml/members.fxml");
    }

    @FXML
    private void navigateToTransactions() throws IOException {
        MainApp.switchScene("/fxml/transactions.fxml");
    }
}
