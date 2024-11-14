package com.work.cookbook;

import com.work.cookbook.model.Ingredient;
import com.work.cookbook.model.Recipe;
import com.work.cookbook.model.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class HoverRecipeController implements Initializable {

    @FXML
    private Label recipeName;

    @FXML
    private TextField recipeID;

    @FXML
    private TextField recipeTag;

    @FXML
    private TextField recipeServings;

    @FXML
    private TextField recipeDescription;

    @FXML
    private TextArea recipeInstructions;

    @FXML
    private TableView<Ingredient> resultsTable; // Update TableView type to Ingredient

    @FXML
    private TableColumn<Ingredient, String> ingredientColumn;

    @FXML
    private TableColumn<Ingredient, String> quantityColumn;

    private String recipeIdText;

    private final DbConnector dbConnector = new DbConnector();

    private DbOperations dbOperations;

    public void setRecipeDetails(Recipe recipe) {
        // Set recipe details
        recipeID.setText(String.valueOf(recipe.getRecipeId())); // Convert to String , recipeid
        recipeName.setText(recipe.getName());
        recipeTag.setText(recipe.getTag());
        recipeServings.setText(String.valueOf(recipe.getNumberOfPeople()));
        recipeDescription.setText(recipe.getShortDescription());
        recipeInstructions.setText(recipe.getDetailedDescription());
        recipeIdText = String.valueOf(recipe.getRecipeId()); // Assign the recipe ID to recipeIdText
        // System.out.println("Recipe ID loaded: " + recipeIdText);
        loadIngredients(); // Call loadData after setting recipe details

    }

    // load ingredients
    private void loadIngredients() {
        if (recipeIdText == null) {
            System.out.println("Null recipe");
            return;
        }

        ObservableList<Ingredient> ingredientsList = dbOperations.getIngredientsByRecipeId(recipeIdText);
        resultsTable.setItems(ingredientsList);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbOperations = new DbOperations();

        // load the ingredient data
        ingredientColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("Quantity"));

    }
}