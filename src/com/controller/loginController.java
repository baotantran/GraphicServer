package com.controller;

import com.jfoenix.controls.JFXTextField;
import com.controller.Controller;
import com.server.Server;
import javafx.fxml.FXML;
import com.interfaces.ApplicationInterface;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class loginController {

    @FXML
    private JFXTextField serverName;
    private static Stage instance;

    private static Controller controller;

    @FXML
    public void login() throws Exception {
        String name = serverName.getText();
        Stage oldStage = ApplicationInterface.getInstance();
        oldStage.close();
        Stage newStage = new Stage();
        instance = newStage;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/interfaces/MainInterface.fxml"));
        Parent root = (Parent) fxmlLoader.load();
        // Pass server name into main interface controller
        controller = fxmlLoader.getController();
        controller.setServerName(name);
        //--------------------------------------------------
        Scene scene = new Scene(root, 500, 600);
        newStage.setResizable(false);
        newStage.setTitle("Synchronous Video Chat");
        newStage.setScene(scene);
        newStage.show();
    }

    public static Stage getInstance() {
        return instance;
    }
}
