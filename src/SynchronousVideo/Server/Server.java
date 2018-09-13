package SynchronousVideo.Server;

import SynchronousVideo.Controller.Controller;
import javafx.fxml.FXMLLoader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;


// Developing server for 1 client
public class Server implements Runnable{
   //private static DataInputStream inMessage;
   private static DataOutputStream outMessage;
   private static ServerSocket server;
   private static Socket connection;
   private static Controller controller;
   private static HashSet<DataOutputStream> outStreams;
   private static HashSet<DataInputStream> inStreams;


   // setup the server
   public Server (int port, Controller controller) {
      this.controller = controller;
      outStreams = new HashSet<DataOutputStream>();
      inStreams = new HashSet<DataInputStream>();
      try {
         controller.showNotification("Setting up server...");
         server = new ServerSocket(port);
      } catch (IOException e) {
         e.printStackTrace();
         controller.showNotification("Socket is not available");
      }
   }

   @Override
    public void run() {
       try{
           waitConnection();
       } catch (IOException e) {
           e.printStackTrace();
       } finally {
           closeConnection();
       }
   }

   public void waitConnection() throws IOException {
       while(true) {
           connection = server.accept();
           SocketThreads socket = new SocketThreads(connection, controller);
           Thread thread = new Thread(socket);
           thread.start();
       }
   }

   // Close server
   public static void closeConnection(){
       try {
           server.close();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

    // Send message from server to all clients on send button click
    public static void sendServerMessage(String message) {
        for(DataOutputStream send: outStreams) {
            try {
                send.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


   private static class SocketThreads implements Runnable{
       private Controller controller;
       private Socket connection;
       private DataInputStream inMessage;
       private DataOutputStream outMessage;
       private static String message;

       public SocketThreads (Socket socket, Controller controller) {
           this.connection = socket;
           this.controller = controller;
       }

       @Override
       public void run() {
           try {
               inMessage = new DataInputStream(connection.getInputStream());
               outMessage = new DataOutputStream(connection.getOutputStream());
               outStreams.add(outMessage); // add stream to stream database
               inStreams.add(inMessage);
               while (!connection.isClosed()) {
                   try {
                       message = inMessage.readUTF();
                       if(message.equalsIgnoreCase("client - end")) {
                           closeConnection();
                           break;
                       }
                       controller.showInMessage(message);
                       sendClientMessage(message);
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
       }

        // send input message from this socket connection to all other socket connect to server
       private void sendClientMessage(String message) throws IOException {
           for (DataOutputStream send: outStreams) {
               send.writeUTF(message);
           }
       }

       private void closeConnection() {
           try {
               inStreams.remove(inMessage);
               outStreams.remove(outMessage);
               inMessage.close();
               outMessage.close();
               connection.close();
               controller.showNotification("A client ended connection");
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
   }
}
