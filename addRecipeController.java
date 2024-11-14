package com.work.cookbook;

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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class addRecipeController implements Initializable {

    @FXML
    private Button btn_addIngredient;

    @FXML
    private Button btn_addRecipe;

    @FXML
    private Button btn_addTag;

    @FXML
    private Button btn_back;

    @FXML
    private ComboBox<String> dropExistingtag;

    @FXML
    private ImageView img_animation;

    @FXML
    private Label labelDescription;

    @FXML
    private Label labelName;

    @FXML
    private Label label_Ingredients;

    @FXML
    private Label label_choose_tag;

    @FXML
    private Label label_detailed_des;

    @FXML
    private TextField text_Name;

    @FXML
    private TextField text_amount;

    @FXML
    private TextField text_description;

    @FXML
    private TextArea text_detailed_des;

    @FXML
    private TextField text_ingredientName;

    @FXML
    private TextField text_newTag;

    @FXML
    private TextField number_of_people;

    @FXML
    private TextField text_ingredientQuantity;

    @FXML
    private ListView<String> ingredientListView;

    private ObservableList<String> ingredients = FXCollections.observableArrayList();
    @FXML

    private String loggedInUsername;

    private final DbOperations dbOperations = new DbOperations();

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
    }

    @FXML
    private void selectExistingTag(ActionEvent event) {
    }

    private Transition createFadeTransition(Parent root, double durationSeconds, double fromValue, double toValue) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(durationSeconds), root);
        fadeTransition.setFromValue(fromValue);
        fadeTransition.setToValue(toValue);
        return fadeTransition;
    }
    // logic for adding ingredient

    @FXML
    private void addIngredient(ActionEvent event) {
        String ingredient = text_ingredientName.getText();
        String quantity = text_ingredientQuantity.getText();
        if (ingredient.isEmpty()) {
            showAlertError("Error", "Ingredient name cannot be empty.");
            return;
        }

        if (quantity.isEmpty()) {
            showAlertError("Error", "Ingredient quantity cannot be empty.");
            return;
        }

        String displayText = ingredient + ": " + quantity;
        ingredients.add(displayText);
        ingredientListView.setItems(ingredients);
        text_ingredientName.clear();
        text_ingredientQuantity.clear();
    }

    @FXML
    private void addTag_List(ActionEvent event) {
        String newTag = text_newTag.getText().trim();

        if (newTag.isEmpty()) {
            showAlertError("Error", "Please enter a new tag.");
            return;
        }

        try {
            dbOperations.addTag(newTag);
            showAlertSuccess("Success", "New tag added successfully.");
            populateTagComboBox();
            text_newTag.clear();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlertError("Error", "Error adding new tag to database.");
        }
    }

    // action event addrecipe with db op to add recipe
    @FXML
    private void addRecipe(ActionEvent event) {
        String name = text_Name.getText();
        String shortDescription = text_description.getText();
        String detailedDescription = text_detailed_des.getText();
        String numberOfPeopleStr = number_of_people.getText();
        String selectedTag = dropExistingtag.getSelectionModel().getSelectedItem();
        String tag = (selectedTag != null && !selectedTag.isEmpty()) ? selectedTag : "";

        // Validate if any of the required fields are empty

        if (name.isEmpty() || shortDescription.isEmpty() || detailedDescription.isEmpty()
                || numberOfPeopleStr.isEmpty()) {
            showAlertError("Error", "All fields with * must be filled in to add a recipe.");
            return;
        }

        int numberOfPeople = Integer.parseInt(numberOfPeopleStr);

        try {
            dbOperations.addRecipe(name, shortDescription, detailedDescription, numberOfPeople, ingredients,
                    tag);
            showAlertSuccess("Success", "Recipe has been added successfully.");
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlertError("Error", "Error adding recipe to database for some reason.");
        }
    }

    // alert info
    private void showAlertSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // clear fields after recipe add
    private void clearFields() {
        text_Name.clear();
        text_description.clear();
        text_detailed_des.clear();
        number_of_people.clear();
        ingredients.clear();
        text_ingredientQuantity.clear();
        text_ingredientName.clear();
        ingredientListView.getItems().clear();
        text_newTag.clear();
    }

    // Init and restrict some textfields to only accept Int
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // restrict ingredient quantity field to accept only int
        text_ingredientQuantity.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), null,
                change -> {
                    String newText = change.getControlNewText();
                    if (newText.matches("\\d*")) {
                        return change;
                    }
                    return null;
                }));

        // restrict num of people field to accept only int
        number_of_people.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), null,
                change -> {
                    String newText = change.getControlNewText();
                    if (newText.matches("\\d*")) {
                        return change;
                    }
                    return null;
                }));

        // double-click event handler to the ListView to remove INgredient from list
        ingredientListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedItem = ingredientListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    ingredients.remove(selectedItem);
                    ingredientListView.setItems(ingredients);
                }
            }
        });

        populateTagComboBox();
    }

    private boolean isAdmin;

    public void setIssAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    private void populateTagComboBox() {
        try {
            ObservableList<String> tags = dbOperations.getAllTags();
            dropExistingtag.setItems(tags);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlertError("Error", "Could not load tags from Database.");
        }
    }

    @FXML
    private void btn_backClicked(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("searchRecipe.fxml"));
            Parent root = loader.load();

            searchRecipeController controller = loader.getController();
            controller.setLoggedInUsername(loggedInUsername);
            controller.setAdmin(isAdmin);

            Scene browseRecipScene = new Scene(root);
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

            currentStage.setScene(browseRecipScene);
            currentStage.setTitle("Browse Recipe");
            currentStage.show();
            Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlertError("Error", "Could not load Browse Recipe for some reason.");
        }
    }

    private void showAlertError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
