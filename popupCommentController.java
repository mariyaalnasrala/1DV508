package com.work.cookbook;

import java.net.URL;
import java.util.ResourceBundle;
import com.work.cookbook.model.Comment;
import com.work.cookbook.model.Recipe;
import com.work.cookbook.DbOperations;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.application.Platform;

public class popupCommentController implements Initializable {

    @FXML
    private Button btn_comment;

    @FXML
    private Button btn_delete;

    @FXML
    private TextField commentField;
    private Integer commentId;
    private Recipe selectedRecipe;
    private String loggedInUsername;
    private DbOperations dbOperations = new DbOperations();

    private searchRecipeController searchController;

    public void setSearchRecipeController(searchRecipeController controller) {
        this.searchController = controller;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialization code here, if needed
    }

    public void setupCommentController(Recipe selectedRecipe, String loggedInUsername) {
        this.selectedRecipe = selectedRecipe;
        this.loggedInUsername = loggedInUsername;

        // Fetch the existing comment ID and text for the user and recipe
        Comment existingComment = dbOperations.getCommentByUserAndRecipe(selectedRecipe.getRecipeId(),
                loggedInUsername);
        if (existingComment != null) {
            commentId = existingComment.getCommID();
            commentField.setText(existingComment.getCommentText());
        } else {
            commentField.clear(); // Clear the field if no comment exists
            commentId = null; // Reset the comment ID
        }
    }

    /*
     * deleet the comment logic
     */
    @FXML
    private void deleteComment() {
        if (commentId != null) {
            // Delete the existing comment
            boolean success = dbOperations.deleteComment(commentId);
            if (success) {
                // Remove the comment from the ObservableList<Comment>
                selectedRecipe.getComments().removeIf(c -> c.getCommID() == commentId);
                // Close the popup
                Stage stage = (Stage) btn_delete.getScene().getWindow();
                stage.close();
                // Refresh the comments view on the JavaFX Application Thread
                Platform.runLater(() -> searchController.refreshTable());
            } else {
                showAlert("Error", "Failed to delete the comment.", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Information", "No comment to delete.", Alert.AlertType.INFORMATION);
        }
    }

    /*
     * add comment
     */
    @FXML
    private void commentRecipe() {
        String commentText = commentField.getText();
        if (!commentText.isEmpty()) {
            boolean success;
            if (commentId != null) {
                // Update the existing comment
                success = dbOperations.updateComment(commentId, commentText);
                // Update the ObservableList<Comment> for the selectedRecipe
                selectedRecipe.getComments().stream()
                        .filter(c -> c.getCommID() == commentId)
                        .findFirst()
                        .ifPresent(c -> c.setCommentText(commentText));
            } else {
                // Add a new comment
                Comment newComment = dbOperations.addComment(selectedRecipe.getRecipeId(), loggedInUsername,
                        commentText);
                success = newComment != null;
                // Add the new comment to the ObservableList<Comment>
                if (success) {
                    selectedRecipe.getComments().add(newComment);
                }
            }

            if (success) {
                // Close the popup
                Stage stage = (Stage) btn_comment.getScene().getWindow();
                stage.close();
                // Refresh the comments view on the JavaFX Application Thread
                Platform.runLater(() -> searchController.refreshTable());
            } else {
                showAlert("Error", "Failed to process the comment.", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Error", "Comment cannot be empty.", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}