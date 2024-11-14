package com.work.cookbook;

import com.work.cookbook.model.Recipe;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class popupModifyRecipeController implements Initializable {

    @FXML
    private TextField newNameField;

    @FXML
    private TextField newDescriptionField;

    @FXML
    private TextArea newInstructionsArea;

    @FXML
    private TextField newServings;

    @FXML
    private ComboBox<String> tags;

    @FXML
    private Button btn_editRecipe;

    @FXML
    private Button btn_abort;

    private Recipe modifiedRecipe;

    private searchRecipeController searchController;

    public void setSearchRecipeController(searchRecipeController controller) {
        this.searchController = controller;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // restrict num of people field to accept only int
        newServings.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), null,
                change -> {
                    String newText = change.getControlNewText();
                    if (newText.matches("\\d*")) {
                        return change;
                    }
                    return null;
                }));
        btn_editRecipe.setOnAction(event -> editRecipe());

        // Populate the ComboBox with tags from the database
        tagList();
    }

    // Method to initialize the text fields with the data of the selected recipe
    public void initialize(Recipe recipe) {
        if (recipe != null) {
            newNameField.setText(recipe.getName());
            newDescriptionField.setText(recipe.getShortDescription());
            newInstructionsArea.setText(recipe.getDetailedDescription());
            newServings.setText(String.valueOf(recipe.getNumberOfPeople()));
            modifiedRecipe = recipe;

            tags.getSelectionModel().select(recipe.getTag());
        }
    }

    // Method to populate the ComboBox with tags from the database
    private void tagList() {
        try {
            ObservableList<String> tagNames = DbOperations.getAllTags();
            tags.setItems(tagNames);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Failed to load tags from the database.");
        }
    }

    /*
     * modify recipe
     * 
     */
    @FXML
    private void editRecipe() {
        // Get new values from text fields
        String newName = newNameField.getText();
        String newDescription = newDescriptionField.getText();
        String newInstructions = newInstructionsArea.getText();
        String newServingsText = newServings.getText();
        int newServings = Integer.parseInt(newServingsText);
        String newTag = tags.getValue();

        // Update the modified recipe with the new values
        modifiedRecipe.setName(newName);
        modifiedRecipe.setShortDescription(newDescription);
        modifiedRecipe.setDetailedDescription(newInstructions);
        modifiedRecipe.setNumberOfPeople(newServings);
        modifiedRecipe.setTag(newTag);

        DbOperations dbOperations = new DbOperations();

        boolean success = DbOperations.editRecipe(modifiedRecipe);
        if (success) {
            showAlert("Recipe modified successfully.");
            closePopup();
        } else {
            showAlert("Failed to modify recipe. Please try again later.");
        }
    }

    @FXML
    private void abort() {
        closePopup();
    }

    private void closePopup() {
        Stage stage = (Stage) btn_abort.getScene().getWindow();
        stage.close();
        Platform.runLater(() -> searchController.refreshTable());
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
