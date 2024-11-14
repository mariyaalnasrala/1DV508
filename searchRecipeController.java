package com.work.cookbook;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import com.work.cookbook.model.Comment;
import com.work.cookbook.model.Recipe;

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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.ButtonType;

public class searchRecipeController implements Initializable {

    @FXML
    private Button btn_back;

    @FXML
    private TableView<Recipe> resultsTable;

    @FXML
    private TableColumn<Recipe, String> nameColumn;

    @FXML
    private TableColumn<Recipe, String> descriptionColumn;

    @FXML
    private TableColumn<Recipe, Integer> servingsColumn;

    @FXML
    private TableColumn<Recipe, String> commentsColumn;

    @FXML
    private TableColumn<Recipe, String> tagColumn;

    @FXML
    private TextField text_search;

    @FXML
    private Button btn_addRecipe;

    @FXML
    private Button btn_modifyRecipe;

    @FXML
    private Button btn_deleteRecipe;

    @FXML
    private Button btn_fav_recipe;

    @FXML
    private Button btn_comment;

    @FXML
    private Button btn_share_recipe;

    @FXML
    private HBox hbox_container;

    DbOperations dbOperations = new DbOperations();

    private String loggedInUsername;
    private Recipe selectedRecipe;

    public void setSelectedRecipe(Recipe recipe) {
        this.selectedRecipe = recipe; // Method to set the selected recipe
    }

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
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
    void addRecipe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addRecipe.fxml"));
            Parent root = loader.load();

            addRecipeController controller = loader.getController();
            controller.setLoggedInUsername(loggedInUsername);
            controller.setIssAdmin(isAdmin); // Set the isAdmin value

            Scene addRecipeScene = new Scene(root);
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

