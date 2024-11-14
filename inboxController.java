package com.work.cookbook;

import com.work.cookbook.model.Message;
import com.work.cookbook.model.Recipe;

import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

public class inboxController implements Initializable {

    @FXML
    private Button btn_back;

    @FXML
    private TableView<Message> messages;

    @FXML
    private TableColumn<Message, Timestamp> timeColumn;

    @FXML
    private TableColumn<Message, String> messagesColumn;

    @FXML
    private TextArea receivedRecipe;

    @FXML
    private TextArea message;
    private String loggedInUsername;

    private boolean isAdmin;

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setLoggedInUsername(String loggedInUsername) {
        this.loggedInUsername = loggedInUsername;
        getMessages();
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    // init with load message method and implemented logic to click on message to
    // view details
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<Message> data = getMessages();
        messages.setItems(data);

        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        messagesColumn.setCellValueFactory(new PropertyValueFactory<>("sender"));

        messages.setRowFactory(tv -> {
            TableRow<Message> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    Message clickedRow = row.getItem();
                    displayMessageDetails(clickedRow);
                }
            });
            return row;
        });
    }

    // get all messages
    private ObservableList<Message> getMessages() {

        ObservableList<Message> messageList = DbOperations.fetchMessages(loggedInUsername);

        // Print the size of the fetched list for troubleshoot
        // System.out.println("Number of messages fetched: " + messageList.size() + " of
        // User " + "' "+ loggedInUsername + " '");

        // Populate the table with the fetched msgs
        messages.setItems(messageList);
        return messageList;
    }

    // display details of the message after selecting a message
    private void displayMessageDetails(Message message) {
        // Fetch recipe details
        String receivedRecipe = DbOperations.fetchRecipeDetails(message.getRecipe_ID());

        // Fetch message details
        String messageDetails = DbOperations.fetchMessageDetails(message.getMessage_ID());

        // Set received recipe and message details in respective TextAreas
        this.receivedRecipe.setText(receivedRecipe); // receivedRecipe is a string
        this.message.setText(messageDetails); // Set message text in TextArea
    }

    private Transition createFadeTransition(Parent root, double durationSeconds, double fromValue, double toValue) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(durationSeconds), root);
        fadeTransition.setFromValue(fromValue);
        fadeTransition.setToValue(toValue);
        return fadeTransition;
    }

    // go to mainmenu
    @FXML
    private void btn_backClicked(ActionEvent event) {
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
            showAlertError("Error", "Could not load main menu.");
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
