package com.work.cookbook;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

public class LoginController implements Initializable {

    @FXML
    private Label label_username;

    @FXML
    private Label label_password;

    @FXML
    private TextField text_username;

    @FXML
    private PasswordField passwordtxt;

    @FXML
    private Button login;

    @FXML
    private CheckBox showpass;

    private DbOperations dbOperations;

    @FXML
    private Label label_logindetails;

    @FXML
    private Pane login_pane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbOperations = new DbOperations();

        showpass.setOnAction(e1 -> {
            if (showpass.isSelected()) {
                passwordtxt.setPromptText(passwordtxt.getText());
                passwordtxt.setText("");
            } else {
                passwordtxt.setText(passwordtxt.getPromptText());
                passwordtxt.setPromptText("");
            }
        });

        // Fade-in animation for the login pane
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), login_pane);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();

        // Translate animation for the label
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5), label_logindetails);
        translateTransition.setFromX(0);
        translateTransition.setToX(20);
        translateTransition.setAutoReverse(true); // make the label move back and forth
        translateTransition.setCycleCount(1000); // number of times to repeat
        translateTransition.play();
    }

    // autheticate login from db
    @FXML
    private void handleLoginButtonAction() {
        String username = text_username.getText();
        String password;
        // String password = passwordtxt.getText();

        if (showpass.isSelected()) {
            password = passwordtxt.getPromptText();

        } else {
            password = passwordtxt.getText();
        }
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            showAlertError("Login Failed", "Username and/or password cannot be empty.");
            return;
        }

        if (dbOperations.authenticateUser(username, password)) {
            String displayName = dbOperations.fetchDisplayName(username);
            boolean isAdmin = dbOperations.checkAdminStatus(username);

            switchToMainMenu(displayName, isAdmin);
        } else {
            showAlertError("Login Failed", "Incorrect username or password. Please try again.");
        }
    }

    // move to main menu
    private void switchToMainMenu(String loggedInUsername, boolean isAdmin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mainmenu.fxml"));
            Parent root = loader.load();

            mainMenuController controller = loader.getController();
            controller.setLoggedInUsername(loggedInUsername);
            controller.setAdmin(isAdmin);
            controller.updateAdminButtonVisibility(isAdmin);
            controller.updateLoggedInLabel();

            Scene mainMenuScene = new Scene(root);
            Stage currentStage = (Stage) login.getScene().getWindow();
            currentStage.setScene(mainMenuScene);
            currentStage.setTitle("Main Menu");
            currentStage.show();

            // Add animation here (e.g., fade transition)
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), root);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
            showAlertError("Error", "Error loading Main Menu");
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
