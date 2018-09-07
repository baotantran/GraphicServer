package SynchronousVideo.Server;

import SynchronousVideo.Controller.Controller;
import javafx.fxml.FXMLLoader;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


// Developing server for 1 client
public class Server {
   //private static DataInputStream inMessage;
   private static DataOutputStream outMessage;
   private static ServerSocket server;
   private static Socket connection;
   public static Controller controller;


   // setup the server
   public Server (int port, Controller controller) {
      this.controller = controller;
      try {
         System.out.println("Setting up server...");
         server = new ServerSocket(port);
      } catch (IOException e) {
         e.printStackTrace();
         System.out.println("Socket is not available");
      }
   }

   // Server setup for 1 use
   public void setConnection() {
      try {
         waitConnection();
         setupStream();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void waitConnection() throws IOException{
      System.out.println("Waiting for connection...");
      connection = server.accept();
      System.out.println("Connected to:" + connection.getRemoteSocketAddress());
      SocketThread socket = new SocketThread(connection, controller);
      Thread t = new Thread(socket);
      t.start();
   }

   public static void closeConnection() {
      try {
         connection.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }



   public void setupStream() throws IOException {
      System.out.println("Setting up output stream");
      //inMessage = new DataInputStream(connection.getInputStream());
      outMessage = new DataOutputStream(connection.getOutputStream());
      System.out.println("Output stream is setup!");
   }


   public static void sendMessage(String message) {
      if(!connection.isClosed()) {
         if(message.equalsIgnoreCase("end")) {
            closeConnection();
         } else {
            try {
               outMessage.writeUTF(message);
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      } else {
         controller.showNotification("The connection is closed");
         controller.showNotification("Waiting for next connection...");
      }
   }


}
