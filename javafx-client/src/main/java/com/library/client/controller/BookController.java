package com.library.client.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.library.client.MainApp;
import com.library.client.api.ApiClient;
import com.library.client.model.Book;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class BookController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, String> bookIdColumn;
    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, String> isbnColumn;
    @FXML private TableColumn<Book, String> statusColumn;
    @FXML private TextField searchField;
    @FXML private Button addButton;
    @FXML private Button deleteButton;
    @FXML private Button backButton;

    private ObservableList<Book> allBooks;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.debug("BookController initialized");
        setupTableColumns();
        loadBooks();
    }

    private void setupTableColumns() {
        bookIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        statusColumn.setCellValueFactory(cellData ->
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> cellData.getValue().isAvailable() ? "Available" : "Borrowed"
                )
        );
    }

    private void loadBooks() {
        // ✅ FIX: save task to variable, attach listeners, start SAME instance
        Task<List<Book>> task = ApiClient.getInstance().getBooks();

        task.setOnSucceeded(event -> {
            List<Book> books = (List<Book>) event.getSource().getValue();
            allBooks = FXCollections.observableArrayList(books);
            Platform.runLater(() -> booksTable.setItems(allBooks));
            logger.info("Loaded {} books", books.size());
        });

        task.setOnFailed(event -> {
            logger.error("Failed to load books", event.getSource().getException());
            showAlert("Error", "Failed to load books", Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        ObservableList<Book> filtered = FXCollections.observableArrayList(
                allBooks.stream()
                        .filter(b -> b.getTitle().toLowerCase().contains(query) ||
                                     b.getAuthor().toLowerCase().contains(query))
                        .toList()
        );
        booksTable.setItems(filtered);
    }

    @FXML
    private void handleAddBook() {
        showAlert("Add Book", "Add book feature not yet implemented", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleDeleteBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a book to delete", Alert.AlertType.WARNING);
            return;
        }

        // ✅ FIX: save task to variable, attach listeners, start SAME instance
        Task<Void> task = ApiClient.getInstance().deleteBook(selected.getId());

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                allBooks.remove(selected);
                booksTable.setItems(allBooks);
                showAlert("Success", "Book deleted successfully", Alert.AlertType.INFORMATION);
            });
            logger.info("Book deleted: {}", selected.getBookId());
        });

        task.setOnFailed(event -> {
            logger.error("Failed to delete book", event.getSource().getException());
            showAlert("Error", "Failed to delete book: " + event.getSource().getException().getMessage(), Alert.AlertType.ERROR);
        });

        new Thread(task).start();
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