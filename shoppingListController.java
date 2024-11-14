package com.work.cookbook;

import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import javafx.scene.control.TableColumn;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import com.work.cookbook.model.Ingredient;

import java.io.IOException;

public class shoppingListController implements Initializable {

  @FXML
  private ComboBox<Integer> combo_week;

  @FXML
  private ComboBox<Integer> combo_week_2;

  @FXML
  private TableView<Ingredient> resultsTable;

  @FXML
  private TableColumn<Ingredient, String> ingredientColumn;

  @FXML
  private TableColumn<Ingredient, String> quantityColumn;

  @FXML
  private TextField text_ingredient;

  @FXML
  private TextField text_quantity;

  @FXML
  private Button btn_add_to_cart;

  @FXML
  private Button btn_remove;

  private String loggedInUsername;

  public void setLoggedInUsername(String loggedInUsername) {
    this.loggedInUsername = loggedInUsername;
  }

  private boolean isAdmin;

  public void setAdmin(boolean isAdmin) {
    this.isAdmin = isAdmin;
  }

  private ObservableList<Ingredient> resultsTableItems;
  private ObservableList<Ingredient> cartItems;

  @FXML
  private ListView<Ingredient> cart;

  private Transition createFadeTransition(Parent root, double durationSeconds, double fromValue, double toValue) {
    FadeTransition fadeTransition = new FadeTransition(Duration.seconds(durationSeconds), root);
    fadeTransition.setFromValue(fromValue);
    fadeTransition.setToValue(toValue);
    return fadeTransition;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    // restrict ingredient quantity field to accept only int
    text_quantity.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), null,
        change -> {
          String newText = change.getControlNewText();
          if (newText.matches("\\d*")) {
            return change;
          }
          return null;
        }));

    // double clikc to remove ingredient from cart in listview
    cart.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        Ingredient selectedItem = cart.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
          cartItems.remove(selectedItem);
        }
      }
    });

    // Initialize the combo_week and combo_week_2 ComboBoxes with data from the
    // database
    populateWeekComboBox(combo_week);
    populateWeekComboBox(combo_week_2);

    // Initialize the TableView columns
    ingredientColumn.setCellValueFactory(cellData -> cellData.getValue().ingredientProperty());
    quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());

    // initialize CartItems
    resultsTableItems = FXCollections.observableArrayList();
    cartItems = FXCollections.observableArrayList();

    cart.setItems(cartItems);
    resultsTable.setItems(resultsTableItems);

    // Add listeners to the ComboBoxes
    combo_week.setOnAction(event -> loadDataFromDatabase());
    combo_week_2.setOnAction(event -> loadDataFromDatabase());

    // Load data from the database and populate the TableView
    loadDataFromDatabase();
  }

  private void populateWeekComboBox(ComboBox<Integer> comboBox) {
    // Establish a database connection
    DbConnector dbConnector = new DbConnector();
    try (Connection connection = dbConnector.getConnection()) {
      // Create a statement object
      Statement statement = connection.createStatement();

      // Execute a SELECT query to retrieve distinct week numbers from the
      // ShoppingList table
      String query = "SELECT DISTINCT Week_Number FROM Week";
      ResultSet resultSet = statement.executeQuery(query);

      // Iterate over the result set and populate the ComboBox
      ObservableList<Integer> weekOptions = FXCollections.observableArrayList();
      while (resultSet.next()) {
        int weekNumber = resultSet.getInt("Week_Number");
        weekOptions.add(weekNumber);
      }
      comboBox.setItems(weekOptions);

      // Close the result set, statement, and connection
      resultSet.close();
      statement.close();
      connection.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void loadDataFromDatabase() {
    // Get the selected week number from the combo_week ComboBox
    Integer selectedWeek = combo_week.getValue();
    int userID = getUserID(loggedInUsername);

    if (selectedWeek == null || userID == -1) {
      return; // No week selected or invalid user, do nothing
    }

    // Establish a database connection
    DbConnector dbConnector = new DbConnector();
    try (Connection connection = dbConnector.getConnection()) {
      // Build the query to filter by selected week and user
      String query = "SELECT Item_Name, Quantity FROM ShoppingList WHERE Week_Number = ? AND User_ID = ?";

      // Prepare the statement
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setInt(1, selectedWeek);
      preparedStatement.setInt(2, userID);

      // Execute the query
      ResultSet resultSet = preparedStatement.executeQuery();

      // Clear existing data
      resultsTable.getItems().clear();

      // Iterate over the result set and populate the TableView
      while (resultSet.next()) {
        String ingredient = resultSet.getString("Item_Name");
        String quantity = resultSet.getString("Quantity");

        Ingredient item = new Ingredient(ingredient, quantity);
        resultsTable.getItems().add(item);
      }

      // Close the result set and statement
      resultSet.close();
      preparedStatement.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void removeIngredient() {
    // Get the selected ingredient
    Ingredient selectedIngredient = resultsTable.getSelectionModel().getSelectedItem();

    // Check if an ingredient is selected
    if (selectedIngredient == null) {
      showAlertError("No Selection", "Please select an ingredient to delete.");
      return;
    }

    // Show confirmation dialog
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Confirm Deletion");
    alert.setHeaderText(null);
    alert.setContentText("Are you sure you want to delete the selected ingredient?");

    // Wait for user response
    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      // User confirmed deletion, proceed with deletion
      deleteIngredient(selectedIngredient);
    }
  }

  private void deleteIngredient(Ingredient ingredient) {
    // Get the selected week number
    Integer selectedWeek = combo_week.getValue();

    // Check if a week is selected
    if (selectedWeek == null) {
      showAlertError("No Week Selected", "Please select a week.");
      return;
    }

    // Get the User_ID of the logged-in user
    int userID = getUserID(loggedInUsername);

    // Validate User_ID
    if (userID == -1) {
      showAlertError("User Error", "Failed to retrieve the user ID. Please try logging in again.");
      return;
    }

    // Establish a database connection
    DbConnector dbConnector = new DbConnector();
    try (Connection connection = dbConnector.getConnection()) {
      // Prepare the DELETE statement
      String query = "DELETE FROM ShoppingList WHERE User_ID = ? AND Week_Number = ? AND Item_Name = ?";
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setInt(1, userID);
      preparedStatement.setInt(2, selectedWeek);
      preparedStatement.setString(3, ingredient.getIngredient());

      // Execute the DELETE statement
      int affectedRows = preparedStatement.executeUpdate();

      // Check if the deletion was successful
      if (affectedRows > 0) {
        // Remove the ingredient from the table view
        resultsTableItems.remove(ingredient);
        showAlertInformation("Success", "Ingredient deleted successfully.");
      } else {
        showAlertError("Deletion Error", "Failed to delete the ingredient.");
      }

      // Close the prepared statement
      preparedStatement.close();
    } catch (SQLException e) {
      e.printStackTrace();
      showAlertError("Database Error", "Failed to delete the ingredient.");
    } catch (Exception e) {
      e.printStackTrace();
      showAlertError("Unexpected Error", "An unexpected error occurred.");
    }
  }

  private int getUserID(String username) {
    DbConnector dbConnector = new DbConnector();
    int userID = -1;
    try (Connection connection = dbConnector.getConnection()) {
      String query = "SELECT User_ID FROM User WHERE Display_Name = ?";
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setString(1, username);
      ResultSet resultSet = preparedStatement.executeQuery();
      if (resultSet.next()) {
        userID = resultSet.getInt("User_ID");
      } else {
        System.out.println("No user found with username: " + username);
      }
      resultSet.close();
      preparedStatement.close();
    } catch (SQLException e) {
      e.printStackTrace();
      System.out.println("Database error: " + e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Unexpected error: " + e.getMessage());
    }
    return userID;
  }

  @FXML
  private void handleAddToCart(ActionEvent event) {
    String ingredient = text_ingredient.getText();
    String quantity = text_quantity.getText();
    Integer selectedWeek = combo_week_2.getValue();

    if (ingredient.isEmpty() || quantity.isEmpty() || selectedWeek == null) {
      showAlertError("Input Error", "Please enter ingredient, quantity, and select a week.");
      return;
    }

    Ingredient item = new Ingredient(ingredient, quantity);
    cartItems.add(item);
    // System.out.println("Items in cart: " + cartItems);

    // resultsTable.setItems(cartItems);

    text_ingredient.clear();
    text_quantity.clear();

  }

  @FXML
  private void generate_shopping_list() {
    Integer selectedWeek = combo_week_2.getValue();

    if (selectedWeek == null) {
      showAlertError("Selection Error", "Please select a week.");
      return; // No week selected, show error and return
    }

    // Get the User_ID of the logged-in user
    int userID = getUserID(loggedInUsername);

    // Validate User_ID
    if (userID == -1) {
      showAlertError("User Error", "Failed to retrieve the user ID. Please try logging in again.");
      return;
    }

    insertcartItemsToDb(userID, selectedWeek);

    cartItems.clear();
    // resultsTable.getItems().clear();

    loadDataFromDatabase();

    showAlertInformation("Success", "Shopping list generated successfully.");

  }

  private void insertcartItemsToDb(int userID, int selectedWeek) {
    DbConnector dbConnector = new DbConnector();
    try (Connection connection = dbConnector.getConnection()) {
      // Prepare an INSERT statement
      String query = "INSERT INTO ShoppingList (User_ID, Item_Name, Quantity, Week_Number) VALUES (?, ?, ?, ?)";
      PreparedStatement preparedStatement = connection.prepareStatement(query);

      for (Ingredient item : cartItems) {
        preparedStatement.setInt(1, userID);
        preparedStatement.setString(2, item.getIngredient());
        preparedStatement.setInt(3, Integer.parseInt(item.getQuantity()));
        preparedStatement.setInt(4, selectedWeek);

        // Execute the INSERT statement
        preparedStatement.executeUpdate();
      }

      // Close the PreparedStatement and the Connection
      preparedStatement.close();
      connection.close();
    } catch (Exception e) {
      e.printStackTrace();
      showAlertError("Database Error", "Failed to add item to the shopping list.");
    }
  }

  @FXML
  public void btn_backClicked(ActionEvent event) {
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

  private void showAlertInformation(String title, String message) {
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