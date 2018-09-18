package com.controller;

import javafx.fxml.FXML;
import com.interfaces.ApplicationInterface;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class loginController {

    @FXML
    public void login() throws Exception {
        Stage oldStage = ApplicationInterface.getInstance();
        oldStage.close();
        Stage newStage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("/com/interfaces/MainInterface.fxml"));
        Scene scene = new Scene(root, 500, 600);
        newStage.setResizable(false);
        newStage.setTitle("Synchronous Video Chat");
        newStage.setScene(scene);
        newStage.show();
    }
}
