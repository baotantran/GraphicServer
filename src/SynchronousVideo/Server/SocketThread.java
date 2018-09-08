package SynchronousVideo.Server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

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
            String message = "";
            inMessage = new DataInputStream(connection.getInputStream());
            while(!connection.isClosed() && !message.equalsIgnoreCase("client - end")) {
                message = inMessage.readUTF();
                controller.showInMessage(message);
            }
            controller.showNotification("Client ended connection");
            Server.closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
