package SynchronousVideo.Interface;


import SynchronousVideo.Controller.Controller;
import SynchronousVideo.Server.Server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ApplicationInterface extends Application {

    private static Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/SynchronousVideo/resources/MainInterface.fxml"));
        Parent root = (AnchorPane) fxmlLoader.load();
        controller = fxmlLoader.<Controller>getController();
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 503, 538));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);


    }
}
