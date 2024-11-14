package com.work.cookbook;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class mainMenuController implements Initializable {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private ImageView backgroundImage;
    @FXML
    private Pane mainPane;

    @FXML
    private Label loggedInLabel;

    @FXML
    private HBox adminBox;
    @FXML
    private Button btn_admin;

    @FXML
    private ImageView adminImage;

    @FXML
    private HBox addRecipeBox;
    @FXML
    private Button btn_add_recipe;

    @FXML
    private ImageView addRecipeImage;

    @FXML
    private HBox inboxBox;

    @FXML
    private Button btn_inbox;

    @FXML
    private ImageView inboxImage;

    @FXML
    private HBox weeklyListBox;

    @FXML
    private Button btn_weeklylist;

    @FXML
    private ImageView weeklyListImage;

    @FXML
    private HBox helpBox;

    @FXML
    private Button btn_help;

    @FXML
    private ImageView helpImage;

    @FXML
    private HBox quitBox;

    private Stage stage;

    @FXML
    private Button btn_quit;

    @FXML
    private Button btn_searchRecipe;

    @FXML
    private Button btn_shopping;

    @FXML
    private ImageView quitImage;
    @FXML
    private Button btn_fav_recipe;

    private String loggedInUsername;
    private boolean isAdmin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (loggedInUsername != null && !loggedInUsername.isEmpty()) {
            loggedInLabel.setText("You are logged in as: " + loggedInUsername);
        } else {
            loggedInLabel.setText("You are logged in.");
        }
        updateLoggedInLabel();
        // setAdminButtonVisibility(isAdmin);
    }

    public void setLoggedInUsername(String displayName) {
        this.loggedInUsername = displayName;
        updateLoggedInLabel();
        // updateAdminButtonVisibility(isAdmin);

    }

    public void updateLoggedInLabel() {
        if (loggedInUsername != null && !loggedInUsername.isEmpty()) {
            loggedInLabel.setText(loggedInUsername);
        } else {
            loggedInLabel.setText("----");
        }
        setAdminButtonVisibility(isAdmin);
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        updateAdminButtonVisibility(isAdmin);
    }

    public void updateAdminButtonVisibility(boolean isAdmin) {
        this.isAdmin = isAdmin;
        btn_admin.setVisible(isAdmin);
    }

    // btn access if isadmin
    public void setAdminButtonVisibility(boolean isAdmin) {
        btn_admin.setVisible(isAdmin);
    }
    // trasition effecr

    private Transition createFadeTransition(Parent root, double durationSeconds, double fromValue, double toValue) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(durationSeconds), root);
        fadeTransition.setFromValue(fromValue);
        fadeTransition.setToValue(toValue);
        return fadeTransition;
    }

    // calls this method when the user clicks on a button
    @FXML
    private void admin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("adminPanelnew.fxml"));
            Parent root = loader.load();

            adminPanelnewController controller = loader.getController();
            controller.setLoggedInUsername(loggedInUsername);
            controller.setAdmin(isAdmin);
            // Populate the user table
            controller.populateUserTable();

            Scene addRecipeScene = new Scene(root);
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

            // Set the new scene
            currentStage.setScene(addRecipeScene);
            currentStage.setTitle("Admin Panel");
            currentStage.show();

            Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error loading Admin Panel page");
        }
    }
    /*
     * inbox screen
     * 
     */

    @FXML
    private void inbox(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("inbox.fxml"));
            Parent root = loader.load();
            System.err.println("Loggin in " + loggedInUsername); // Print loggedInUsername before setting it

            inboxController controller = loader.getController();
            controller.setLoggedInUsername(loggedInUsername);
            controller.setAdmin(isAdmin); // Set the isAdmin value

            Scene inboxscene = new Scene(root);
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

            // Set the new scene
            currentStage.setScene(inboxscene);
            currentStage.setTitle("Inbox");
            currentStage.show();

            Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error loading Inbox page");
        }
    }

    /*
     * weekly list screen
     */
    @FXML
    private void weeklyList(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("weeklyList.fxml"));
            Parent root = loader.load();

            weeklyListController controller = loader.getController();
            controller.setLoggedInUsername(loggedInUsername);
            controller.setIssAdmin(isAdmin); // Set the isAdmin value

            Scene addRecipeScene = new Scene(root);
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

            // Set the new scene
            currentStage.setScene(addRecipeScene);
            currentStage.setTitle("Weekly List");
            currentStage.show();

            Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error loading Weekly List page");
        }
    }

    /*
     * help screen
     */
    @FXML
    private void help(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("help.fxml"));
            Parent root = loader.load();

            helpController controller = loader.getController();
            controller.setLoggedInUsername(loggedInUsername);
            controller.setAdmin(isAdmin); // Set the isAdmin value

            Scene helpScene = new Scene(root);
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

            // Set the new scene
            currentStage.setScene(helpScene);
            currentStage.setTitle("Help");
            currentStage.show();

            Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error loading Help page");
        }
    }

    /*
     * search recipe screen
     */
    @FXML
    private void searchRecipe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("searchRecipe.fxml"));
            Parent root = loader.load();

            searchRecipeController controller = loader.getController();
            controller.setLoggedInUsername(loggedInUsername);
            controller.setAdmin(isAdmin); // Set the isAdmin value

            Scene searchRecipeScene = new Scene(root);
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

            // Set the new scene
            currentStage.setScene(searchRecipeScene);
            currentStage.setTitle("Search Recipe");
            currentStage.show();

            Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error loading Add Recipe page");
        }

    }

    /*
     * nav to fav recipe screen
     */
    @FXML
    private void fav_recipe(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("favRecipe.fxml"));
            Parent root = loader.load();
            System.err.println("Loggin in " + loggedInUsername); // Print loggedInUsername before setting it

            favRecipeController controller = loader.getController();
            controller.setLoggedInUsername(loggedInUsername);
            controller.setAdmin(isAdmin);

            Scene favRecipeScene = new Scene(root);
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

            currentStage.setScene(favRecipeScene);
            currentStage.setTitle("Favourite Recipe");
            currentStage.show();

            Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error loading Favourite Recipe page");
        }
    }

    /*
     * nav to shopping list screen
     * 
     */
    @FXML
    private void shoppingList(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("shoppingList.fxml"));
            Parent root = loader.load();

            shoppingListController controller = loader.getController();
            controller.setLoggedInUsername(loggedInUsername);
            controller.setAdmin(isAdmin);

            Scene shoppingListScene = new Scene(root);
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

            currentStage.setScene(shoppingListScene);
            currentStage.setTitle("Shopping List");
            currentStage.show();
            Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Error loading Favourite Recipe page");
        }

    }

    /*
     * logoff button
     */
    @FXML
    private void logOff(ActionEvent event) {
        showAlert("Quit", "You have logged out!");

        // Switch back to login scene
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            Scene loginScene = new Scene(root);
            Stage currentStage = (Stage) ((Button) event.getSource()).getScene().getWindow();

            currentStage.setScene(loginScene);
            currentStage.setTitle("Login");
            currentStage.show();

            Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
