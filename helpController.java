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
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class helpController implements Initializable {

    @FXML
    private ListView<String> menuList;

    @FXML
    private TextField search_help;

    @FXML
    private TextArea helpContentArea;

    @FXML
    private ImageView helpImageView;

    private boolean isAdmin;

    public boolean isAdmin() {
        return isAdmin;
    }

    private String loggedInUsername;

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    private Transition createFadeTransition(Parent root, double durationSeconds, double fromValue, double toValue) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(durationSeconds), root);
        fadeTransition.setFromValue(fromValue);
        fadeTransition.setToValue(toValue);
        return fadeTransition;
    }

    // Sample help topics
    private final ObservableList<String> helpTopics = FXCollections.observableArrayList(
            "Browse Recipe",
            "Search Recipe",
            "Add Recipe",
            "Delete Recipe",
            "Modify Recipe",
            "Favourite Recipe",
            "Send Recipe",
            "Comment Recipe",
            "Messages",
            "Weekly List",
            "Remove Recipe from favorite list",
            "Generate shopping list",
            "View shopping list",
            "Remove item from shopping list",
            "Admin Panel");

    // Sample help content
    private final String[] helpContent = {
            "To browse recipes, use the navigation menu on the left side of the screen." +
                    "\nClick on the 'Browse Recipe' option to view a list of available recipes." +
                    "\nYou can double click on a recipe to view its details." +
                    "You can also search for recipes in alphabetical order by clicking on the 'Name' column." +
                    "This will display the recipes from A to Z. If you click on the 'Name' column again, " +
                    "you can see the recipes sorted in reverse alphabetical order, from Z to A.|img/browse_recipe.png",

            "To search recipe, go to the 'Browse Recipe' screen option in the navigation menu." +
                    " \nEnter the name of the recipe in the search bar and click 'Enter'. |img/browse_recipe.png",

            "To add a new recipe, go to the 'Browse Recipe' screen option in the navigation menu.\nClick on the 'Add Recipe'"
                    + "\nFill in the required information such as the recipe name, ingredients, and description. " +
                    "\nClick on the 'Add recipe' button to add the recipe to the list. |img/add-recipe.jpg",

            "To delete a recipe, go to the 'Browse Recipe' screen and select the recipe you want to delete from the list. "
                    + "\nClick on the 'Delete Recipe' button to remove the recipe from the list." +
                    "\nPlease note that this action is irreversible. |img/delete-recipe.jpg",

            "To modify a recipe, select the recipe you want to modify from the list and\n" +
                    "click the 'Modify Recipe' button. This will open a new window where you can change the details of the recipe."
                    +
                    "\nOnce you've made your changes, click 'Confirm' to update the recipe in the list. |img/modify-recipe.jpg",

            "To mark a recipe as a favorite, go to the 'Browse Recipe' screen and select the recipe." +
                    "\nClick on the 'Favorite Recipe' button '❤' to add the recipe to your favorites list." +
                    "\nYou can access your favorite recipes by selecting the 'Favorite Recipes' option in the navigation menu. | img/favorite-recipe.jpg",

            "To send a recipe, go to the 'Browse Recipes' screen and select the recipe you want to send." +
                    " \nClick on the 'Send Recipe' button on the left. Select the recipient's username, enter a message,"
                    +
                    " \nand then click the 'Confirm' button to send the recipe. |img/send-recipe.jpg",

            "To comment on a recipe, go to the 'Browse Recipes' screen and select the recipe you want to comment on." +
                    "\nThen, click the 'Comment' button '💬', enter your comment, and click 'Confirm'." +
                    "\nYou can also modify your existing comment or add additional comments in the same way. | img/comment-recipe.jpg",

            "In the inbox, you can see all of your messages. For each message, you can view the date, the sender's name,"
                    +
                    "the recipe details, and the message content. By clicking on the sender's name, you can see the full"
                    +
                    "message details and recipe details. | img/messages.jpg",

            "In the weekly list, you can see a recipe for every day of the year. You can select a specific week and see the"
                    +
                    "recipe alternatives available to cook for that week or day.\nAdditionally, you can click on a recipe"
                    +
                    "to view its detailed information. | img/weekly-list.jpg",

            "To easily remove a recipe from your favorites, go to the 'Favorite Recipes' section on the left side of the screen."
                    +
                    "\nSelect the recipe you want to remove, then click the 'Remove' button.\n" +
                    "Confirm the removal to complete the process. | img/remove-favorite.jpg",

            "Go to the 'Shopping List' section. Select a specific week to create a shopping list for that week." +
                    "\nEnter the name and quantity of each ingredient you need. You can see the ingredients you've" +
                    " added written in the cart.\nWhen you're finished, click 'Generate Shopping List' to complete the process. |img/generate-shoppinglist.jpg",

            "To view a shopping list, go to the 'Shopping List' section. Select a specific week to see the\n" +
                    "shopping list of ingredients for that week. |img/view-shoppinglist.jpg",

            "To remove an item from your shopping list, follow these steps:\r\n" + //
                    "Go to the 'Shopping List' section.\r\n" + //
                    "Select the week you want to modify.\r\n" + //
                    "Click on the item you want to remove.\r\n" + //
                    "Click the 'Remove' button to delete the item.\r\n" + //
                    "You can also click on the 'Name' column to sort the items in alphabetical order,\n" +
                    "from A to Z. Clicking the 'Name' column again will sort the items in reverse alphabetical" +
                    " order, from Z to A. You can also click on the 'Quantity' column to sort the items" +
                    " from the smallest quantity to the largest. |img/remove-item.jpg",
            "To access the admin panel, you need to have admin privileges. " +
                    "\nIf you are an admin, click on the 'Admin Panel' option in the navigation menu. " +
                    "\nIn the admin panel, you can manage user accounts, view logs, and perform other administrative tasks. |img/admin-panel.jpg"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Populate menuList with help topics
        menuList.setItems(helpTopics);

        // Initialize help content area
        helpContentArea.setEditable(false);

        // Display default help content
        displayHelpContent(helpContent[0]);

        // Listen for selection changes in the menu list
        menuList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Get the index of the selected item
                int selectedIndex = menuList.getSelectionModel().getSelectedIndex();
                displayHelpContent(helpContent[selectedIndex]);
            }
        });

        // Listen for changes in the search field
        search_help.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchText = newValue.toLowerCase();

            // Filter help topics
            ObservableList<String> filteredHelpTopics = FXCollections.observableArrayList();
            for (String helpTopic : helpTopics) {
                if (helpTopic.toLowerCase().contains(searchText)) {
                    filteredHelpTopics.add(helpTopic);
                }
            }

            // Update the menu list
            menuList.setItems(filteredHelpTopics);

            // Reset the help content area if no search text is entered
            if (searchText.isEmpty()) {
                displayHelpContent(helpContent[0]);
            } else if (!filteredHelpTopics.isEmpty()) {
                // Display the help content of the first matching topic
                int selectedIndex = helpTopics.indexOf(filteredHelpTopics.get(0));
                displayHelpContent(helpContent[selectedIndex]);
            } else {
                // No matching topics found
                helpContentArea.setText("No matching topics found.");
                helpImageView.setImage(null); // Clear the image if no match is found
            }
        });
    }

    private void displayHelpContent(String content) {
        // split the content to separate text and image path
        String[] parts = content.split("\\|");
        String text = parts[0].trim();
        String imagePath = parts.length > 1 ? parts[1].trim() : null;

        // Display the text content
        helpContentArea.setText(text);

        // Display the image if available
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                helpImageView.setImage(image);
                helpImageView.setFitWidth(730); // Adjust the width
                helpImageView.setPreserveRatio(true);
            } catch (Exception e) {
                e.printStackTrace();
                helpImageView.setImage(null);
                helpContentArea.setText(text + "\n\n[Image could not be loaded]");
            }
        } else {
            helpImageView.setImage(null); // Clear the image if no image path is provided
        }
    }

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

    private void showAlertError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