            // Set the new scene
            currentStage.setScene(addRecipeScene);
            currentStage.setTitle("Add Recipe");
            currentStage.show();
            Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlertError("Error", "Error loading Add Recipe page");
        }

    }

    @FXML
    void btn_backClicked(ActionEvent event) {
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

    @FXML
    void delRecipe() {
        Recipe selectedRecipe = resultsTable.getSelectionModel().getSelectedItem();

        if (selectedRecipe == null) {
            showAlertError("Error", "Please select a reccipie to delete. ");
            return;

        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm deletion of recipe");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete the selected recipe? ");

        ButtonType yesButton = new ButtonType("yes");
        ButtonType noButton = new ButtonType("no");

        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            if (dbOperations.deleteRecipe(selectedRecipe)) {
                showAlertInformation("success", "Recipie deleted successfully. ");
                populateTable();
            } else {
                showAlertError("Error", "Failed to delete recipe");
            }

        }

    }

    @FXML
    void modifyRecipe(ActionEvent event) {

        // Retrieve the selected recipe from the TableView
        Recipe selectedRecipe = resultsTable.getSelectionModel().getSelectedItem();

        // Check if a recipe is selected
        if (selectedRecipe != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("popupModifyRecipe.fxml"));
                Parent root = loader.load();

                popupModifyRecipeController controller = loader.getController();
                controller.initialize(selectedRecipe);
                controller.setSearchRecipeController(this);

                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Modify Recipe");
                stage.show();
                Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
                fadeTransition.play();

            } catch (IOException e) {
                e.printStackTrace();
                showAlertError("Error", "Could not load modify recipe popup.");
            }
        } else {
            // If no recipe is selected, display an error message
            showAlertError("No recipe selected.", "Please select a recipe to modify.");
        }

    }

    @FXML
    void fav_recipe() {
        Recipe selectedRecipe = resultsTable.getSelectionModel().getSelectedItem();

        if (selectedRecipe == null) {
            showAlertError("Error", "Please select a recipe to add to Favorites.");
            return;
        }

        int recipeId = selectedRecipe.getRecipeId(); // Get the ID of the selected recipe
        String recipeName = selectedRecipe.getName(); // Get the name of the selected recipe
        String shortDescription = selectedRecipe.getShortDescription(); // Get the short description of the selected
                                                                        // recipe

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm to add recipe to Favorites");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to add the recipe to favorites?");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");

        confirmAlert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            if (dbOperations.addToFavorites(loggedInUsername, recipeId, recipeName, shortDescription)) {
                showAlertInformation("Success", "Recipe added to Favorites successfully.");
                populateTable();
            } else {
                showAlertError("Error", "Failed to add recipe to Favorites.");
            }
        }
    }

    @FXML
    void share_recipe() {
        Recipe selectedRecipe = resultsTable.getSelectionModel().getSelectedItem(); // select the recipe

        // Check if a recipe is selected
        if (selectedRecipe != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("shareRecipe.fxml"));
                Parent root = loader.load();

                shareRecipeController controller = loader.getController();
                controller.initialize(selectedRecipe);
                controller.setLoggedInUsername(loggedInUsername);
                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Share Recipe");
                stage.show();
                Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
                fadeTransition.play();

            } catch (IOException e) {
                e.printStackTrace();
                showAlertError("Error", "Could not load share recipe popup.");
            }
        } else {
            // If no recipe is selected, display an error message
            showAlertError("No recipe selected.", "Please select a recipe to share.");
        }

    }

    @FXML
    public void searchRecipe() {
        String searchTerm = text_search.getText().trim();
        ObservableList<Recipe> filteredRecipes = FXCollections.observableArrayList();

        if (!searchTerm.isEmpty()) {
            // Iterate through all recipes in the table
            for (Recipe recipe : resultsTable.getItems()) {
                // Check if the recipe name contains the search term (case-insensitive)
                if (recipe.getName().toLowerCase().contains(searchTerm.toLowerCase())) {
                    // Add the matching recipe to the filtered list
                    filteredRecipes.add(recipe);
                } else if (recipe.getTag().toLowerCase().contains(searchTerm.toLowerCase())) {
                    filteredRecipes.add(recipe);
                }
            }
            // Update the table with the filtered recipes
            resultsTable.setItems(filteredRecipes);
        } else {
            // If the search term is empty, reload all recipes and their comments
            populateTable();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeColumns();
        populateTable();

        // Set up event handler for double-click on table rows
        resultsTable.setRowFactory(tv -> {
            TableRow<Recipe> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Recipe recipe = row.getItem();
                    showRecipeDetailsPopup(recipe);
                }
            });
            return row;
        });
    }

    private Stage recipeDetailsStage;

    private void showRecipeDetailsPopup(Recipe recipe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hoverRecipe.fxml"));
            Parent root = loader.load();

            HoverRecipeController controller = loader.getController();
            controller.setRecipeDetails(recipe);

            Scene scene = new Scene(root);

            if (recipeDetailsStage != null) {
                recipeDetailsStage.close(); // Close existing popup if open
            }

            recipeDetailsStage = new Stage();
            recipeDetailsStage.setScene(scene);
            recipeDetailsStage.setTitle("Recipe Details");
            recipeDetailsStage.show();

            Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlertError("Error", "Could not load recipe details popup.");
        }
    }

    @FXML
    void comment(ActionEvent event) {
        Recipe selectedRecipe = resultsTable.getSelectionModel().getSelectedItem();
        if (selectedRecipe != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("popupComment.fxml"));
                Parent root = loader.load();

                popupCommentController controller = loader.getController();
                controller.setupCommentController(selectedRecipe, loggedInUsername);

                // Pass the reference of searchRecipeController to popupCommentController
                controller.setSearchRecipeController(this);

                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.setTitle("Comment Recipe");

                stage.showAndWait(); // This will wait for the popup to close

                Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
                fadeTransition.play();

                // The refreshCommentsView() call is now moved to popupCommentController

            } catch (IOException e) {
                e.printStackTrace();
                showAlertError("Error", "Could not load comment recipe popup.");
            }
        } else {
            showAlertError("No recipe selected.", "Please select a recipe to comment.");
        }
    }

    public void refreshTable() {
        resultsTable.refresh();
    }

    private void initializeColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("shortDescription"));
        servingsColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfPeople"));
        tagColumn.setCellValueFactory(new PropertyValueFactory<>("tag"));
        commentsColumn.setCellValueFactory(new PropertyValueFactory<>("allComments"));

    }

    private void populateTable() {
        ObservableList<Recipe> recipes = dbOperations.getAllRecipes();
        for (Recipe recipe : recipes) {
            ObservableList<Comment> comments = dbOperations.getCommentsForRecipe(recipe.getRecipeId());
            recipe.setComments(comments);
        }
        resultsTable.setItems(recipes);
        resultsTable.refresh(); // Refresh the TableView to update the UI
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
