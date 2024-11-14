package com.work.cookbook;

import com.work.cookbook.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class popupDelUserController {

    @FXML
    private Label username;

    @FXML
    private Button btn_delUser;

    @FXML
    private Button btn_abort;

    // Method to initialize the popup with the user's data
    public void initialize(User user) {
        username.setText(user.getUsername());
    }

    // Method to handle the deletion logic
    @FXML
    public void deleteUser() {
        DbOperations dbOperations = new DbOperations();
        boolean success = dbOperations.deleteUser(username.getText());

        if (success) {
            showAlert("User deleted successfully.");
        } else {
            showAlert("Failed to delete user.");
        }

        closePopup();
    }

    // Method to handle the abort action
    @FXML
    public void abortDeletion() {
        closePopup();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closePopup() {
        Stage stage = (Stage) btn_delUser.getScene().getWindow();
        stage.close();
    }
}
