package com.work.cookbook;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import com.work.cookbook.DbConnector;
import com.work.cookbook.model.Recipe;

public class weeklyListController implements Initializable {

    private boolean isAdmin;

    @FXML
    private Button btn_back;

    @FXML
    private ComboBox<String> weekComboBox;

    @FXML
    private ListView<Recipe> listMonday;

    @FXML
    private ListView<Recipe> listTuesday;

    @FXML
    private ListView<Recipe> listWednesday;

    @FXML
    private ListView<Recipe> listThursday;

    @FXML
    private ListView<Recipe> listFriday;

    @FXML
    private ListView<Recipe> listSaturday;

    @FXML
    private ListView<Recipe> listSunday;

    @FXML
    private Label labelFriday;

    @FXML
    private Label labelTuesday;

    @FXML
    private Label labelWednesday;

    @FXML
    private Label labelThursday;

    @FXML
    private Label labelMonday;

    @FXML
    private Label labelSaturday;

    @FXML
    private Label labelSunday;

    private String loggedInUsername;

    private Transition createFadeTransition(Parent root, double durationSeconds, double fromValue, double toValue) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(durationSeconds), root);
        fadeTransition.setFromValue(fromValue);
        fadeTransition.setToValue(toValue);
        return fadeTransition;
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

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
    }

    public void setIssAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            loadWeeks();
        } catch (SQLException e) {
            showAlertError("Database Error", "Failed to connect to the database.");
        }
    }

    private void loadWeeks() throws SQLException {
        DbConnector dbConnector = new DbConnector();
        try (Connection connection = dbConnector.getConnection()) {
            String query = "SELECT DISTINCT Week_Number FROM Week ORDER BY Week_Number";
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            ObservableList<String> weeks = FXCollections.observableArrayList();
            while (resultSet.next()) {
                weeks.add(resultSet.getString("Week_Number"));
            }
            weekComboBox.setItems(weeks);
            weekComboBox.setOnAction(e -> {
                String selectedWeek = weekComboBox.getSelectionModel().getSelectedItem();
                if (selectedWeek != null) {
                    clearListViews();
                    loadRecipesForWeek(selectedWeek);
                }
            });
        } catch (SQLException e) {
            showAlertError("Database Error", "Failed to load weeks from the database.");
        }
    }

    private void loadRecipesForWeek(String selectedWeek) {
        System.out.println("Loading recipes for week: " + selectedWeek); // Debugging statement
        try {
            DbConnector dbConnector = new DbConnector();
            Connection connection = dbConnector.getConnection();

            // Clear the list views before loading new data
            clearListViews();

            // Parse the selected week number
            int weekNumber = Integer.parseInt(selectedWeek);

            // Query to retrieve recipes for each day of the selected week
            String query = "SELECT d.Day_Name, r.* " +
                    "FROM Day d " +
                    "JOIN DinnerList dl ON d.Day_ID = dl.Day_ID " +
                    "JOIN Recipe r ON dl.Recipe_ID = r.Recipe_ID " +
                    "JOIN Week w ON d.Week_ID = w.Week_ID " +
                    "WHERE w.Week_Number = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, weekNumber);
            ResultSet resultSet = statement.executeQuery();

            // Initialize lists for each day
            ObservableList<Recipe> recipesForMonday = FXCollections.observableArrayList();
            ObservableList<Recipe> recipesForTuesday = FXCollections.observableArrayList();
            ObservableList<Recipe> recipesForWednesday = FXCollections.observableArrayList();
            ObservableList<Recipe> recipesForThursday = FXCollections.observableArrayList();
            ObservableList<Recipe> recipesForFriday = FXCollections.observableArrayList();
            ObservableList<Recipe> recipesForSaturday = FXCollections.observableArrayList();
            ObservableList<Recipe> recipesForSunday = FXCollections.observableArrayList();

            // Process the result set
            while (resultSet.next()) {
                String dayName = resultSet.getString("Day_Name");
                Recipe recipe = createRecipeFromResultSet(resultSet);

                // Add the recipe to the corresponding day's list
                switch (dayName) {
                    case "Monday":
                        recipesForMonday.add(recipe);
                        break;
                    case "Tuesday":
                        recipesForTuesday.add(recipe);
                        break;
                    case "Wednesday":
                        recipesForWednesday.add(recipe);
                        break;
                    case "Thursday":
                        recipesForThursday.add(recipe);
                        break;
                    case "Friday":
                        recipesForFriday.add(recipe);
                        break;
                    case "Saturday":
                        recipesForSaturday.add(recipe);
                        break;
                    case "Sunday":
                        recipesForSunday.add(recipe);
                        break;
                }
            }

            // Set up the list views
            setupListView(listMonday, recipesForMonday);
            setupListView(listTuesday, recipesForTuesday);
            setupListView(listWednesday, recipesForWednesday);
            setupListView(listThursday, recipesForThursday);
            setupListView(listFriday, recipesForFriday);
            setupListView(listSaturday, recipesForSaturday);
            setupListView(listSunday, recipesForSunday);

            // Refresh the list views
            refreshListView(listMonday);
            refreshListView(listTuesday);
            refreshListView(listWednesday);
            refreshListView(listThursday);
            refreshListView(listFriday);
            refreshListView(listSaturday);
            refreshListView(listSunday);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlertError("Database Error", "Failed to load recipes for the selected week: " + e.getMessage());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            showAlertError("Error", "Invalid week number format.");
        }
    }

    // Call this method after updating the ObservableList for a ListView
    private void refreshListView(ListView<Recipe> listView) {
        Platform.runLater(() -> {
            listView.refresh();
        });
    }

    private Recipe createRecipeFromResultSet(ResultSet resultSet) throws SQLException {
        try {
            // Extract data from resultSet and create a Recipe object
            int recipeId = resultSet.getInt("Recipe_ID");
            String name = resultSet.getString("Name");
            String shortDescription = resultSet.getString("Short_Description");
            String detailedDescription = resultSet.getString("Detailed_Description");
            int numberOfPeople = resultSet.getInt("Number_of_Persons");
            String tag = resultSet.getString("tag_name");

            // Assuming ingredients and comments are not retrieved in this query
            return new Recipe(recipeId, name, shortDescription, detailedDescription, numberOfPeople,
                    FXCollections.observableArrayList(), tag);
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception
            throw e; // Re-throw the exception to be handled by the calling method
        }
    }

    private void setupListView(ListView<Recipe> listView, ObservableList<Recipe> recipes) {
        listView.setCellFactory(new Callback<ListView<Recipe>, ListCell<Recipe>>() {
            @Override
            public ListCell<Recipe> call(ListView<Recipe> listView) {
                return new ListCell<Recipe>() {
                    @Override
                    protected void updateItem(Recipe recipe, boolean empty) {
                        super.updateItem(recipe, empty);
                        if (empty || recipe == null) {
                            setText(null);
                        } else {
                            setText(recipe.getName());
                        }
                    }
                };
            }
        });

        listView.setItems(recipes);

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !listView.getSelectionModel().isEmpty()) {
                Recipe recipe = listView.getSelectionModel().getSelectedItem();
                showRecipeDetailsPopup(recipe);
            }
        });
    }

    private void showRecipeDetailsPopup(Recipe recipe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hoverRecipe.fxml"));
            Parent root = loader.load();

            HoverRecipeController controller = loader.getController();
            controller.setRecipeDetails(recipe);

            Scene scene = new Scene(root);

            Stage recipeDetailsStage = new Stage();
            recipeDetailsStage.setScene(scene);
            recipeDetailsStage.setTitle("Recipe Details");
            recipeDetailsStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlertError("Error", "Could not load recipe details popup.");
        }
    }

    private void clearListViews() {
        listMonday.getItems().clear();
        listTuesday.getItems().clear();
        listWednesday.getItems().clear();
        listThursday.getItems().clear();
        listFriday.getItems().clear();
        listSaturday.getItems().clear();
        listSunday.getItems().clear();
    }

    private void showAlertError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
