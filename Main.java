package com.work.cookbook;

import java.util.TimeZone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
 * Main class to run app
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Parent root = FXMLLoader.load(getClass().getResource("welcome.fxml"));
        primaryStage.setTitle("Welcome");
        primaryStage.setScene(new Scene(root, 1285, 778));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
