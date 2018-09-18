package com.interfaces;


import com.controller.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ApplicationInterface extends Application {

    public static Stage instance;

    @Override
    public void start(Stage primaryStage) throws Exception{
        instance = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/com/interfaces/login.fxml"));
        primaryStage.setTitle("Login Pane");
        primaryStage.setScene(new Scene(root, 300, 200));
        primaryStage.show();
    }

    public static Stage getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        launch(args);
    }
}