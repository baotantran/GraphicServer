package SynchronousVideo.Controller;

import SynchronousVideo.Server.Server;
import SynchronousVideo.Server.SocketThread;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class Controller {

    private static Controller controllerInstance;

    public Controller() {
        controllerInstance = this;
    }

    public static Controller getInstance() {
        return controllerInstance;
    }

    @FXML TextArea message;
    @FXML TextField userIn;

    // Show message sent from server
    public void showOutMessage() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                message.appendText("Server: " + userIn.getText() + "\n");
                userIn.setText("");
            }
        });
    }

    // Show message from client to server
    public void showInMessage(String mess) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                message.appendText("Client: " + mess + "\n");
            }
        });
    }

    public void showNotification(String mess) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                message.appendText(mess + "\n");
            }
        });
    }

    public void sendMessage() {
        Server.sendServerMessage(userIn.getText());
        showOutMessage();
    }

    public void startServer() {
        Controller controller = getInstance();
        Server server = new Server(5678, controller);
        Thread t = new Thread(server);
        t.start();
    }

}
