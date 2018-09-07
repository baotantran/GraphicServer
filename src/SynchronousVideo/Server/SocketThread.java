package SynchronousVideo.Server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import SynchronousVideo.Controller.Controller;

// Stream input data
public class SocketThread implements Runnable{
    private Socket connection;
    private Controller controller;
    private DataInputStream inMessage;

    public SocketThread(Socket socket, Controller controller) {
        this.connection = socket;
        this.controller = controller;
    }

    @Override
    public void run() {
        try {
            inMessage = new DataInputStream(connection.getInputStream());
            while(connection.isConnected()) {
                controller.showInMessage(inMessage.readUTF());
            }
            controller.showNotification("Client ended connection");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
