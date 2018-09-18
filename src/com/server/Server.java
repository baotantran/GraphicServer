package com.server;

import com.controller.Controller;
import com.message.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
   public static HashSet<ObjectOutputStream> outStreams;
   public static HashSet<ObjectInputStream> inStreams;


   // setup the server
   public Server (int port, Controller controller) {
      this.controller = controller;
      outStreams = new HashSet<ObjectOutputStream>();
      inStreams = new HashSet<ObjectInputStream>();
      try {
         controller.showNotification("Setting up server...");
         server = new ServerSocket(port);
      } catch (IOException e) {
         e.printStackTrace();
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
    public static void sendServerMessage(Message message) {
        for(ObjectOutputStream send: outStreams) {
            try {
                send.writeObject(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


   private static class SocketThreads implements Runnable{
       private Controller controller;
       private Socket connection;
       private ObjectInputStream inMessage;
       private ObjectOutputStream outMessage;
       private static Message message;

       public SocketThreads (Socket socket, Controller controller) {
           this.connection = socket;
           this.controller = controller;
       }

       @Override
       public void run() {
           try {
               inMessage = new ObjectInputStream(connection.getInputStream());
               outMessage = new ObjectOutputStream(connection.getOutputStream());
               outStreams.add(outMessage); // add stream to stream database
               inStreams.add(inMessage);
               while (!connection.isClosed()) {
                   try {
                       message = (Message) inMessage.readObject();
                       if(message.getStringMessage().equalsIgnoreCase("client - end")) {
                           closeConnection();
                           break;
                       }
                       controller.showInMessage(message.getStringMessage());
                       sendClientMessage(message);
                   } catch (IOException e) {
                       e.printStackTrace();
                   } catch (ClassNotFoundException e) {
                       e.printStackTrace();
                   }
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
       }

        // send input message from this socket connection to all other socket connect to server
       private void sendClientMessage(Message message) throws IOException {
           for (ObjectOutputStream send: outStreams) {
               send.writeObject(message);
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
