package com.work.cookbook;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import org.w3c.dom.Text;

import com.work.cookbook.model.User;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class popupModifyUserController implements Initializable {

    private User userToUpdate;

    @FXML
    private TextField newNameField;

    @FXML
    private TextField newUsernameField;

    @FXML
    private TextField newPasswordField;

    @FXML
    private Button btn_editUser;

    @FXML
    private Button btn_abort;

    public void initialize(User user) {
        userToUpdate = user;
        if (newNameField != null && user != null) {
            newNameField.setText(user.getDisplayName());
            newUsernameField.setText(user.getUsername());
            newPasswordField.setText(user.getPassword());
        }
    }

    /*
     * edit user details
     */
    @FXML
    private void editUser() {
        // new user details from text fields
        String newName = newNameField.getText();
        String newUsername = newUsernameField.getText();
        String newPassword = newPasswordField.getText();

        // Check if any of the fields are empty
        if (newName.isEmpty() || newUsername.isEmpty() || newPassword.isEmpty()) {
            showAlert("Please fill in all fields.");
            return; // Exit the method without updating the user details
        }

        // Update user details in the database
        DbOperations dbOperations = new DbOperations();
        try {
            boolean success = dbOperations.editUser(userToUpdate.getUsername(), newName, newUsername, newPassword);

            if (success) {
                showAlert("User modified successfully.");
                closePopup();
            } else {
                showAlert("Failed to modify user. Please try again later.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("An unexpected error occurred while updating user. Please try again later.");
        }
    }

    @FXML
    private void abort() {
        closePopup();
    }

    private void closePopup() {
        Stage stage = (Stage) btn_abort.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
