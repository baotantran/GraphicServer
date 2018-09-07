package SynchronousVideo.Controller;

import SynchronousVideo.Server.Server;
import SynchronousVideo.Server.SocketThread;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class Controller {

    @FXML TextArea message;
    @FXML TextField userIn;

    public void showOutMessage() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                message.appendText("Server: " + userIn.getText() + "\n");
                userIn.setText("");
            }
        });
    }

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
                message.appendText(mess);
            }
        });

    }

    public void sendMessage() {
        Server.sendMessage(userIn.getText());
        showOutMessage();
    }
}
