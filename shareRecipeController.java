package com.work.cookbook;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.work.cookbook.model.Recipe;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class shareRecipeController implements Initializable {

    @FXML
    private Button btn_share_recipe;

    @FXML
    private ComboBox<String> users;

    @FXML
    private TextArea text_message;

    private DbOperations dbOperations = new DbOperations();

    @FXML
    private TextField text_selectedRecipe;

    private String loggedInUsername;

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // fill combobox after popup starts
        getUsers();
    }

    // method to initialize labe with the name of the selected recipe
    public void initialize(Recipe recipe) {
        if (recipe != null) {
            text_selectedRecipe.setText(recipe.getName());
        }
        // System.out.println(loggedInUsername);
    }

    // fill the combox box with names of all users
    @FXML
    private void getUsers() {
        ObservableList<String> userList = FXCollections.observableArrayList();

        // Call method from DbOperations to get user display names
        DbOperations dbOperations = new DbOperations();
        List<String> displayNames = dbOperations.getAllUserDisplayNames();
        userList.addAll(displayNames);

        users.setItems(userList);
    }

    // button for sharing recipe logic
    @FXML
    private void shareRecipe() {
        // get the text for recipe name, message and select user
        String selectedRecipeName = text_selectedRecipe.getText();
        String selectedUser = users.getValue();
        String message = text_message.getText();
        // error hadnler
        if (selectedUser == null || message.isEmpty()) {
            showAlertError("Error", "Please select a user and enter a message.");
            return;
        }
        // get the sender's user ID using the loggedInUsername
        int senderId = dbOperations.getUserIdByName(loggedInUsername);

        int recipeId = dbOperations.getRecipeIdByName(selectedRecipeName);
        int receiverId = dbOperations.getUserIdByName(selectedUser);
        // use dboperation to send recipe to db message table
        if (dbOperations.sendMessageToUser(recipeId, senderId, receiverId, message)) {
            showAlert("Success", "Recipe sent to " + selectedUser + ".");
            // close the popup
            Stage stage = (Stage) btn_share_recipe.getScene().getWindow();
            stage.close();
        } else {
            showAlertError("Error", "Failed to send recipe.");
        }
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
}
