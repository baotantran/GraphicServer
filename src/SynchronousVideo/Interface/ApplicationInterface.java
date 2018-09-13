package SynchronousVideo.Interface;


import SynchronousVideo.Controller.Controller;
import SynchronousVideo.Server.Server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ApplicationInterface extends Application {

    private static Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/SynchronousVideo/resources/MainInterface.fxml"));
        primaryStage.setTitle("Synchronous Video Chat");
        primaryStage.setScene(new Scene(root, 500, 600));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);


    }
}
