package com.work.cookbook;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This class handles the user interface for adding new users in a JavaFX
 * application.
 * It allows for input of user details, such as name, username, password, and
 * admin status,
 * and provides the functionality to add these details to a database.
 */
public class popupAddUserController {

    // Text field for entering the user's name.
    @FXML
    private TextField text_Name;

    // Text field for entering the user's username.
    @FXML
    private TextField text_Username;

    // Text field for entering the user's password.
    @FXML
    private TextField text_password;

    // Radio button to select if the user is an admin (Yes).
    @FXML
    private RadioButton radioAdmin_Yes;

    // Radio button to select if the user is not an admin (No).
    @FXML
    private RadioButton radioAdmin_No;

    // Button to trigger the addition of a new user.
    @FXML
    private Button btn_addUser;

    private final DbOperations dbOperations = new DbOperations();

    private final ToggleGroup adminToggleGroup = new ToggleGroup();

    public void initialize() {
        radioAdmin_Yes.setToggleGroup(adminToggleGroup);
        radioAdmin_No.setToggleGroup(adminToggleGroup);
    }

    /**
     * This method is triggered by pressing the 'Add User' button.
     * It retrieves user inputs, validates them, and then calls {@link #insertUser}
     * to add them to the database.
     * Alerts the user about the success or failure of the operation.
     *
     * @param event The event that triggered this method.
     */
    @FXML
    private void addUser(ActionEvent event) throws SQLException {
        String name = text_Name.getText().trim();
        String username = text_Username.getText().trim();
        String password = text_password.getText().trim();
        boolean isAdmin = radioAdmin_Yes.isSelected();

        if (name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "All fields are required!", Alert.AlertType.ERROR);
            return;
        }

        dbOperations.insertUser(name, username, password, isAdmin);
        showAlert("Success", "User has been added successfully.", AlertType.INFORMATION);
        closePopup();

    }

    private void closePopup() {
        Stage stage = (Stage) btn_addUser.getScene().getWindow();
        stage.close();
    }

    /**
     * Displays an alert to the user.
     *
     * @param title     The title of the alert.
     * @param message   The message to display in the alert.
     * @param alertType The type of alert to show.
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlertError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
