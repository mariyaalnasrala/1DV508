package com.work.cookbook;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

import com.work.cookbook.model.Recipe;

import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class favRecipeController implements Initializable {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TableView<Recipe> resultsTable;

    @FXML
    private TableColumn<Recipe, String> nameColumn;

    @FXML
    private TableColumn<Recipe, String> descriptionColumn;

    @FXML
    private Button btn_remove_fav;

    @FXML
    private Button btn_back;

    private String loggedInUsername;

    private DbOperations dbOperations = new DbOperations();

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
        populateTable(); // Call populateTable when loggedInUsername is set
    }

    private boolean isAdmin;

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    private Transition createFadeTransition(Parent root, double durationSeconds, double fromValue, double toValue) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(durationSeconds), root);
        fadeTransition.setFromValue(fromValue);
        fadeTransition.setToValue(toValue);
        return fadeTransition;
    }

    @FXML
    void btn_backClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainMenu.fxml"));
            Parent root = loader.load();

            mainMenuController controller = loader.getController();
            controller.setLoggedInUsername(loggedInUsername);
            controller.setAdmin(isAdmin);
            controller.setAdminButtonVisibility(isAdmin);

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

    @FXML
    public void remove_favourite(ActionEvent event) {
        Recipe selectedRecipe = resultsTable.getSelectionModel().getSelectedItem();

        if (selectedRecipe == null) {
            showAlertError("Error", "Please select a recipe to remove from favorites. ");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm removal recipe");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to remove this recipe from favorites? ");

        ButtonType yesButton = new ButtonType("yes");
        ButtonType noButton = new ButtonType("No");

        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            try {
                // Get the ID of the selected recipe from the FavoriteRecipe table
                int recipeId = dbOperations.getFavoriteRecipeId(loggedInUsername, selectedRecipe.getName());
                // Call the method to remove the recipe from favorites in the database
                if (dbOperations.deleteFavoriteRecipe(loggedInUsername, recipeId)) {
                    resultsTable.getItems().remove(selectedRecipe);
                    showAlertInformation("Success", "Recipe removed from favorites successfully. ");
                } else {
                    showAlertError("Error", "Failed to remove recipe from favorites. ");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlertError("Error", "An error occurred while removing recipe from favorites.");
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // System.out.println(loggedInUsername); // for troubleshooting to get correct
        // table of the user
        initializeColumns();
    }

    private void initializeColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("shortDescription"));
    }

    private void populateTable() {
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            return;
        }
        try {
            ObservableList<Recipe> favoriteRecipes = dbOperations.getFavoriteRecipes(loggedInUsername);

            // Print the size of the fetched list for troubleshoot
            // System.out.println(
            // "Number of favorite recipes fetched: " + favoriteRecipes.size() + " of User "
            // + "' "
            // + loggedInUsername + " '");

            // Populate the table with the fetched favorite recipes
            resultsTable.setItems(favoriteRecipes);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlertError("Error", "Failed to fetch favorite recipes from the database.");
        }
    }

    private void showAlertError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlertInformation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
