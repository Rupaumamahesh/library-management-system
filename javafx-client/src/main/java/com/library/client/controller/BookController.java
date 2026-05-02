package com.library.client.controller;

import com.library.client.MainApp;
import com.library.client.api.ApiClient;
import com.library.client.model.Book;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the books screen.
 * Displays books in a TableView with search filtering.
 * Add/Edit/Delete operations on books.
 */
public class BookController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @FXML
    private TableView<Book> booksTable;

    @FXML
    private TableColumn<Book, String> bookIdColumn;

    @FXML
    private TableColumn<Book, String> titleColumn;

    @FXML
    private TableColumn<Book, String> authorColumn;

    @FXML
    private TableColumn<Book, String> isbnColumn;

    @FXML
    private TableColumn<Book, String> statusColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button addButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button backButton;

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
        var task = ApiClient.getInstance().getBooks();
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
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Add New Book");
        dialog.setHeaderText("Enter book details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField bookIdField = new TextField();
        bookIdField.setPromptText("e.g. B001");
        TextField titleField = new TextField();
        titleField.setPromptText("Book title");
        TextField authorField = new TextField();
        authorField.setPromptText("Author name");
        TextField isbnField = new TextField();
        isbnField.setPromptText("ISBN (optional)");

        grid.add(new Label("Book ID:"), 0, 0);
        grid.add(bookIdField, 1, 0);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Author:"), 0, 2);
        grid.add(authorField, 1, 2);
        grid.add(new Label("ISBN:"), 0, 3);
        grid.add(isbnField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node addBtn = dialog.getDialogPane().lookupButton(addButtonType);
        addBtn.setDisable(true);
        bookIdField.textProperty().addListener((obs, old, val) ->
                addBtn.setDisable(val.trim().isEmpty() || titleField.getText().trim().isEmpty() || authorField.getText().trim().isEmpty()));
        titleField.textProperty().addListener((obs, old, val) ->
                addBtn.setDisable(val.trim().isEmpty() || bookIdField.getText().trim().isEmpty() || authorField.getText().trim().isEmpty()));
        authorField.textProperty().addListener((obs, old, val) ->
                addBtn.setDisable(val.trim().isEmpty() || bookIdField.getText().trim().isEmpty() || titleField.getText().trim().isEmpty()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Book book = new Book();
                book.setBookId(bookIdField.getText().trim());
                book.setTitle(titleField.getText().trim());
                book.setAuthor(authorField.getText().trim());
                book.setIsbn(isbnField.getText().trim());
                book.setAvailable(true);
                return book;
            }
            return null;
        });

        Optional<Book> result = dialog.showAndWait();
        result.ifPresent(book -> {
            var task = ApiClient.getInstance().addBook(book);
            task.setOnSucceeded(event -> {
                Book created = (Book) event.getSource().getValue();
                Platform.runLater(() -> {
                    allBooks.add(created);
                    booksTable.setItems(allBooks);
                    showAlert("Success", "Book \"" + created.getTitle() + "\" added!", Alert.AlertType.INFORMATION);
                });
                logger.info("Book added: {}", created.getBookId());
            });
            task.setOnFailed(event -> {
                logger.error("Failed to add book", event.getSource().getException());
                Platform.runLater(() -> showAlert("Error",
                        "Failed to add book: " + event.getSource().getException().getMessage(),
                        Alert.AlertType.ERROR));
            });
            new Thread(task).start();
        });
    }

    @FXML
    private void handleDeleteBook() {
        Book selected = booksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a book to delete", Alert.AlertType.WARNING);
            return;
        }

        var deleteTask = ApiClient.getInstance().deleteBook(selected.getId());
        deleteTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                allBooks.remove(selected);
                booksTable.setItems(allBooks);
                showAlert("Success", "Book deleted successfully", Alert.AlertType.INFORMATION);
            });
            logger.info("Book deleted: {}", selected.getBookId());
        });
        deleteTask.setOnFailed(event -> {
            logger.error("Failed to delete book", event.getSource().getException());
            showAlert("Error", "Failed to delete book: " + event.getSource().getException().getMessage(), Alert.AlertType.ERROR);
        });
        new Thread(deleteTask).start();
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