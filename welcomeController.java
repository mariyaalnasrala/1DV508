package com.work.cookbook;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import java.io.IOException;

public class welcomeController implements Initializable {

    @FXML
    private ProgressBar progress;

    @FXML
    private Button btn_cook;

    private BooleanProperty progressBarCompleted = new SimpleBooleanProperty(false);

    // Init the welcome screen
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Create a task to simulate progress
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 0; i <= 100; i++) {
                    final int progressValue = i;
                    // Update progress on UI thread
                    updateProgress(progressValue, 100);
                    Thread.sleep(10); // Simulate work being done
                }
                progressBarCompleted.set(true); // Set progress bar completed flag to true
                return null;
            }
        };

        // progress bar progress to the task progress
        progress.progressProperty().bind(task.progressProperty());

        // button visibility to progress bar completion
        btn_cook.visibleProperty().bind(progressBarCompleted);

        // Start the task in a background thread
        new Thread(task).start();
    }

    private Transition createFadeTransition(Parent root, double durationSeconds, double fromValue, double toValue) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(durationSeconds), root);
        fadeTransition.setFromValue(fromValue);
        fadeTransition.setToValue(toValue);
        return fadeTransition;
    }

    // load login screen when clikc on cook
    @FXML
    private void cook() {
        try {
            // Load the login screen FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
            Parent root = loader.load();

            // Create the scene
            Scene loginScene = new Scene(root);

            // Get the stage and set the scene
            Stage stage = (Stage) progress.getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("Login");
            stage.show();

            Transition fadeTransition = createFadeTransition(root, 0.5, 0, 1);
            fadeTransition.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
