package com.work.cookbook;

import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ResourceBundle;

import com.work.cookbook.model.User;

/**
 * Controller class for the admin panel in a JavaFX application.
 * Handles the navigation and actions related to administrative tasks such as
 * adding, deleting, and modifying users,
 * as well as listing all users and navigating back to the main menu.
 */
public class adminPanelnewController implements Initializable {

    private boolean isAdmin;

    @FXML
    private Button btn_addUser;
    @FXML
    private Button btn_delUser;
    @FXML
    private Button btn_modifyUser;
    @FXML
    private Button btn_listUsers;
    @FXML
    private Button btn_back;

    private String loggedInUsername;

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Integer> list_userID;

    @FXML
    private TableColumn<User, String> list_name;

    @FXML
    private TableColumn<User, String> list_username;

    @FXML
    private TableColumn<User, String> list_password;

    @FXML
    private TableColumn<User, Boolean> list_admin;

    @FXML
    private TextField text_search;

    private DbOperations dbOperations = new DbOperations();

    /**
     * Initializes the controller class. This method is automatically called after
     * the FXML file has been loaded.
     * 
     * @param location  The location used to resolve relative paths for the root
     *                  object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if
     *                  the root object was not localized.
     */

    public void initialize(URL location, ResourceBundle resources) {
        // Setting cell value factories
        list_userID.setCellValueFactory(new PropertyValueFactory<>("userID"));
        list_username.setCellValueFactory(new PropertyValueFactory<>("username"));
        list_name.setCellValueFactory(new PropertyValueFactory<>("displayName"));
        list_password.setCellValueFactory(new PropertyValueFactory<>("Password"));
        list_admin.setCellValueFactory(new PropertyValueFactory<>("isAdmin"));

        // Fetch real user data from the database
        ObservableList<User> userList = dbOperations.getUserListFromDB();
        userTable.setItems(userList);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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

    /**
     * Sets the username of the currently logged-in user.
     * 
     * @param loggedInUsername The username of the user who is currently logged in.
     */
    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
    }

    // search user from table
    @FXML
    public void searchUser(ActionEvent event) {
        String searchText = text_search.getText().trim();
        ObservableList<User> filteredList = FXCollections.observableArrayList();

        DbConnector dbConnector = new DbConnector();

        try (Connection conn = dbConnector.getConnection()) {
            DbOperations dbOperations = new DbOperations();
            filteredList = dbOperations.searchUserByName(conn, searchText);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlertError("Database Error", "Could not search for user data.");
        }

        // Update the table view to display the filtered list
        userTable.setItems(filteredList);
    }

    /**
     * user table.
     */
    public void populateUserTable() {
        ObservableList<User> userList = dbOperations.getUserListFromDB();
        userTable.setItems(userList);
    }

    /**
     * Handles the action to add a new user. This method opens a new window where
     * users can be added.
     * 
     * @param event The event that triggered the method call.
     */
    @FXML
    private void addUser(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("popupAddUser.fxml"));
            Parent root = loader.load();

            popupAddUserController controller = loader.getController();

            // Create a new stage for the popup
            Stage popupStage = new Stage();
            Scene scene = new Scene(root);

            popupStage.setScene(scene);
            popupStage.setTitle("Add User");

            popupStage.initModality(Modality.APPLICATION_MODAL);

            popupStage.initOwner(((Button) event.getSource()).getScene().getWindow());

            popupStage.showAndWait();

            populateUserTable();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error loading Add User popup");
        }
    }

    private Transition createFadeTransition(Parent root, double durationSeconds, double fromValue, double toValue) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(durationSeconds), root);
        fadeTransition.setFromValue(fromValue);
        fadeTransition.setToValue(toValue);
        return fadeTransition;
    }

    /**
     * Handles the action to delete a user. This method opens a new window where
     * users can be deleted.
     * 
     * @param event The event that triggered the method call.
     */
    public void delUser(ActionEvent event) {
        System.out.println(loggedInUsername);
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // Check if the selected user is an admin
            if (selectedUser.isAdmin()) {
                showAlert("Permission Error", "Admin users cannot be deleted.");
                return; // Stop further execution
            }
            try {
                System.out.println(selectedUser);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("popupDelUser.fxml"));
                Parent root = loader.load();

                popupDelUserController controller = loader.getController();
                controller.initialize(selectedUser); // Pass the selected user to the controller

                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(((Button) event.getSource()).getScene().getWindow());
                stage.setScene(new Scene(root));
                stage.setTitle("Delete User Confirmation");
                Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
                fadeTransition.play();

                stage.showAndWait();

                populateUserTable(); // Refresh the table after deletion

            } catch (IOException e) {
                e.printStackTrace();
                showAlertError("Error", "Could not load the Delete User popup.");
            }
        } else {
            showAlert("Selection Error", "Please select a user to delete.");
        }
    }

    /**
     * Handles the action to modify user details. This method opens a new window
     * where user details can be modified.
     * 
     * @param event The event that triggered the method call.
     */
    @FXML
    public void modifyUser(ActionEvent event) {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // Check if the selected user is an admin
            if (selectedUser.isAdmin()) {
                showAlert("Permission Error", "Admin users cannot be modified.");
                return;
            }
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("popupModifyUser.fxml"));
                Parent root = loader.load();

                popupModifyUserController controller = loader.getController();
                controller.initialize(selectedUser);

                Stage popupStage = new Stage();
                Scene scene = new Scene(root);

                popupStage.setScene(scene);
                popupStage.setTitle("Modify User");

                popupStage.initModality(Modality.APPLICATION_MODAL);

                popupStage.initOwner(((Button) event.getSource()).getScene().getWindow());
                Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
                fadeTransition.play();

                popupStage.setOnHidden(e -> populateUserTable());

                popupStage.showAndWait();
                populateUserTable(); // Refresh the table after edit

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Error loading Modify User popup");
            }
        }
    }

    /**
     * Handles the action to navigate back to the main menu. This method opens the
     * main menu.
     * 
     * @param event The event that triggered the method call.
     */
    @FXML
    private void btn_backClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainMenu.fxml"));
            Parent root = loader.load();

            mainMenuController controller = loader.getController();
            controller.setLoggedInUsername(loggedInUsername);
            controller.setAdmin(isAdmin);
            controller.setAdminButtonVisibility(isAdmin); // Make sure to update the admin button visibility

            Scene mainMenuScene = new Scene(root);
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

            currentStage.setScene(mainMenuScene);
            currentStage.setTitle("Main Menu");
            currentStage.show();

            Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlertError("Error", "Could not load main menu for some reason.");
        }
    }

    /**
     * Sets whether the current user has administrative privileges.
     * 
     * @param isAdmin true if the user has admin privileges, false otherwise.
     */
    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    /**
     * Checks if the current user has administrative privileges.
     * 
     * @return true if the user is an admin, false otherwise.
     */
    public boolean isAdmin() {
        return isAdmin;
    }
}
