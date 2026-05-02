package com.library.client.controller;

import com.library.client.MainApp;
import com.library.client.api.ApiClient;
import com.library.client.model.Member;
import com.library.client.model.Transaction;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the members screen.
 * Displays members in a TableView.
 * Register new members, view history, delete members.
 */
public class MemberController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    @FXML
    private TableView<Member> membersTable;

    @FXML
    private TableColumn<Member, String> memberIdColumn;

    @FXML
    private TableColumn<Member, String> nameColumn;

    @FXML
    private TableColumn<Member, String> emailColumn;

    @FXML
    private TableColumn<Member, String> phoneColumn;

    @FXML
    private TableColumn<Member, Integer> borrowedCountColumn;

    @FXML
    private Button registerButton;

    @FXML
    private Button viewHistoryButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button backButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.debug("MemberController initialized");
        setupTableColumns();
        loadMembers();
    }

    private void setupTableColumns() {
        memberIdColumn.setCellValueFactory(new PropertyValueFactory<>("memberId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        borrowedCountColumn.setCellValueFactory(new PropertyValueFactory<>("borrowedCount"));
    }

    private void loadMembers() {
        var task = ApiClient.getInstance().getMembers();
        task.setOnSucceeded(event -> {
            List<Member> members = (List<Member>) event.getSource().getValue();
            ObservableList<Member> items = FXCollections.observableArrayList(members);
            Platform.runLater(() -> membersTable.setItems(items));
            logger.info("Loaded {} members", members.size());
        });
        task.setOnFailed(event -> {
            logger.error("Failed to load members", event.getSource().getException());
            showAlert("Error", "Failed to load members", Alert.AlertType.ERROR);
        });
        new Thread(task).start();
    }

    @FXML
    private void handleRegister() {
        Dialog<Member> dialog = new Dialog<>();
        dialog.setTitle("Register New Member");
        dialog.setHeaderText("Enter member details");

        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField memberIdField = new TextField();
        memberIdField.setPromptText("e.g. M001");
        TextField nameField = new TextField();
        nameField.setPromptText("Full name");
        TextField emailField = new TextField();
        emailField.setPromptText("email@example.com");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone (optional)");

        grid.add(new Label("Member ID:"), 0, 0);
        grid.add(memberIdField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node registerBtn = dialog.getDialogPane().lookupButton(registerButtonType);
        registerBtn.setDisable(true);
        memberIdField.textProperty().addListener((obs, old, val) ->
                registerBtn.setDisable(val.trim().isEmpty() || nameField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()));
        nameField.textProperty().addListener((obs, old, val) ->
                registerBtn.setDisable(val.trim().isEmpty() || memberIdField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty()));
        emailField.textProperty().addListener((obs, old, val) ->
                registerBtn.setDisable(val.trim().isEmpty() || memberIdField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                Member member = new Member();
                member.setMemberId(memberIdField.getText().trim());
                member.setName(nameField.getText().trim());
                member.setEmail(emailField.getText().trim());
                member.setPhone(phoneField.getText().trim());
                return member;
            }
            return null;
        });

        Optional<Member> result = dialog.showAndWait();
        result.ifPresent(member -> {
            var task = ApiClient.getInstance().addMember(member);
            task.setOnSucceeded(event -> {
                Member created = (Member) event.getSource().getValue();
                Platform.runLater(() -> {
                    membersTable.getItems().add(created);
                    showAlert("Success", "Member \"" + created.getName() + "\" registered!", Alert.AlertType.INFORMATION);
                });
                logger.info("Member registered: {}", created.getMemberId());
            });
            task.setOnFailed(event -> {
                logger.error("Failed to register member", event.getSource().getException());
                Platform.runLater(() -> showAlert("Error",
                        "Failed to register member: " + event.getSource().getException().getMessage(),
                        Alert.AlertType.ERROR));
            });
            new Thread(task).start();
        });
    }

    @FXML
    private void handleViewHistory() {
        Member selected = membersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a member", Alert.AlertType.WARNING);
            return;
        }

        // Build dialog with a TableView of transactions
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Borrow History");
        dialog.setHeaderText("History for: " + selected.getName() + " (" + selected.getMemberId() + ")");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(650, 400);

        // Table setup
        TableView<Transaction> historyTable = new TableView<>();
        historyTable.setPlaceholder(new Label("No borrow history found"));

        TableColumn<Transaction, String> recordCol = new TableColumn<>("Record ID");
        recordCol.setCellValueFactory(new PropertyValueFactory<>("recordId"));
        recordCol.setPrefWidth(100);

        TableColumn<Transaction, String> bookCol = new TableColumn<>("Book ID");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        bookCol.setPrefWidth(100);

        TableColumn<Transaction, String> borrowCol = new TableColumn<>("Borrow Date");
        borrowCol.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        borrowCol.setPrefWidth(120);

        TableColumn<Transaction, String> returnCol = new TableColumn<>("Return Date");
        returnCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        returnCol.setPrefWidth(120);

        TableColumn<Transaction, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);

        historyTable.getColumns().addAll(recordCol, bookCol, borrowCol, returnCol, statusCol);

        Label loadingLabel = new Label("Loading history...");
        VBox content = new VBox(10, loadingLabel, historyTable);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        // Load history in background
        var task = ApiClient.getInstance().getHistory(selected.getId());
        task.setOnSucceeded(event -> {
            List<Transaction> transactions = (List<Transaction>) event.getSource().getValue();
            Platform.runLater(() -> {
                loadingLabel.setText("Total records: " + transactions.size());
                historyTable.setItems(FXCollections.observableArrayList(transactions));
            });
            logger.info("Loaded {} history records for member {}", transactions.size(), selected.getMemberId());
        });
        task.setOnFailed(event -> {
            logger.error("Failed to load history", event.getSource().getException());
            Platform.runLater(() -> loadingLabel.setText("Failed to load history: " + event.getSource().getException().getMessage()));
        });
        new Thread(task).start();

        dialog.showAndWait();
    }

    @FXML
    private void handleDelete() {
        Member selected = membersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a member to delete", Alert.AlertType.WARNING);
            return;
        }

        var deleteTask = ApiClient.getInstance().deleteMember(selected.getId());
        deleteTask.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                membersTable.getItems().remove(selected);
                showAlert("Success", "Member deleted successfully", Alert.AlertType.INFORMATION);
            });
            logger.info("Member deleted: {}", selected.getMemberId());
        });
        deleteTask.setOnFailed(event -> {
            logger.error("Failed to delete member", event.getSource().getException());
            showAlert("Error", "Failed to delete member: " + event.getSource().getException().getMessage(), Alert.AlertType.ERROR);
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