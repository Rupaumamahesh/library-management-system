package com.library.client.controller;

import com.library.client.MainApp;
import com.library.client.api.ApiClient;
import com.library.client.model.Member;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
        ApiClient.getInstance().getMembers()
                .setOnSucceeded(event -> {
                    List<Member> members = (List<Member>) event.getSource().getValue();
                    ObservableList<Member> items = FXCollections.observableArrayList(members);
                    Platform.runLater(() -> membersTable.setItems(items));
                    logger.info("Loaded {} members", members.size());
                })
                .setOnFailed(event -> {
                    logger.error("Failed to load members", event.getSource().getException());
                    showAlert("Error", "Failed to load members", Alert.AlertType.ERROR);
                });
        new Thread(ApiClient.getInstance().getMembers()).start();
    }

    @FXML
    private void handleRegister() {
        showAlert("Register Member", "Register member feature not yet implemented", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleViewHistory() {
        Member selected = membersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a member", Alert.AlertType.WARNING);
            return;
        }
        showAlert("History", "View history for " + selected.getName() + " not yet implemented", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleDelete() {
        Member selected = membersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Error", "Please select a member to delete", Alert.AlertType.WARNING);
            return;
        }

        ApiClient.getInstance().deleteMember(selected.getId())
                .setOnSucceeded(event -> {
                    Platform.runLater(() -> {
                        membersTable.getItems().remove(selected);
                        showAlert("Success", "Member deleted successfully", Alert.AlertType.INFORMATION);
                    });
                    logger.info("Member deleted: {}", selected.getMemberId());
                })
                .setOnFailed(event -> {
                    logger.error("Failed to delete member", event.getSource().getException());
                    showAlert("Error", "Failed to delete member: " + event.getSource().getException().getMessage(), Alert.AlertType.ERROR);
                });
        new Thread(ApiClient.getInstance().deleteMember(selected.getId())).start();
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
